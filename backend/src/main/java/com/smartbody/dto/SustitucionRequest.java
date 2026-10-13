package com.smartbody.dto;

import jakarta.validation.constraints.NotNull;

/**
 * RF09 - sustitucion de un ejercicio de la rutina o de un alimento del plan.
 * Si {@code nuevoId} es null el sistema elige automaticamente la primera alternativa compatible.
 */
public record SustitucionRequest(

        @NotNull(message = "Indica el elemento que deseas cambiar")
        Long itemId,

        Long nuevoId) {
}
