package com.smartbody.dto;

/**
 * Datos de la pantalla de inicio en una sola peticion.
 * {@code sugerirCambioRutina} implementa el aviso de RF11 dentro de la aplicacion.
 */
public record ResumenResponse(String nombre,
                              String email,
                              boolean perfilCompleto,
                              String objetivo,
                              Double pesoKg,
                              Double imc,
                              String clasificacionImc,
                              Integer diasPorSemana,
                              Integer minutosPorSesion,
                              Long rutinaActivaId,
                              Long planActivoId,
                              Long diasDesdeRutina,
                              boolean sugerirCambioRutina,
                              boolean esAdministrador) {
}
