package com.smartbody.planes.cliente;

import com.smartbody.planes.entity.NivelExperiencia;
import com.smartbody.planes.entity.Sexo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Perfil del usuario tal como lo expone el servicio auth (GET /api/users/profile). */
public record PerfilRemoto(Integer edad,
                           Sexo sexo,
                           Double pesoKg,
                           Double alturaCm,
                           NivelExperiencia nivelExperiencia,
                           Integer minutosPorSesion,
                           Integer diasPorSemana,
                           ObjetivoRemoto objetivo,
                           List<RestriccionRemota> restricciones,
                           boolean completo) {

    public record ObjetivoRemoto(Long id, String codigo, String nombre) {
    }

    public record RestriccionRemota(Long id, String codigo) {
    }

    public Set<Long> idsRestricciones() {
        return restricciones == null ? Set.of()
                : restricciones.stream().map(RestriccionRemota::id).collect(Collectors.toSet());
    }

    public Set<String> codigosRestricciones() {
        return restricciones == null ? Set.of()
                : restricciones.stream().map(RestriccionRemota::codigo).collect(Collectors.toSet());
    }
}
