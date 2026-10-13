package com.smartbody.planes.service.reglas;

import com.smartbody.planes.entity.Sexo;

import java.util.Set;

/** Datos del usuario que se consideran para generar el plan nutricional y la hidratacion. */
public record SolicitudNutricion(String objetivo,
                                 Sexo sexo,
                                 int edad,
                                 double pesoKg,
                                 double alturaCm,
                                 int diasPorSemana,
                                 int minutosPorSesion,
                                 Set<Long> restriccionIds,
                                 Set<String> restriccionCodigos) {
}
