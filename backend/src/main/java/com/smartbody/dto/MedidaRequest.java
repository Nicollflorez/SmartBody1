package com.smartbody.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** RF10 - nuevo registro de medidas. Solo el peso es obligatorio. */
public record MedidaRequest(

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "25.0", message = "El peso debe ser mayor a 25 kg")
        @DecimalMax(value = "350.0", message = "El peso debe ser menor a 350 kg")
        Double pesoKg,

        @DecimalMin(value = "100.0", message = "La altura debe ser mayor a 100 cm")
        @DecimalMax(value = "250.0", message = "La altura debe ser menor a 250 cm")
        Double alturaCm,

        @DecimalMin(value = "30.0", message = "La cintura debe ser mayor a 30 cm")
        @DecimalMax(value = "250.0", message = "La cintura debe ser menor a 250 cm")
        Double cinturaCm,

        @DecimalMin(value = "30.0", message = "La cadera debe ser mayor a 30 cm")
        @DecimalMax(value = "250.0", message = "La cadera debe ser menor a 250 cm")
        Double caderaCm,

        @DecimalMin(value = "30.0", message = "El pecho debe ser mayor a 30 cm")
        @DecimalMax(value = "250.0", message = "El pecho debe ser menor a 250 cm")
        Double pechoCm,

        @DecimalMin(value = "10.0", message = "El brazo debe ser mayor a 10 cm")
        @DecimalMax(value = "100.0", message = "El brazo debe ser menor a 100 cm")
        Double brazoCm) {
}
