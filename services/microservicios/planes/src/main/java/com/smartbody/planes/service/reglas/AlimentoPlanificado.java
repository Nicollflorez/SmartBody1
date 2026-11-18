package com.smartbody.planes.service.reglas;

import com.smartbody.planes.entity.Alimento;
import com.smartbody.planes.entity.MomentoComida;

/** Alimento elegido por el generador con su cantidad en gramos. */
public record AlimentoPlanificado(Alimento alimento, MomentoComida momento, double gramos) {
}
