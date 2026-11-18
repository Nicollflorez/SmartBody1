import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Preparacion de datos (Fase 3 de CRISP-DM).
 *
 * Pasos aplicados, en este orden:
 *   1. Eliminacion de duplicados (se conserva la primera aparicion).
 *   2. Eliminacion de filas con valores faltantes (3,5% del total, sin imputar).
 *   3. Validacion de rangos y coherencia: se eliminan filas que incumplen alguna regla.
 *   4. Outliers: NO se eliminan automaticamente. Se cuentan los que marca la regla IQR y
 *      se verifica que cumplan las reglas de validacion (son valores extremos plausibles).
 *
 * Uso (desde mineria/):
 *   java -cp lib/weka-stable-3.8.6.jar herramientas/LimpiezaDataset.java datos/smartbody_dataset_100000.arff datos/smartbody_limpio.arff informe/informe_limpieza.txt
 */
public class LimpiezaDataset {

    public static void main(String[] args) throws IOException {
        Path entrada = Path.of(args[0]);
        Path salidaArff = Path.of(args[1]);
        Path informe = Path.of(args[2]);

        Instances original;
        try (BufferedReader r = Files.newBufferedReader(entrada)) {
            original = new Instances(r);
        }
        original.setClassIndex(original.numAttributes() - 1);

        Instances limpio = new Instances(original, 0);
        Set<String> vistas = new HashSet<>();
        Map<String, Integer> descartes = new LinkedHashMap<>();
        descartes.put("duplicados", 0);
        descartes.put("faltantes", 0);
        descartes.put("invalidos (rangos o coherencia)", 0);

        int outliersIqr = 0;
        Map<String, double[]> limitesIqr = limitesIqr(original);

        for (Instance in : original) {
            if (!vistas.add(in.toString())) {
                descartes.merge("duplicados", 1, Integer::sum);
                continue;
            }
            if (in.hasMissingValue()) {
                descartes.merge("faltantes", 1, Integer::sum);
                continue;
            }
            if (!cumpleReglas(in, original)) {
                descartes.merge("invalidos (rangos o coherencia)", 1, Integer::sum);
                continue;
            }
            if (fueraDeIqr(in, original, limitesIqr)) {
                outliersIqr++;
            }
            limpio.add(in);
        }
        limpio.setClassIndex(limpio.numAttributes() - 1);

        // Instances.toString() genera el ARFF completo (cabecera y datos) sin depender de ArffSaver.
        Files.createDirectories(salidaArff.toAbsolutePath().getParent());
        Files.writeString(salidaArff, limpio.toString());

        StringBuilder out = new StringBuilder();
        out.append("INFORME DE LIMPIEZA - ").append(entrada.getFileName()).append(" -> ").append(salidaArff.getFileName()).append('\n');
        out.append(String.format(Locale.US, "Filas originales: %d%n", original.numInstances()));
        descartes.forEach((motivo, n) -> out.append(String.format(Locale.US, "  eliminadas por %-32s %6d (%.2f%%)%n",
                motivo, n, 100.0 * n / original.numInstances())));
        out.append(String.format(Locale.US, "Filas conservadas: %d (%.2f%%)%n", limpio.numInstances(),
                100.0 * limpio.numInstances() / original.numInstances()));
        out.append(String.format(Locale.US, "De ellas, con al menos un atributo fuera del rango IQR (conservadas tras validar): %d (%.2f%%)%n",
                outliersIqr, 100.0 * outliersIqr / limpio.numInstances()));
        out.append("\nLimites IQR usados (calculados sobre el dataset original):\n");
        limitesIqr.forEach((atributo, l) -> out.append(String.format(Locale.US, "  %-28s Q1=%8.2f  Q3=%8.2f  limite inferior=%9.2f  limite superior=%9.2f%n",
                atributo, l[0], l[1], l[2], l[3])));

        Files.createDirectories(informe.toAbsolutePath().getParent());
        Files.writeString(informe, out.toString());
        System.out.print(out);
        System.out.println("ARFF limpio: " + salidaArff.toAbsolutePath() + " (" + Files.size(salidaArff) + " bytes)");
    }

    /** Mismas reglas que InformeCalidadArff, seccion 4. Una fila es valida si no incumple ninguna. */
    static boolean cumpleReglas(Instance in, Instances d) {
        double edad = v(in, d, "edad");
        double altura = v(in, d, "altura_cm");
        double pesoIni = v(in, d, "peso_inicial_kg");
        double pesoObj = v(in, d, "peso_objetivo_kg");
        double dif = v(in, d, "diferencia_peso_kg");
        double imc = v(in, d, "imc_inicial");
        double entrena = v(in, d, "entrenamientos_semana");
        double minutos = v(in, d, "minutos_sesion");
        double restr = v(in, d, "restricciones_total");
        double adhN = v(in, d, "adherencia_nutricional_pct");
        double adhE = v(in, d, "adherencia_ejercicio_pct");
        double semanas = v(in, d, "semanas_estimadas");

        if (edad < 12 || edad > 100) return false;
        if (altura < 120 || altura > 230) return false;
        if (pesoIni < 30 || pesoIni > 250) return false;
        if (pesoObj < 30 || pesoObj > 250) return false;
        if (entrena < 1 || entrena > 7) return false;
        if (minutos < 15 || minutos > 180) return false;
        if (restr < 0) return false;
        if (adhN < 0 || adhN > 100) return false;
        if (adhE < 0 || adhE > 100) return false;
        if (semanas <= 0 || semanas > 104) return false;
        if (Math.abs(pesoIni / Math.pow(altura / 100.0, 2) - imc) > 0.05) return false;
        if (Math.abs(Math.abs(pesoIni - pesoObj) - dif) > 0.15) return false;
        String obj = in.stringValue(d.attribute("objetivo"));
        if (obj.equals("PERDER_PESO") && pesoObj >= pesoIni) return false;
        if (obj.equals("GANAR_MASA") && pesoObj <= pesoIni) return false;
        return true;
    }

    static Map<String, double[]> limitesIqr(Instances d) {
        Map<String, double[]> mapa = new LinkedHashMap<>();
        for (int i = 0; i < d.numAttributes(); i++) {
            Attribute a = d.attribute(i);
            if (!a.isNumeric()) continue;
            double[] valores = d.stream().filter(in -> !in.isMissing(a))
                    .mapToDouble(in -> in.value(a)).sorted().toArray();
            double q1 = percentil(valores, 0.25);
            double q3 = percentil(valores, 0.75);
            double iqr = q3 - q1;
            mapa.put(a.name(), new double[]{q1, q3, q1 - 1.5 * iqr, q3 + 1.5 * iqr});
        }
        return mapa;
    }

    static boolean fueraDeIqr(Instance in, Instances d, Map<String, double[]> limites) {
        for (Map.Entry<String, double[]> e : limites.entrySet()) {
            Attribute a = d.attribute(e.getKey());
            if (in.isMissing(a)) continue;
            double x = in.value(a);
            double[] l = e.getValue();
            if (x < l[2] || x > l[3]) return true;
        }
        return false;
    }

    static double v(Instance in, Instances d, String nombre) {
        Attribute a = d.attribute(nombre);
        return in.isMissing(a) ? Double.NaN : in.value(a);
    }

    static double percentil(double[] ordenados, double p) {
        double pos = p * (ordenados.length - 1);
        int bajo = (int) Math.floor(pos);
        int alto = (int) Math.ceil(pos);
        return ordenados[bajo] + (pos - bajo) * (ordenados[alto] - ordenados[bajo]);
    }
}
