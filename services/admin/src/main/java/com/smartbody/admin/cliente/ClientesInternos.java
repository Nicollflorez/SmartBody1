package com.smartbody.admin.cliente;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartbody.comun.cliente.ClienteServicios;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acceso a las rutas internas de auth, planes y progreso. El panel compone sus respuestas a partir
 * de estos datos; la rutina, el plan y el perfil completos se reenvian tal cual (JsonNode).
 */
@Component
public class ClientesInternos {

    // ---- auth ----
    public record Objetivo(Long id, String codigo, String nombre) {
    }

    public record Perfil(Objetivo objetivo, boolean completo) {
    }

    public record Usuario(Long id, String nombre, String email, boolean activo, boolean esAdministrador,
                          LocalDateTime creadoEn, Perfil perfil) {
    }

    public record EstadisticasUsuarios(long totalUsuarios, long usuariosActivos, long nuevosUltimos30Dias,
                                       long perfilesCompletos, Map<String, Long> usuariosPorObjetivo,
                                       Map<String, Long> usuariosPorRestriccion) {
    }

    private record EstadoRequest(boolean activo) {
    }

    // ---- planes ----
    public record EstadisticasPlanes(long rutinasGeneradas, long planesGenerados, Map<Long, Long> rutinasPorUsuario) {
    }

    public record ActividadPlan(String tipo, Long usuarioId, String descripcion, LocalDateTime fecha) {
    }

    // ---- progreso ----
    public record EstadisticasProgreso(long medidasRegistradas, Map<Long, Long> medidasPorUsuario) {
    }

    public record ActividadMedida(Long usuarioId, Double pesoKg, LocalDateTime fecha) {
    }

    public record UsuarioObjetivo(Long usuarioId, String objetivoCodigo) {
    }

    public record Evaluacion(Long usuarioId, double pesoInicial, double pesoActual, double cambioPesoKg,
                             Double imcInicial, Double imcActual, boolean mejoro, long diasSeguimiento) {
    }

    private final ClienteServicios cliente;
    private final String auth;
    private final String planes;
    private final String progreso;

    public ClientesInternos(ClienteServicios cliente,
                            @Value("${smartbody.servicios.auth}") String auth,
                            @Value("${smartbody.servicios.planes}") String planes,
                            @Value("${smartbody.servicios.progreso}") String progreso) {
        this.cliente = cliente;
        this.auth = auth;
        this.planes = planes;
        this.progreso = progreso;
    }

    public List<Usuario> usuarios() {
        return lista(cliente.interno().get(auth + "/internal/usuarios", Usuario[].class));
    }

    public List<Usuario> usuariosRecientes() {
        return lista(cliente.interno().get(auth + "/internal/usuarios/recientes", Usuario[].class));
    }

    public EstadisticasUsuarios estadisticasUsuarios() {
        return cliente.interno().get(auth + "/internal/usuarios/estadisticas", EstadisticasUsuarios.class);
    }

    public Usuario usuario(Long id) {
        return cliente.interno().get(auth + "/internal/usuarios/" + id, Usuario.class);
    }

    /** Perfil completo del usuario, tal como lo devuelve auth, para reenviarlo al panel. */
    public JsonNode perfilCompleto(Long id) {
        JsonNode usuario = cliente.interno().get(auth + "/internal/usuarios/" + id, JsonNode.class);
        return usuario == null ? null : usuario.get("perfil");
    }

    public void cambiarEstado(Long id, boolean activo) {
        cliente.interno().put(auth + "/internal/usuarios/" + id + "/estado", new EstadoRequest(activo), JsonNode.class);
    }

    public EstadisticasPlanes estadisticasPlanes() {
        return cliente.interno().get(planes + "/internal/planes/estadisticas", EstadisticasPlanes.class);
    }

    public List<ActividadPlan> actividadPlanes() {
        return lista(cliente.interno().get(planes + "/internal/planes/recientes", ActividadPlan[].class));
    }

    public Optional<JsonNode> rutinaActiva(Long usuarioId) {
        return cliente.interno().getSiExiste(planes + "/internal/planes/usuarios/" + usuarioId + "/rutina-activa", JsonNode.class);
    }

    public Optional<JsonNode> planActivo(Long usuarioId) {
        return cliente.interno().getSiExiste(planes + "/internal/planes/usuarios/" + usuarioId + "/plan-activo", JsonNode.class);
    }

    public void generarPlanes(Long usuarioId) {
        cliente.interno().post(planes + "/internal/planes/usuarios/" + usuarioId + "/generar", null, Void.class);
    }

    public EstadisticasProgreso estadisticasProgreso() {
        return cliente.interno().get(progreso + "/internal/progreso/estadisticas", EstadisticasProgreso.class);
    }

    public List<ActividadMedida> actividadMedidas() {
        return lista(cliente.interno().get(progreso + "/internal/progreso/recientes", ActividadMedida[].class));
    }

    public JsonNode progresoDe(Long usuarioId) {
        return cliente.interno().get(progreso + "/internal/progreso/usuarios/" + usuarioId, JsonNode.class);
    }

    public List<Evaluacion> evaluar(List<UsuarioObjetivo> usuarios) {
        return lista(cliente.interno().post(progreso + "/internal/progreso/evaluaciones", usuarios, Evaluacion[].class));
    }

    private static <T> List<T> lista(T[] elementos) {
        return elementos == null ? List.of() : Arrays.asList(elementos);
    }
}
