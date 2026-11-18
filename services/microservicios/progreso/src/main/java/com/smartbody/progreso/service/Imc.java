package com.smartbody.progreso.service;

/** Calculo y clasificacion del indice de masa corporal (misma regla que el perfil en auth). */
public final class Imc {

    private Imc() {
    }

    public static Double calcular(Double pesoKg, Double alturaCm) {
        if (pesoKg == null || alturaCm == null || alturaCm <= 0) {
            return null;
        }
        double alturaMetros = alturaCm / 100.0;
        return Math.round((pesoKg / (alturaMetros * alturaMetros)) * 100.0) / 100.0;
    }

    /** Rangos de referencia de la OMS para adultos. */
    public static String clasificar(Double imc) {
        if (imc == null) {
            return null;
        }
        if (imc < 18.5) {
            return "Bajo peso";
        }
        if (imc < 25) {
            return "Peso saludable";
        }
        if (imc < 30) {
            return "Sobrepeso";
        }
        return "Obesidad";
    }
}
