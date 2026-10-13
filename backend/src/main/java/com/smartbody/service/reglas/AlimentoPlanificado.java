package com.smartbody.service.reglas;

import com.smartbody.entity.Alimento;
import com.smartbody.entity.MomentoComida;

/** Alimento elegido por el generador con su cantidad en gramos. */
public record AlimentoPlanificado(Alimento alimento, MomentoComida momento, double gramos) {
}
