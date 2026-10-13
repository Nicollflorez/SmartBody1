package com.smartbody.auth.dto;

import com.smartbody.auth.entity.Objetivo;

public record ObjetivoResponse(Long id, String codigo, String nombre, String descripcion) {

    public static ObjetivoResponse desde(Objetivo objetivo) {
        if (objetivo == null) {
            return null;
        }
        return new ObjetivoResponse(objetivo.getId(), objetivo.getCodigo(), objetivo.getNombre(), objetivo.getDescripcion());
    }
}
