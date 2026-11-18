package com.smartbody.progreso.dto;

import com.smartbody.progreso.entity.EstadoSeguimiento;
import com.smartbody.progreso.entity.Prediccion;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comparación entre la estimación y la evolución real del peso (medidas registradas después de predecir).
 * Los campos que dependen de medidas aparecen en null mientras no existan datos suficientes.
 */
public record SeguimientoPrediccionResponse(Long id,
                                            String objetivo,
                                            Double pesoInicial,
                                            Double pesoObjetivo,
                                            Double semanasEstimadas,
                                            Double pesoActual,
                                            Double semanasTranscurridas,
                                            Double avancePorcentaje,
                                            Double ritmoRealKgSemana,
                                            EstadoSeguimiento estado,
                                            Double semanasRealesHastaObjetivo,
                                            Double diferenciaSemanas,
                                            Double semanasRestantesEstimadas,
                                            LocalDateTime ultimaActualizacion,
                                            List<PuntoPeso> puntos,
                                            String aviso) {

    /** Punto de la gráfica de evolución del peso. */
    public record PuntoPeso(LocalDateTime fecha, Double pesoKg) {
    }

    public static SeguimientoPrediccionResponse sinMedidas(Prediccion p) {
        return new SeguimientoPrediccionResponse(p.getId(), p.getObjetivo(), p.getPesoInicialKg(), p.getPesoObjetivoKg(),
                p.getSemanasEstimadas(), null, null, null, null, EstadoSeguimiento.PENDIENTE, null, null, null,
                null, List.of(), PrediccionResponse.AVISO);
    }
}
