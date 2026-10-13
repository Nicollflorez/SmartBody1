package com.smartbody.service;

import com.smartbody.dto.AdminDtos;
import com.smartbody.entity.Medida;
import com.smartbody.entity.NivelExperiencia;
import com.smartbody.entity.Objetivo;
import com.smartbody.entity.Perfil;
import com.smartbody.entity.Rol;
import com.smartbody.entity.Usuario;
import com.smartbody.repository.MedidaRepository;
import com.smartbody.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Reporte de efectividad del administrador. */
class ReporteServiceTest {

    private final List<Medida> medidas = new ArrayList<>();

    private Usuario usuario(long id, String nombre, String codigoObjetivo, String nombreObjetivo, double... pesos) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setNombre(nombre);
        usuario.agregarRol(new Rol(Rol.USUARIO));

        Objetivo objetivo = new Objetivo();
        ReflectionTestUtils.setField(objetivo, "codigo", codigoObjetivo);
        ReflectionTestUtils.setField(objetivo, "nombre", nombreObjetivo);
        Perfil perfil = new Perfil();
        perfil.setEdad(30);
        perfil.setPesoKg(pesos.length > 0 ? pesos[pesos.length - 1] : 70.0);
        perfil.setAlturaCm(170.0);
        perfil.setNivelExperiencia(NivelExperiencia.PRINCIPIANTE);
        perfil.setMinutosPorSesion(45);
        perfil.setDiasPorSemana(3);
        perfil.setObjetivo(objetivo);
        usuario.setPerfil(perfil);

        LocalDateTime inicio = LocalDateTime.now().minusDays(60);
        for (int i = 0; i < pesos.length; i++) {
            Medida medida = new Medida();
            medida.setUsuario(usuario);
            medida.setPesoKg(pesos[i]);
            medida.setImc(Perfil.calcularImc(pesos[i], 170.0));
            ReflectionTestUtils.setField(medida, "registradoEn", inicio.plusDays(30L * i));
            medidas.add(medida);
        }
        return usuario;
    }

    @Test
    void calculaElPorcentajeDeUsuariosQueMejoraronSegunSuObjetivo() {
        Usuario laura = usuario(1, "Laura", "PERDER_PESO", "Perder peso", 70, 68, 66.5);
        Usuario julian = usuario(2, "Julián", "PERDER_PESO", "Perder peso", 92, 93.1);
        Usuario andres = usuario(3, "Andrés", "GANAR_MASA", "Ganar masa muscular", 64, 67.2);
        Usuario sinSeguimiento = usuario(4, "Nuevo", "GANAR_MASA", "Ganar masa muscular", 70);
        Usuario admin = new Usuario();
        ReflectionTestUtils.setField(admin, "id", 5L);
        admin.agregarRol(new Rol(Rol.ADMINISTRADOR));

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        MedidaRepository medidaRepository = mock(MedidaRepository.class);
        when(usuarioRepository.findAllParaAdministracion()).thenReturn(List.of(laura, julian, andres, sinSeguimiento, admin));
        when(medidaRepository.findAllByOrderByRegistradoEnAsc()).thenReturn(medidas);

        AdminDtos.ReporteEfectividad reporte = new ReporteService(usuarioRepository, medidaRepository).efectividad();

        assertThat(reporte.usuariosConPerfil()).isEqualTo(4);
        assertThat(reporte.usuariosConSeguimiento()).isEqualTo(3);
        assertThat(reporte.usuariosQueMejoraron()).isEqualTo(2);
        assertThat(reporte.porcentajeMejora()).isEqualTo(66.7);

        AdminDtos.EfectividadObjetivo perderPeso = reporte.porObjetivo().get(0);
        assertThat(perderPeso.objetivo()).isEqualTo("Perder peso");
        assertThat(perderPeso.usuarios()).isEqualTo(2);
        assertThat(perderPeso.porcentajeMejora()).isEqualTo(50.0);
        assertThat(perderPeso.promedioCambioPesoKg()).isEqualTo(-1.2);

        // Primero quienes mejoraron (de mayor a menor cambio), luego el resto.
        assertThat(reporte.usuarios()).extracting(AdminDtos.ProgresoUsuario::nombre).containsExactly("Laura", "Andrés", "Julián");
    }
}
