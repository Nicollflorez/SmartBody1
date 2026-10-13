package com.smartbody.dto;

import java.util.List;

/** RF10 - historial de medidas (del mas antiguo al mas reciente) y resumen de la evolucion. */
public record ProgresoResponse(List<MedidaResponse> medidas,
                               Double pesoInicial,
                               Double pesoActual,
                               Double cambioPesoKg,
                               Double imcActual,
                               String clasificacionImc) {
}
