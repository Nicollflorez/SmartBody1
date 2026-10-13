package com.smartbody.planes.dto;

import com.smartbody.planes.entity.Ejercicio;
import com.smartbody.planes.entity.NivelExperiencia;
import com.smartbody.planes.entity.TipoEjercicio;

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
