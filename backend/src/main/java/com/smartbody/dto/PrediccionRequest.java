package com.smartbody.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para estimar cuántas semanas podría tardar el usuario en alcanzar su peso objetivo.
 * Los rangos son los del dataset con el que se entreno el modelo (mineria/README.md).
 * El resultado es orientativo: no es una garantía de pérdida ni ganancia de peso.
 */
public record PrediccionRequest(

        @NotNull(message = "El peso inicial es obligatorio")
        @DecimalMin(value = "40.0", message = "El peso inicial debe estar entre 40 y 160 kg")
        @DecimalMax(value = "160.0", message = "El peso inicial debe estar entre 40 y 160 kg")
        Double pesoInicial,

        @NotNull(message = "El peso objetivo es obligatorio")
        @DecimalMin(value = "40.0", message = "El peso objetivo debe estar entre 40 y 160 kg")
        @DecimalMax(value = "160.0", message = "El peso objetivo debe estar entre 40 y 160 kg")
        Double pesoObjetivo,

        @NotNull(message = "La altura es obligatoria")
        @DecimalMin(value = "145.0", message = "La altura debe estar entre 145 y 200 cm")
        @DecimalMax(value = "200.0", message = "La altura debe estar entre 145 y 200 cm")
        Double alturaCm,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 18, message = "La edad debe estar entre 18 y 80 años")
        @Max(value = 80, message = "La edad debe estar entre 18 y 80 años")
        Integer edad,

        @NotNull(message = "El sexo es obligatorio")
        @Pattern(regexp = "MASCULINO|FEMENINO", message = "El sexo debe ser MASCULINO o FEMENINO")
        String sexo,

        @NotNull(message = "El objetivo es obligatorio")
        @Pattern(regexp = "PERDER_PESO|GANAR_MASA", message = "El objetivo debe ser PERDER_PESO o GANAR_MASA")
        String objetivo,

        @NotNull(message = "El nivel de actividad es obligatorio")
        @Pattern(regexp = "SEDENTARIO|LIGERO|MODERADO|ACTIVO", message = "Nivel de actividad no válido")
        String nivelActividad,

        @NotNull(message = "El nivel de experiencia es obligatorio")
        @Pattern(regexp = "PRINCIPIANTE|INTERMEDIO|AVANZADO", message = "Nivel de experiencia no válido")
        String nivelExperiencia,

        @NotNull(message = "El tipo de actividad es obligatorio")
        @Pattern(regexp = "FUERZA|CARDIO|MIXTO", message = "Tipo de actividad no válido")
        String tipoActividad,

        @NotNull(message = "Las sesiones por semana son obligatorias")
        @Min(value = 1, message = "Las sesiones por semana deben estar entre 1 y 7")
        @Max(value = 7, message = "Las sesiones por semana deben estar entre 1 y 7")
        Integer entrenamientosSemana,

        @NotNull(message = "La duración de la sesión es obligatoria")
        @Min(value = 15, message = "La duración de la sesion debe estar entre 15 y 180 minutos")
        @Max(value = 180, message = "La duración de la sesion debe estar entre 15 y 180 minutos")
        Integer minutosSesion,

        @NotNull(message = "El número de restricciones es obligatorio")
        @Min(value = 0, message = "Las restricciones deben estar entre 0 y 3")
        @Max(value = 3, message = "Las restricciones deben estar entre 0 y 3")
        Integer restriccionesTotal,

        @NotNull(message = "Las calorías objetivo del plan son obligatorias")
        @DecimalMin(value = "800.0", message = "Las calorías objetivo deben estar entre 800 y 5000 kcal")
        @DecimalMax(value = "5000.0", message = "Las calorías objetivo deben estar entre 800 y 5000 kcal")
        Double caloriasObjetivo,

        @NotNull(message = "La adherencia nutricional es obligatoria")
        @DecimalMin(value = "0.0", message = "La adherencia debe estar entre 0 y 100 %")
        @DecimalMax(value = "100.0", message = "La adherencia debe estar entre 0 y 100 %")
        Double adherenciaNutricional,

        @NotNull(message = "La adherencia al ejercicio es obligatoria")
        @DecimalMin(value = "0.0", message = "La adherencia debe estar entre 0 y 100 %")
        @DecimalMax(value = "100.0", message = "La adherencia debe estar entre 0 y 100 %")
        Double adherenciaEjercicio) {
}
