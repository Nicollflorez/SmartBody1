package com.smartbody.service;

import com.smartbody.dto.AdminDtos;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Consulta y gestion de un usuario concreto desde el panel de administracion.
 * Reutiliza los mismos servicios que usa la aplicacion, por lo que el administrador ve exactamente
 * la misma rutina, plan y progreso que el usuario.
 *
 * <p>No es transaccional a proposito: cada consulta usa su propia transaccion y la ausencia de
 * rutina o plan (404) no debe invalidar las demas.</p>
 */
@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AdminService adminService;
    private final PerfilService perfilService;
    private final RutinaService rutinaService;
    private final NutricionService nutricionService;
    private final ProgresoService progresoService;

    public AdminUsuarioService(UsuarioRepository usuarioRepository,
                               AdminService adminService,
                               PerfilService perfilService,
                               RutinaService rutinaService,
                               NutricionService nutricionService,
                               ProgresoService progresoService) {
        this.usuarioRepository = usuarioRepository;
        this.adminService = adminService;
        this.perfilService = perfilService;
        this.rutinaService = rutinaService;
        this.nutricionService = nutricionService;
        this.progresoService = progresoService;
    }

    public AdminDtos.DetalleUsuario detalle(Long usuarioId) {
        String email = emailDe(usuarioId);
        AdminDtos.UsuarioAdmin usuario = adminService.usuarios().stream()
                .filter(u -> u.id().equals(usuarioId))
                .findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario."));

        return new AdminDtos.DetalleUsuario(
                usuario,
                perfilService.consultar(email),
                siExiste(() -> rutinaService.activa(email)),
                siExiste(() -> nutricionService.activo(email)),
                progresoService.consultar(email));
    }

    /** Genera una rutina y un plan nutricional nuevos para el usuario con su perfil actual. */
    public AdminDtos.DetalleUsuario generarPlan(Long usuarioId) {
        String email = emailDe(usuarioId);
        try {
            rutinaService.generar(email);
            nutricionService.generar(email);
        } catch (ApiException e) {
            if (e.getMessage() != null && e.getMessage().contains("Completa tu perfil")) {
                throw ApiException.solicitudInvalida(
                        "El usuario aún no ha completado su perfil, por eso no se le puede generar un plan.");
            }
            throw e;
        }
        return detalle(usuarioId);
    }

    private String emailDe(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario."));
        return usuario.getEmail();
    }

    private static <T> T siExiste(Supplier<T> consulta) {
        try {
            return consulta.get();
        } catch (ApiException e) {
            return null;
        }
    }
}
