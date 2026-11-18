package com.smartbody.service.prediccion;

import com.smartbody.exception.ApiException;
import com.smartbody.dto.PrediccionRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Validacion de coherencia y calculo de las variables derivadas, con las mismas formulas que
 * usa mineria/GeneradorDataset.java. Asi el modelo recibe exactamente lo que vio en el entrenamiento.
 *
 * Reglas de seguridad: no se recomiendan deficits extremos. Las calorias objetivo no pueden quedar
 * por debajo del minimo seguro, y los valores fuera del rango de entrenamiento se rechazan.
 */
public final class ReglasPrediccion {

    /** Derivados que el modelo necesita y que el usuario no envia directamente. */
    public record Derivados(double diferencia, double imc, int consumoKcal, int balanceKcal) {
    }

    static final double IMC_MINIMO_OBJETIVO = 18.5;
    static final double IMC_MINIMO_ENTRENAMIENTO = 17.0;
    static final double IMC_MAXIMO_ENTRENAMIENTO = 55.0;
    static final double BALANCE_MAXIMO_KCAL = 600.0;
    static final double DIFERENCIA_MAXIMA_PERDER_KG = 50.0;
    static final double DIFERENCIA_MAXIMA_GANAR_KG = 20.0;

    private ReglasPrediccion() {
    }

    public static Derivados validarYDerivar(PrediccionRequest r) {
        boolean perder = "PERDER_PESO".equals(r.objetivo());
        boolean masculino = "MASCULINO".equals(r.sexo());
        double alturaM = r.alturaCm() / 100.0;

        if (perder && r.pesoObjetivo() >= r.pesoInicial()) {
            throw ApiException.solicitudInvalida("Para perder peso, el peso objetivo debe ser menor que el peso inicial.");
        }
        if (!perder && r.pesoObjetivo() <= r.pesoInicial()) {
            throw ApiException.solicitudInvalida("Para ganar masa, el peso objetivo debe ser mayor que el peso inicial.");
        }

        double diferencia = redondear1(Math.abs(r.pesoInicial() - r.pesoObjetivo()));
        double maxDiferencia = perder ? DIFERENCIA_MAXIMA_PERDER_KG : DIFERENCIA_MAXIMA_GANAR_KG;
        if (diferencia < 1 || diferencia > maxDiferencia) {
            throw ApiException.solicitudInvalida("La diferencia de peso debe estar entre 1 y "
                    + (int) maxDiferencia + " kg para este objetivo.");
        }

        double imc = redondear2(r.pesoInicial() / (alturaM * alturaM));
        if (imc < IMC_MINIMO_ENTRENAMIENTO || imc > IMC_MAXIMO_ENTRENAMIENTO) {
            throw ApiException.solicitudInvalida("El IMC inicial (" + imc + ") queda fuera del rango que el modelo "
                    + "puede estimar (" + (int) IMC_MINIMO_ENTRENAMIENTO + " a " + (int) IMC_MAXIMO_ENTRENAMIENTO + ").");
        }

        if (perder && r.pesoObjetivo() / (alturaM * alturaM) < IMC_MINIMO_OBJETIVO) {
            throw ApiException.solicitudInvalida("El peso objetivo deja un IMC menor a 18,5. "
                    + "Consulta con un profesional de salud antes de fijar esa meta.");
        }

        int consumo = (int) Math.round(gastoEnergeticoDiario(r, masculino));
        int balance;
        if (perder) {
            int minimoSeguro = masculino ? 1500 : 1200;
            if (r.caloriasObjetivo() < minimoSeguro) {
                throw ApiException.solicitudInvalida("Las calorías objetivo no pueden ser menores a "
                        + minimoSeguro + " kcal al día.");
            }
            balance = (int) Math.round(consumo - r.caloriasObjetivo());
            if (balance < 0) {
                throw ApiException.solicitudInvalida("Para perder peso, las calorías objetivo deben ser menores "
                        + "al gasto estimado (" + consumo + " kcal).");
            }
        } else {
            balance = (int) Math.round(r.caloriasObjetivo() - consumo);
            if (balance < 0) {
                throw ApiException.solicitudInvalida("Para ganar masa, las calorías objetivo deben ser mayores "
                        + "al gasto estimado (" + consumo + " kcal).");
            }
        }
        if (balance > BALANCE_MAXIMO_KCAL) {
            throw ApiException.solicitudInvalida("La diferencia diaria con el gasto (" + balance + " kcal) supera "
                    + (int) BALANCE_MAXIMO_KCAL + " kcal y está fuera del rango del modelo. No se recomiendan cambios tan bruscos.");
        }

        return new Derivados(diferencia, imc, consumo, balance);
    }

    /** Valores por nombre de atributo ARFF (mismos nombres y tipos que el entrenamiento). */
    public static Map<String, Object> aAtributos(PrediccionRequest r, Derivados d) {
        Map<String, Object> v = new HashMap<>();
        v.put("edad", r.edad().doubleValue());
        v.put("sexo", r.sexo());
        v.put("altura_cm", r.alturaCm());
        v.put("peso_inicial_kg", r.pesoInicial());
        v.put("peso_objetivo_kg", r.pesoObjetivo());
        v.put("diferencia_peso_kg", d.diferencia());
        v.put("imc_inicial", d.imc());
        v.put("objetivo", r.objetivo());
        v.put("nivel_actividad", r.nivelActividad());
        v.put("nivel_experiencia", r.nivelExperiencia());
        v.put("entrenamientos_semana", r.entrenamientosSemana().doubleValue());
        v.put("minutos_sesion", r.minutosSesion().doubleValue());
        v.put("tipo_actividad", r.tipoActividad());
        v.put("consumo_energetico_kcal", (double) d.consumoKcal());
        v.put("balance_energetico_kcal", (double) d.balanceKcal());
        v.put("restricciones_total", r.restriccionesTotal().doubleValue());
        v.put("adherencia_nutricional_pct", r.adherenciaNutricional());
        v.put("adherencia_ejercicio_pct", r.adherenciaEjercicio());
        return v;
    }

    /** Mifflin-St Jeor multiplicado por el factor de actividad (igual que el generador). */
    static double gastoEnergeticoDiario(PrediccionRequest r, boolean masculino) {
        double tmb = 10 * r.pesoInicial() + 6.25 * r.alturaCm() - 5 * r.edad() + (masculino ? 5 : -161);
        return tmb * factorActividad(r.nivelActividad());
    }

    static double factorActividad(String nivel) {
        return switch (nivel) {
            case "SEDENTARIO" -> 1.2;
            case "LIGERO" -> 1.375;
            case "MODERADO" -> 1.55;
            default -> 1.725;
        };
    }

    static double redondear1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    static double redondear2(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
