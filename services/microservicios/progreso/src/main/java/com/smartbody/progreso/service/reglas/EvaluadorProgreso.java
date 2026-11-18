package com.smartbody.progreso.service.reglas;

/**
 * Decide si un usuario mejoro segun su propio objetivo, comparando su primera y su ultima medida.
 * Se usa en el reporte de efectividad del administrador.
 */
public final class EvaluadorProgreso {

    /** Variacion de peso que se considera "mantener" el peso. */
    static final double TOLERANCIA_MANTENER_KG = 1.0;
    static final double IMC_MINIMO_SALUDABLE = 18.5;
    static final double IMC_MAXIMO_SALUDABLE = 24.9;

    private EvaluadorProgreso() {
    }

    public static boolean mejoro(String objetivo, double pesoInicial, double pesoActual, Double imcInicial, Double imcActual) {
        double cambio = pesoActual - pesoInicial;
        return switch (objetivo == null ? "" : objetivo) {
            case "PERDER_PESO" -> cambio < 0;
            case "GANAR_MASA" -> cambio > 0;
            case "MANTENER_PESO" -> Math.abs(cambio) <= TOLERANCIA_MANTENER_KG;
            // Condicion fisica y resistencia no se miden por el peso: se valora que el IMC se acerque
            // al rango saludable o permanezca dentro de el.
            default -> {
                if (imcInicial == null || imcActual == null) {
                    yield false;
                }
                double distanciaActual = distanciaAlRangoSaludable(imcActual);
                yield distanciaActual == 0 || distanciaActual < distanciaAlRangoSaludable(imcInicial);
            }
        };
    }

    static double distanciaAlRangoSaludable(double imc) {
        if (imc < IMC_MINIMO_SALUDABLE) {
            return IMC_MINIMO_SALUDABLE - imc;
        }
        if (imc > IMC_MAXIMO_SALUDABLE) {
            return imc - IMC_MAXIMO_SALUDABLE;
        }
        return 0;
    }
}
