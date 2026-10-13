package com.smartbody.auth.dto;

import com.smartbody.auth.entity.Restriccion;
import com.smartbody.auth.entity.TipoRestriccion;

public record RestriccionResponse(Long id, String codigo, TipoRestriccion tipo, String nombre, String descripcion) {

    public static RestriccionResponse desde(Restriccion restriccion) {
        return new RestriccionResponse(restriccion.getId(), restriccion.getCodigo(), restriccion.getTipo(),
                restriccion.getNombre(), restriccion.getDescripcion());
    }
}
