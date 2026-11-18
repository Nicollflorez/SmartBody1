package com.smartbody.presenter.prediccion

import com.smartbody.model.dto.PrediccionRequest
import com.smartbody.presenter.Validaciones

/** Datos que la app ya conoce del perfil y del plan nutricional del usuario. */
data class DatosBase(
    val pesoActualKg: Double?,
    val alturaCm: Double?,
    val edad: Int?,
    val sexo: String?,
    val objetivoCodigo: String?,
    val nivelExperiencia: String?,
    val minutosPorSesion: Int?,
    val diasPorSemana: Int?,
    val restricciones: Int,
    val caloriasObjetivo: Int?
)

/** Datos que escribe el usuario en la pantalla de prediccion. */
data class EntradaPrediccion(
    val pesoObjetivo: String = "",
    val nivelActividad: String = "MODERADO",
    val tipoActividad: String = "MIXTO",
    val adherenciaNutricional: String = "",
    val adherenciaEjercicio: String = ""
)

/** Resultado de validar la entrada: una peticion lista para enviar o los motivos para no enviarla. */
sealed interface ConstruccionPeticion {
    data class Lista(val peticion: PrediccionRequest) : ConstruccionPeticion
    data class Incompleta(
        val faltantes: List<String>,
        val errores: Map<String, String>
    ) : ConstruccionPeticion
}

/**
 * Arma la peticion de prediccion sin logica de presentacion. Valida lo que la app puede
 * comprobar antes de llamar al servidor; el backend repite las comprobaciones.
 */
object ConstructorPeticionPrediccion {

    val NIVELES_ACTIVIDAD = listOf("SEDENTARIO", "LIGERO", "MODERADO", "ACTIVO")
    val TIPOS_ACTIVIDAD = listOf("FUERZA", "CARDIO", "MIXTO")
    private val OBJETIVOS_SOPORTADOS = setOf("PERDER_PESO", "GANAR_MASA")

    fun construir(base: DatosBase, entrada: EntradaPrediccion): ConstruccionPeticion {
        val faltantes = mutableListOf<String>()
        if (base.pesoActualKg == null) faltantes += "Registra tu peso en Progreso."
        if (base.alturaCm == null) faltantes += "Completa la altura en tu perfil."
        if (base.edad == null) faltantes += "Completa la edad en tu perfil."
        if (base.sexo != "MASCULINO" && base.sexo != "FEMENINO") {
            faltantes += "Tu perfil necesita sexo masculino o femenino para esta estimación."
        }
        if (base.objetivoCodigo !in OBJETIVOS_SOPORTADOS) {
            faltantes += "Tu objetivo debe ser perder peso o ganar masa para esta estimación."
        }
        if (base.nivelExperiencia == null) faltantes += "Completa tu nivel de experiencia en tu perfil."
        if (base.minutosPorSesion == null || base.diasPorSemana == null) {
            faltantes += "Completa la disponibilidad (días y minutos) en tu perfil."
        }
        if (base.caloriasObjetivo == null) faltantes += "Genera tu plan nutricional para conocer tus calorías objetivo."

        val errores = mutableMapOf<String, String>()
        val pesoObjetivo = Validaciones.leerDecimal(entrada.pesoObjetivo)
        if (pesoObjetivo == null) {
            errores["pesoObjetivo"] = "Escribe tu peso objetivo en kilos."
        }
        val adhNutricional = leerPorcentaje(entrada.adherenciaNutricional)
        if (adhNutricional == null) errores["adherenciaNutricional"] = "Escribe un porcentaje entre 0 y 100."
        val adhEjercicio = leerPorcentaje(entrada.adherenciaEjercicio)
        if (adhEjercicio == null) errores["adherenciaEjercicio"] = "Escribe un porcentaje entre 0 y 100."

        if (faltantes.isNotEmpty() || errores.isNotEmpty()) {
            return ConstruccionPeticion.Incompleta(faltantes, errores)
        }

        return ConstruccionPeticion.Lista(
            PrediccionRequest(
                pesoInicial = base.pesoActualKg!!,
                pesoObjetivo = pesoObjetivo!!,
                alturaCm = base.alturaCm!!,
                edad = base.edad!!,
                sexo = base.sexo!!,
                objetivo = base.objetivoCodigo!!,
                nivelActividad = entrada.nivelActividad,
                nivelExperiencia = base.nivelExperiencia!!,
                tipoActividad = entrada.tipoActividad,
                entrenamientosSemana = base.diasPorSemana!!,
                minutosSesion = base.minutosPorSesion!!,
                // El modelo se entreno con 0 a 3 restricciones; un valor mayor se trata como 3.
                restriccionesTotal = base.restricciones.coerceIn(0, 3),
                caloriasObjetivo = base.caloriasObjetivo!!.toDouble(),
                adherenciaNutricional = adhNutricional!!,
                adherenciaEjercicio = adhEjercicio!!
            )
        )
    }

    private fun leerPorcentaje(texto: String): Double? {
        val valor = Validaciones.leerDecimal(texto) ?: return null
        return valor.takeIf { it in 0.0..100.0 }
    }
}
