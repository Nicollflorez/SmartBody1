package com.smartbody.dto;

import com.smartbody.entity.EstadoSeguimiento;
import com.smartbody.entity.Prediccion;

import java.time.LocalDateTime;

/** Elemento del historial de predicciones del usuario. */
public record PrediccionResumenResponse(Long id,
                                        String objetivo,
                                        Double pesoInicial,
                                        Double pesoObjetivo,
                                        Double semanasEstimadas,
                                        EstadoSeguimiento estado,
                                        LocalDateTime fechaPrediccion) {

    public static PrediccionResumenResponse desde(Prediccion p) {
        return new PrediccionResumenResponse(p.getId(), p.getObjetivo(), p.getPesoInicialKg(), p.getPesoObjetivoKg(),
                p.getSemanasEstimadas(), p.getEstadoSeguimiento(), p.getFechaPrediccion());
    }
}
