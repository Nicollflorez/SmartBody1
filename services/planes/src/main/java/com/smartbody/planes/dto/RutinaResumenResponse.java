package com.smartbody.planes.dto;

import com.smartbody.planes.entity.Rutina;

import java.time.LocalDateTime;

/** Elemento del historial de rutinas del usuario. */
public record RutinaResumenResponse(Long id, String nombre, String objetivo, boolean activa, LocalDateTime generadaEn) {

    public static RutinaResumenResponse desde(Rutina rutina) {
        return new RutinaResumenResponse(rutina.getId(), rutina.getNombre(), rutina.getObjetivo().getNombre(),
                rutina.isActiva(), rutina.getGeneradaEn());
    }
}
