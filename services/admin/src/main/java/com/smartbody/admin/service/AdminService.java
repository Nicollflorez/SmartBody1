package com.smartbody.admin.service;

import com.smartbody.admin.cliente.ClientesInternos;
import com.smartbody.admin.cliente.ClientesInternos.Usuario;
import com.smartbody.admin.dto.AdminDtos;
import com.smartbody.comun.error.ApiException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Panel de administracion: compone el resumen de uso, el historial de actividad y la gestion de
 * cuentas a partir de los datos de auth, planes y progreso.
 */
@Service
public class AdminService {

    static final int LIMITE_ACTIVIDAD = 20;
    static final int LIMITE_RESTRICCIONES = 5;

    private final ClientesInternos clientes;

    public AdminService(ClientesInternos clientes) {
        this.clientes = clientes;
    }

    public AdminDtos.Resumen resumen() {
        var usuarios = clientes.estadisticasUsuarios();
        var planes = clientes.estadisticasPlanes();
        var progreso = clientes.estadisticasProgreso();
        return new AdminDtos.Resumen(
                usuarios.totalUsuarios(),
                usuarios.usuariosActivos(),
                usuarios.nuevosUltimos30Dias(),
                usuarios.perfilesCompletos(),
                planes.rutinasGeneradas(),
                planes.planesGenerados(),
                progreso.medidasRegistradas(),
                conteos(usuarios.usuariosPorObjetivo()),
                conteos(usuarios.usuariosPorRestriccion()).stream().limit(LIMITE_RESTRICCIONES).toList(),
                actividadReciente());
    }

    /** Historial de uso: los eventos mas recientes de todos los usuarios, del mas nuevo al mas antiguo. */
    public List<AdminDtos.Actividad> actividadReciente() {
        Map<Long, Usuario> porId = clientes.usuarios().stream()
                .collect(Collectors.toMap(Usuario::id, Function.identity(), (a, b) -> a));
        Stream<AdminDtos.Actividad> registros = clientes.usuariosRecientes().stream()
                .map(u -> new AdminDtos.Actividad("REGISTRO", "Creó su cuenta", u.nombre(), u.email(), u.creadoEn()));
        Stream<AdminDtos.Actividad> planes = clientes.actividadPlanes().stream()
                .map(a -> new AdminDtos.Actividad(a.tipo(), a.descripcion(),
                        nombre(porId, a.usuarioId()), email(porId, a.usuarioId()), a.fecha()));
        Stream<AdminDtos.Actividad> medidas = clientes.actividadMedidas().stream()
                .map(m -> new AdminDtos.Actividad("MEDIDA", "Registró un peso de " + formatoPeso(m.pesoKg()) + " kg",
                        nombre(porId, m.usuarioId()), email(porId, m.usuarioId()), m.fecha()));
        return Stream.of(registros, planes, medidas)
                .flatMap(s -> s)
                .sorted(Comparator.comparing(AdminDtos.Actividad::fecha).reversed())
                .limit(LIMITE_ACTIVIDAD)
                .toList();
    }

    public List<AdminDtos.UsuarioAdmin> usuarios() {
        Map<Long, Long> rutinasPorUsuario = clientes.estadisticasPlanes().rutinasPorUsuario();
        Map<Long, Long> medidasPorUsuario = clientes.estadisticasProgreso().medidasPorUsuario();
        return clientes.usuarios().stream()
                .map(u -> aUsuarioAdmin(u, rutinasPorUsuario.getOrDefault(u.id(), 0L), medidasPorUsuario.getOrDefault(u.id(), 0L)))
                .toList();
    }

    public AdminDtos.UsuarioAdmin usuario(Long id) {
        return usuarios().stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario."));
    }

    /** Activa o desactiva una cuenta. Una cuenta desactivada no puede iniciar sesion. */
    public void cambiarEstado(Long usuarioId, boolean activo) {
        clientes.cambiarEstado(usuarioId, activo);
    }

    public AdminDtos.DetalleUsuario detalle(Long usuarioId) {
        return new AdminDtos.DetalleUsuario(
                usuario(usuarioId),
                clientes.perfilCompleto(usuarioId),
                clientes.rutinaActiva(usuarioId).orElse(null),
                clientes.planActivo(usuarioId).orElse(null),
                clientes.progresoDe(usuarioId));
    }

    /** Genera una rutina y un plan nutricional nuevos para el usuario con su perfil actual. */
    public AdminDtos.DetalleUsuario generarPlan(Long usuarioId) {
        try {
            clientes.generarPlanes(usuarioId);
        } catch (ApiException e) {
            if (e.getMessage() != null && e.getMessage().contains("Completa tu perfil")) {
                throw ApiException.solicitudInvalida(
                        "El usuario aún no ha completado su perfil, por eso no se le puede generar un plan.");
            }
            throw e;
        }
        return detalle(usuarioId);
    }

    static AdminDtos.UsuarioAdmin aUsuarioAdmin(Usuario u, long rutinas, long medidas) {
        boolean perfilCompleto = u.perfil() != null && u.perfil().completo();
        String objetivo = u.perfil() == null || u.perfil().objetivo() == null ? null : u.perfil().objetivo().nombre();
        return new AdminDtos.UsuarioAdmin(u.id(), u.nombre(), u.email(), u.activo(), u.esAdministrador(), u.creadoEn(),
                perfilCompleto, objetivo, rutinas, medidas);
    }

    private static List<AdminDtos.Conteo> conteos(Map<String, Long> mapa) {
        return mapa == null ? List.of()
                : mapa.entrySet().stream().map(e -> new AdminDtos.Conteo(e.getKey(), e.getValue())).toList();
    }

    private static String nombre(Map<Long, Usuario> porId, Long usuarioId) {
        Usuario u = porId.get(usuarioId);
        return u == null ? "Usuario " + usuarioId : u.nombre();
    }

    private static String email(Map<Long, Usuario> porId, Long usuarioId) {
        Usuario u = porId.get(usuarioId);
        return u == null ? null : u.email();
    }

    private static String formatoPeso(Double peso) {
        if (peso == null) {
            return "-";
        }
        return peso % 1 == 0 ? String.valueOf(peso.intValue()) : String.valueOf(peso).replace('.', ',');
    }
}
