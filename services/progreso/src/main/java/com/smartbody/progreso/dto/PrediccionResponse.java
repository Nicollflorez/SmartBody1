package com.smartbody.progreso.dto;

import com.smartbody.progreso.entity.Prediccion;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Resultado de una estimacion. Siempre se presenta como orientativo. */
public record PrediccionResponse(Long id,
                                 Double pesoInicial,
                                 Double pesoObjetivo,
                                 String objetivo,
                                 Double semanasEstimadas,
                                 LocalDate fechaAproximada,
                                 String modelo,
                                 String versionModelo,
                                 String tipoPrediccion,
                                 String aviso,
                                 LocalDateTime fechaPrediccion) {

    public static final String AVISO = "Estimación orientativa basada en datos sintéticos. "
            + "No garantiza una pérdida ni una ganancia de peso. Consulta a un profesional de nutrición o salud.";

    public static PrediccionResponse desde(Prediccion p) {
        long dias = Math.round(p.getSemanasEstimadas() * 7);
        return new PrediccionResponse(p.getId(), p.getPesoInicialKg(), p.getPesoObjetivoKg(), p.getObjetivo(),
                p.getSemanasEstimadas(), p.getFechaPrediccion().toLocalDate().plusDays(dias),
                p.getModelo(), p.getVersionModelo(), "orientativa", AVISO, p.getFechaPrediccion());
    }
}
