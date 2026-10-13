package com.smartbody.dto;

import com.smartbody.entity.Restriccion;
import com.smartbody.entity.TipoRestriccion;

public record RestriccionResponse(Long id, String codigo, TipoRestriccion tipo, String nombre, String descripcion) {

    public static RestriccionResponse desde(Restriccion restriccion) {
        return new RestriccionResponse(restriccion.getId(), restriccion.getCodigo(), restriccion.getTipo(),
                restriccion.getNombre(), restriccion.getDescripcion());
    }
}
