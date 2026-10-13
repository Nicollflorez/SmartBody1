package com.smartbody.service.reglas;

import com.smartbody.entity.Ejercicio;
import com.smartbody.entity.RutinaEjercicio;

/** Ejercicio elegido por el generador, con su prescripcion, antes de guardarse en la base de datos. */
public record EjercicioPlanificado(Ejercicio ejercicio,
                                   int dia,
                                   int orden,
                                   int series,
                                   Integer repeticiones,
                                   Integer duracionSeg,
                                   int descansoSeg) {

    public int segundosEstimados() {
        return RutinaEjercicio.estimarSegundos(series, duracionSeg, descansoSeg);
    }
}
