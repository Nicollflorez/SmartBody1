package com.smartbody.dto;

import com.smartbody.entity.Ejercicio;
import com.smartbody.entity.NivelExperiencia;
import com.smartbody.entity.TipoEjercicio;

/** RF08 - informacion para ejecutar correctamente un ejercicio. */
public record EjercicioResponse(Long id,
                                String nombre,
                                String descripcion,
                                String grupoMuscular,
                                String equipamiento,
                                TipoEjercicio tipo,
                                NivelExperiencia nivelMinimo,
                                String instrucciones,
                                String imagenUrl) {

    public static EjercicioResponse desde(Ejercicio ejercicio) {
        return new EjercicioResponse(ejercicio.getId(), ejercicio.getNombre(), ejercicio.getDescripcion(),
                ejercicio.getGrupoMuscular(), ejercicio.getEquipamiento(), ejercicio.getTipo(),
                ejercicio.getNivelMinimo(), ejercicio.getInstrucciones(), ejercicio.getImagenUrl());
    }
}
