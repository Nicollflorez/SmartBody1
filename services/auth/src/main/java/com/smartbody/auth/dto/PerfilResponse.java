package com.smartbody.auth.dto;

import com.smartbody.auth.entity.NivelExperiencia;
import com.smartbody.auth.entity.Perfil;
import com.smartbody.auth.entity.Restriccion;
import com.smartbody.auth.entity.Sexo;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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

    public static PerfilResponse desde(Perfil perfil, Collection<Restriccion> restricciones) {
        List<RestriccionResponse> lista = restricciones.stream()
                .sorted(Comparator.comparing(Restriccion::getTipo).thenComparing(Restriccion::getNombre))
                .map(RestriccionResponse::desde)
                .toList();

        if (perfil == null) {
            return new PerfilResponse(null, null, null, null, null, null, null, null, lista, null, null, false);
        }
        Double imc = perfil.calcularImc();
        return new PerfilResponse(perfil.getEdad(), perfil.getSexo(), perfil.getPesoKg(), perfil.getAlturaCm(),
                perfil.getNivelExperiencia(), perfil.getMinutosPorSesion(), perfil.getDiasPorSemana(),
                ObjetivoResponse.desde(perfil.getObjetivo()), lista, imc, Perfil.clasificarImc(imc),
                perfil.estaCompleto());
    }
}
