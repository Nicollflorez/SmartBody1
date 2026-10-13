package com.smartbody.service;

import com.smartbody.dto.AdminDtos;
import com.smartbody.entity.Rol;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.MedidaRepository;
import com.smartbody.repository.PlanNutricionalRepository;
import com.smartbody.repository.RutinaRepository;
import com.smartbody.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas del panel de administracion. */
class AdminServiceTest {

    private UsuarioRepository usuarioRepository;
    private RutinaRepository rutinaRepository;
    private PlanNutricionalRepository planRepository;
    private MedidaRepository medidaRepository;
    private AdminService adminService;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        rutinaRepository = mock(RutinaRepository.class);
        planRepository = mock(PlanNutricionalRepository.class);
        medidaRepository = mock(MedidaRepository.class);
        adminService = new AdminService(usuarioRepository, rutinaRepository, planRepository, medidaRepository);
    }

    private static Usuario usuario(long id, String nombre, String rol, LocalDateTime creadoEn) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        ReflectionTestUtils.setField(usuario, "creadoEn", creadoEn);
        usuario.setNombre(nombre);
        usuario.setEmail(nombre.toLowerCase() + "@smartbody.com");
        usuario.agregarRol(new Rol(rol));
        return usuario;
    }

    @Test
    void desactivaLaCuentaDeUnUsuario() {
        Usuario ana = usuario(2, "Ana", Rol.USUARIO, LocalDateTime.now());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(ana));

        adminService.cambiarEstado(2L, false);

        assertThat(ana.isActivo()).isFalse();
    }

    @Test
    void noPermiteDesactivarUnaCuentaDeAdministrador() {
        Usuario admin = usuario(1, "Admin", Rol.ADMINISTRADOR, LocalDateTime.now());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminService.cambiarEstado(1L, false))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("administrador");
        assertThat(admin.isActivo()).isTrue();
    }

    @Test
    void elResumenReuneLasEstadisticasYOrdenaLaActividadDeLaMasReciente() {
        LocalDateTime ahora = LocalDateTime.now();
        when(usuarioRepository.count()).thenReturn(3L);
        when(usuarioRepository.countByActivoTrue()).thenReturn(2L);
        when(usuarioRepository.countByCreadoEnAfter(any())).thenReturn(1L);
        when(usuarioRepository.contarPerfilesCompletos()).thenReturn(2L);
        when(rutinaRepository.count()).thenReturn(5L);
        when(planRepository.count()).thenReturn(4L);
        when(medidaRepository.count()).thenReturn(9L);
        when(usuarioRepository.contarPorObjetivo()).thenReturn(List.<Object[]>of(new Object[]{"Perder peso", 2L}));
        when(usuarioRepository.contarPorRestriccion()).thenReturn(List.<Object[]>of(new Object[]{"Lesión de rodilla", 1L}));
        when(usuarioRepository.findTop20ByOrderByCreadoEnDesc()).thenReturn(List.of(
                usuario(1, "Antiguo", Rol.USUARIO, ahora.minusDays(10)),
                usuario(2, "Nuevo", Rol.USUARIO, ahora.minusHours(1))));

        AdminDtos.Resumen resumen = adminService.resumen();

        assertThat(resumen.totalUsuarios()).isEqualTo(3);
        assertThat(resumen.rutinasGeneradas()).isEqualTo(5);
        assertThat(resumen.usuariosPorObjetivo()).containsExactly(new AdminDtos.Conteo("Perder peso", 2));
        assertThat(resumen.actividadReciente()).extracting(AdminDtos.Actividad::usuario).containsExactly("Nuevo", "Antiguo");
    }
}
