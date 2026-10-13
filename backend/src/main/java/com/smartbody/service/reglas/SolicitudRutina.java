package com.smartbody.service.reglas;

import com.smartbody.entity.NivelExperiencia;

import java.util.Set;

/** Datos del usuario que se consideran para generar una rutina (RF06). */
public record SolicitudRutina(String objetivo,
                              NivelExperiencia nivel,
                              int minutosPorSesion,
                              int diasPorSemana,
                              Set<Long> restriccionIds) {
}
