package com.smartbody.progreso.dto;

import com.smartbody.progreso.entity.TipoRestriccion;

/** Restriccion tal como la expone auth en el perfil. */
public record RestriccionResponse(Long id, String codigo, TipoRestriccion tipo, String nombre, String descripcion) {
}
