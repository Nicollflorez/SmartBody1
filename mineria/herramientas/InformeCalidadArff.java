import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Informe de calidad de datos sobre el ARFF de SmartBody (Fase 2 de CRISP-DM).
 * Equivale a la pestana Preprocess de Weka Explorer: tipos, faltantes, estadisticos,
 * valores extremos (regla IQR), duplicados y validacion de rangos y coherencia.
 *
 * Uso (desde mineria/):
 *   java -cp lib/weka-stable-3.8.6.jar herramientas/InformeCalidadArff.java datos/smartbody_dataset_100000.arff informe/informe_calidad_original.txt
 */
public class InformeCalidadArff {

    public static void main(String[] args) throws IOException {
        Path rutaArff = Path.of(args[0]);
        Path rutaSalida = Path.of(args[1]);

        Instances datos;
        try (BufferedReader r = Files.newBufferedReader(rutaArff)) {
            datos = new Instances(r);
        }
        datos.setClassIndex(datos.numAttributes() - 1);

        StringBuilder out = new StringBuilder();
        out.append("INFORME DE CALIDAD DE DATOS - ").append(rutaArff.getFileName()).append('\n');
        out.append("Instancias cargadas por Weka: ").append(datos.numInstances()).append('\n');
        out.append("Atributos: ").append(datos.numAttributes()).append(" (clase: ")
           .append(datos.classAttribute().name()).append(")\n\n");

        seccionAtributos(datos, out);
        seccionFaltantes(datos, out);
        seccionDuplicados(datos, out);
        seccionRangos(datos, out);

        Files.createDirectories(rutaSalida.toAbsolutePath().getParent());
        Files.writeString(rutaSalida, out.toString());
        System.out.print(out);
        System.out.println("\nInforme guardado en: " + rutaSalida.toAbsolutePath());
    }

    static void seccionAtributos(Instances d, StringBuilder out) {
        out.append("== 1. Atributos, tipos, faltantes, estadisticos y valores extremos (IQR) ==\n");
        out.append(String.format(Locale.US, "%-28s %-9s %7s %10s %10s %10s %10s %8s %8s %9s%n",
                "atributo", "tipo", "faltan", "min", "max", "media", "desv", "Q1", "Q3", "extremos"));
        for (int i = 0; i < d.numAttributes(); i++) {
            Attribute a = d.attribute(i);
            if (a.isNumeric()) {
                List<Double> valores = new ArrayList<>();
                int faltan = 0;
                for (Instance in : d) {
                    if (in.isMissing(i)) faltan++;
                    else valores.add(in.value(i));
                }
                Collections.sort(valores);
                double q1 = percentil(valores, 0.25);
                double q3 = percentil(valores, 0.75);
                double iqr = q3 - q1;
                long extremos = valores.stream().filter(v -> v < q1 - 1.5 * iqr || v > q3 + 1.5 * iqr).count();
                double[] est = estadisticos(valores);
                out.append(String.format(Locale.US, "%-28s %-9s %7d %10.2f %10.2f %10.2f %10.2f %8.2f %8.2f %9d%n",
                        a.name(), "NUMERIC", faltan, est[0], est[1], est[2], est[3], q1, q3, extremos));
            } else {
                Map<String, Integer> conteo = new LinkedHashMap<>();
                int faltan = 0;
                for (Instance in : d) {
                    if (in.isMissing(i)) {
                        faltan++;
                    } else {
                        conteo.merge(in.stringValue(i), 1, Integer::sum);
                    }
                }
                out.append(String.format(Locale.US, "%-28s %-9s %7d   categorias: %s%n",
                        a.name(), "NOMINAL", faltan, conteo));
            }
        }
        out.append("\nExtremos (IQR): valores fuera de [Q1 - 1,5*IQR ; Q3 + 1,5*IQR].\n\n");
    }

    static void seccionFaltantes(Instances d, StringBuilder out) {
        long filasConFaltantes = d.stream().filter(Instance::hasMissingValue).count();
        out.append("== 2. Valores faltantes ==\n");
        out.append("Filas con al menos un valor faltante: ").append(filasConFaltantes)
           .append(String.format(Locale.US, " (%.2f%%)%n%n", 100.0 * filasConFaltantes / d.numInstances()));
    }

    static void seccionDuplicados(Instances d, StringBuilder out) {
        Map<String, Integer> vistas = new HashMap<>();
        for (Instance in : d) {
            vistas.merge(in.toString(), 1, Integer::sum);
        }
        int distintas = vistas.size();
        int repetidas = d.numInstances() - distintas;
        out.append("== 3. Registros duplicados (contenido identico en todos los atributos) ==\n");
        out.append("Filas distintas: ").append(distintas).append('\n');
        out.append("Filas duplicadas (sobrantes): ").append(repetidas)
           .append(String.format(Locale.US, " (%.2f%%)%n%n", 100.0 * repetidas / d.numInstances()));
    }

    static void seccionRangos(Instances d, StringBuilder out) {
        Map<String, Integer> fallos = new LinkedHashMap<>();
        Attribute edad = d.attribute("edad");
        Attribute altura = d.attribute("altura_cm");
        Attribute pesoIni = d.attribute("peso_inicial_kg");
        Attribute pesoObj = d.attribute("peso_objetivo_kg");
        Attribute dif = d.attribute("diferencia_peso_kg");
        Attribute imc = d.attribute("imc_inicial");
        Attribute objetivo = d.attribute("objetivo");
        Attribute entrena = d.attribute("entrenamientos_semana");
        Attribute minutos = d.attribute("minutos_sesion");
        Attribute restr = d.attribute("restricciones_total");
        Attribute adhN = d.attribute("adherencia_nutricional_pct");
        Attribute adhE = d.attribute("adherencia_ejercicio_pct");
        Attribute semanas = d.attribute("semanas_estimadas");

        for (Instance in : d) {
            regla(fallos, "edad fuera de 12..100", v(in, edad), 12, 100);
            regla(fallos, "altura_cm fuera de 120..230", v(in, altura), 120, 230);
            regla(fallos, "peso_inicial_kg fuera de 30..250", v(in, pesoIni), 30, 250);
            regla(fallos, "peso_objetivo_kg fuera de 30..250", v(in, pesoObj), 30, 250);
            regla(fallos, "entrenamientos_semana fuera de 1..7", v(in, entrena), 1, 7);
            regla(fallos, "minutos_sesion fuera de 15..180", v(in, minutos), 15, 180);
            regla(fallos, "restricciones_total negativo", v(in, restr), 0, Double.MAX_VALUE);
            regla(fallos, "adherencia_nutricional_pct fuera de 0..100", v(in, adhN), 0, 100);
            regla(fallos, "adherencia_ejercicio_pct fuera de 0..100", v(in, adhE), 0, 100);
            regla(fallos, "semanas_estimadas <= 0 o > 104", v(in, semanas), 0.0001, 104);

            double a = v(in, altura), p = v(in, pesoIni), o = v(in, pesoObj);
            if (!Double.isNaN(a) && !Double.isNaN(p) && !Double.isNaN(v(in, imc))) {
                double imcCalc = p / Math.pow(a / 100.0, 2);
                if (Math.abs(imcCalc - v(in, imc)) > 0.05) {
                    fallos.merge("imc_inicial no coincide con peso/altura", 1, Integer::sum);
                }
            }
            if (!Double.isNaN(p) && !Double.isNaN(o) && !Double.isNaN(v(in, dif))) {
                if (Math.abs(Math.abs(p - o) - v(in, dif)) > 0.15) {
                    fallos.merge("diferencia_peso_kg no coincide con |inicial - objetivo|", 1, Integer::sum);
                }
            }
            if (!in.isMissing(objetivo) && !Double.isNaN(p) && !Double.isNaN(o)) {
                String obj = in.stringValue(objetivo);
                if (obj.equals("PERDER_PESO") && o >= p) {
                    fallos.merge("PERDER_PESO con peso objetivo >= peso inicial", 1, Integer::sum);
                }
                if (obj.equals("GANAR_MASA") && o <= p) {
                    fallos.merge("GANAR_MASA con peso objetivo <= peso inicial", 1, Integer::sum);
                }
            }
        }

        out.append("== 4. Validacion de rangos y coherencia (un registro puede fallar varias reglas) ==\n");
        if (fallos.isEmpty()) {
            out.append("Sin incumplimientos.\n");
        }
        fallos.forEach((regla, n) -> out.append(String.format(Locale.US, "%-62s %6d%n", regla, n)));
    }

    /** Cuenta un fallo si el valor existe y esta fuera del rango [min, max]. */
    static void regla(Map<String, Integer> fallos, String nombre, double valor, double min, double max) {
        if (Double.isNaN(valor)) return;
        if (valor < min || valor > max) {
            fallos.merge(nombre, 1, Integer::sum);
        }
    }

    static double v(Instance in, Attribute a) {
        return a == null || in.isMissing(a) ? Double.NaN : in.value(a);
    }

    static double percentil(List<Double> ordenados, double p) {
        if (ordenados.isEmpty()) return Double.NaN;
        double pos = p * (ordenados.size() - 1);
        int bajo = (int) Math.floor(pos);
        int alto = (int) Math.ceil(pos);
        return ordenados.get(bajo) + (pos - bajo) * (ordenados.get(alto) - ordenados.get(bajo));
    }

    /** min, max, media, desviacion estandar (poblacional). */
    static double[] estadisticos(List<Double> valores) {
        double min = valores.get(0), max = valores.get(valores.size() - 1);
        double media = valores.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
        double varianza = valores.stream().mapToDouble(x -> (x - media) * (x - media)).average().orElse(Double.NaN);
        return new double[]{min, max, media, Math.sqrt(varianza)};
    }
}
