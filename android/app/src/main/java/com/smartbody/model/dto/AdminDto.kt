package com.smartbody.model.dto

/** Panel de administracion. */

data class ConteoDto(
    val nombre: String,
    val cantidad: Long
)

/** Evento del historial de uso. [tipo]: REGISTRO, RUTINA, PLAN o MEDIDA. */
data class ActividadDto(
    val tipo: String,
    val descripcion: String,
    val usuario: String,
    val email: String,
    val fecha: String
)

data class ResumenAdminDto(
    val totalUsuarios: Long,
    val usuariosActivos: Long,
    val usuariosNuevosUltimos30Dias: Long,
    val perfilesCompletos: Long,
    val rutinasGeneradas: Long,
    val planesGenerados: Long,
    val medidasRegistradas: Long,
    val usuariosPorObjetivo: List<ConteoDto>,
    val restriccionesMasComunes: List<ConteoDto>,
    val actividadReciente: List<ActividadDto>
)

data class UsuarioAdminDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val activo: Boolean,
    val administrador: Boolean,
    val creadoEn: String?,
    val perfilCompleto: Boolean,
    val objetivo: String?,
    val rutinas: Long,
    val medidas: Long
)

data class CambioEstadoRequest(
    val activo: Boolean
)

/** Perfil, rutina, plan y progreso de un usuario vistos por el administrador. */
data class DetalleUsuarioAdminDto(
    val usuario: UsuarioAdminDto,
    val perfil: PerfilDto,
    val rutina: RutinaDto?,
    val plan: PlanNutricionalDto?,
    val progreso: ProgresoDto
)

data class ProgresoUsuarioDto(
    val id: Long,
    val nombre: String,
    val objetivo: String,
    val pesoInicial: Double,
    val pesoActual: Double,
    val cambioPesoKg: Double,
    val imcInicial: Double?,
    val imcActual: Double?,
    val mejoro: Boolean,
    val diasSeguimiento: Long
)

data class EfectividadObjetivoDto(
    val objetivo: String,
    val usuarios: Long,
    val mejoraron: Long,
    val porcentajeMejora: Double,
    val promedioCambioPesoKg: Double
)

data class ReporteEfectividadDto(
    val usuariosConPerfil: Long,
    val usuariosConSeguimiento: Long,
    val usuariosQueMejoraron: Long,
    val porcentajeMejora: Double,
    val promedioDiasSeguimiento: Double,
    val porObjetivo: List<EfectividadObjetivoDto>,
    val usuarios: List<ProgresoUsuarioDto>
)
