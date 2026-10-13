package com.smartbody.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/** Respuestas del panel de administracion. */
public final class AdminDtos {

    private AdminDtos() {
    }

    /** Cantidad asociada a un nombre (objetivo, restriccion...). */
    public record Conteo(String nombre, long cantidad) {
    }

    /** Evento del historial de uso: registro, rutina, plan nutricional o medida. */
    public record Actividad(String tipo, String descripcion, String usuario, String email, LocalDateTime fecha) {
    }

    public record Resumen(long totalUsuarios,
                          long usuariosActivos,
                          long usuariosNuevosUltimos30Dias,
                          long perfilesCompletos,
                          long rutinasGeneradas,
                          long planesGenerados,
                          long medidasRegistradas,
                          List<Conteo> usuariosPorObjetivo,
                          List<Conteo> restriccionesMasComunes,
                          List<Actividad> actividadReciente) {
    }

    public record UsuarioAdmin(Long id,
                               String nombre,
                               String email,
                               boolean activo,
                               boolean administrador,
                               LocalDateTime creadoEn,
                               boolean perfilCompleto,
                               String objetivo,
                               long rutinas,
                               long medidas) {
    }

    public record CambioEstado(@NotNull(message = "Indica si la cuenta queda activa") Boolean activo) {
    }

    /** Todo lo que el administrador necesita ver de un usuario. Rutina y plan pueden ser null si no existen. */
    public record DetalleUsuario(UsuarioAdmin usuario,
                                 PerfilResponse perfil,
                                 RutinaResponse rutina,
                                 PlanNutricionalResponse plan,
                                 ProgresoResponse progreso) {
    }

    /** Progreso de un usuario entre su primera y su ultima medida. */
    public record ProgresoUsuario(Long id,
                                  String nombre,
                                  String objetivo,
                                  double pesoInicial,
                                  double pesoActual,
                                  double cambioPesoKg,
                                  Double imcInicial,
                                  Double imcActual,
                                  boolean mejoro,
                                  long diasSeguimiento) {
    }

    public record EfectividadObjetivo(String objetivo,
                                      long usuarios,
                                      long mejoraron,
                                      double porcentajeMejora,
                                      double promedioCambioPesoKg) {
    }

    /** Reporte de efectividad: que tanto estan mejorando los usuarios segun su objetivo. */
    public record ReporteEfectividad(long usuariosConPerfil,
                                     long usuariosConSeguimiento,
                                     long usuariosQueMejoraron,
                                     double porcentajeMejora,
                                     double promedioDiasSeguimiento,
                                     List<EfectividadObjetivo> porObjetivo,
                                     List<ProgresoUsuario> usuarios) {
    }
}
