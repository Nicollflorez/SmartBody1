package com.smartbody.planes.dto;

import com.smartbody.planes.entity.Alimento;

/** Alimento del catalogo con sus valores por cada 100 g. */
public record AlimentoResponse(Long id,
                               String nombre,
                               String grupo,
                               Double caloriasPor100g,
                               Double proteinaG,
                               Double carbohidratosG,
                               Double grasasG,
                               String descripcion,
                               String icono) {

    public static AlimentoResponse desde(Alimento alimento) {
        return new AlimentoResponse(alimento.getId(), alimento.getNombre(), alimento.getGrupo(),
                alimento.getCaloriasPor100g(), alimento.getProteinaG(), alimento.getCarbohidratosG(),
                alimento.getGrasasG(), alimento.getDescripcion(), alimento.getIcono());
    }
}
