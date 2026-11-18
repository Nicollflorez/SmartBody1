package com.smartbody.progreso.dto;

import com.smartbody.progreso.entity.NivelExperiencia;
import com.smartbody.progreso.entity.Sexo;

import java.util.List;

/** Perfil del usuario tal como lo expone el servicio auth (GET /api/users/profile). */
public record PerfilResponse(Integer edad,
                             Sexo sexo,
                             Double pesoKg,
                             Double alturaCm,
                             NivelExperiencia nivelExperiencia,
                             Integer minutosPorSesion,
                             Integer diasPorSemana,
                             ObjetivoResponse objetivo,
                             List<RestriccionResponse> restricciones,
                             Double imc,
                             String clasificacionImc,
                             boolean completo) {
}
