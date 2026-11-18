package com.smartbody.progreso.dto;

import com.smartbody.progreso.entity.TipoEjercicio;

import java.time.LocalDateTime;
import java.util.List;

/** Rutina tal como la expone el servicio planes (GET /api/routines/active). */
public record RutinaResponse(Long id,
                             String nombre,
                             ObjetivoResponse objetivo,
                             int diasPorSemana,
                             int minutosPorSesion,
                             boolean activa,
                             LocalDateTime generadaEn,
                             List<Dia> dias) {

    public record Dia(int numero, String enfoque, int minutosEstimados, List<Item> ejercicios) {
    }

    public record Item(Long id,
                       Long ejercicioId,
                       String nombre,
                       String grupoMuscular,
                       TipoEjercicio tipo,
                       int series,
                       Integer repeticiones,
                       Integer duracionSeg,
                       int descansoSeg) {
    }
}
