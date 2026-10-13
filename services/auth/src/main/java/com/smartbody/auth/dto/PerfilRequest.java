package com.smartbody.auth.dto;

import com.smartbody.auth.entity.NivelExperiencia;
import com.smartbody.auth.entity.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** RF03, RF04 y RF05 - datos personales, fisicos, objetivo, disponibilidad y restricciones. */
public record PerfilRequest(

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 12, message = "La edad mínima es 12 años")
        @Max(value = 100, message = "La edad máxima es 100 años")
        Integer edad,

        @NotNull(message = "El sexo es obligatorio")
        Sexo sexo,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "25.0", message = "El peso debe ser mayor a 25 kg")
        @DecimalMax(value = "350.0", message = "El peso debe ser menor a 350 kg")
        Double pesoKg,

        @NotNull(message = "La altura es obligatoria")
        @DecimalMin(value = "100.0", message = "La altura debe ser mayor a 100 cm")
        @DecimalMax(value = "250.0", message = "La altura debe ser menor a 250 cm")
        Double alturaCm,

        @NotNull(message = "El nivel de experiencia es obligatorio")
        NivelExperiencia nivelExperiencia,

        @NotNull(message = "El tiempo por sesión es obligatorio")
        @Min(value = 15, message = "El tiempo mínimo por sesión es 15 minutos")
        @Max(value = 180, message = "El tiempo máximo por sesión es 180 minutos")
        Integer minutosPorSesion,

        @NotNull(message = "Los días por semana son obligatorios")
        @Min(value = 1, message = "Entrena al menos 1 día por semana")
        @Max(value = 7, message = "La semana tiene máximo 7 días")
        Integer diasPorSemana,

        @NotNull(message = "Selecciona un objetivo")
        Long objetivoId,

        List<Long> restriccionIds) {
}
