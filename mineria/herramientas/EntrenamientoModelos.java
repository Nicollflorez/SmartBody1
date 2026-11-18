import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializationHelper;
import weka.core.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Fase 4 (modelado) y Fase 5 (evaluacion) de CRISP-DM.
 *
 * Compara LinearRegression, REPTree y RandomForest con validacion cruzada de 10 particiones.
 * Todos usan los mismos datos, el mismo orden aleatorio (semilla 42) y las mismas particiones.
 * Al final entrena cada algoritmo con todo el conjunto limpio y guarda el modelo serializado
 * junto con su cabecera (estructura de atributos) para poder cargarlo despues sin reentrenar.
 *
 * Uso (desde mineria/):
 *   java -Xmx2g -cp lib/weka-stable-3.8.6.jar herramientas/EntrenamientoModelos.java datos/smartbody_limpio.arff modelos informe
 */
public class EntrenamientoModelos {

    static final int SEMILLA = 42;
    static final int PARTICIONES = 10;

    record Resultado(String nombre, String configuracion, double mae, double rmse, double correlacion,
                     double r2, double maeDesviacion, double p50, double p90, double segundos,
                     double[] maePorRango, int[] nPorRango, double[] reales, double[] predichas) {}

    static final String[] RANGOS = {"0-12", "12-24", "24-48", "48-104"};
    static final double[] LIMITES = {12, 24, 48, 104.0001};

    public static void main(String[] args) throws Exception {
        Path arff = Path.of(args[0]);
        Path dirModelos = Path.of(args[1]);
        Path dirInforme = Path.of(args[2]);
        Files.createDirectories(dirModelos);
        Files.createDirectories(dirInforme);

        Instances datos;
        try (BufferedReader r = Files.newBufferedReader(arff)) {
            datos = new Instances(r);
        }
        datos.setClassIndex(datos.numAttributes() - 1);

        Instances base = new Instances(datos);
        base.randomize(new Random(SEMILLA));

        Map<String, Supplier<Classifier>> algoritmos = new LinkedHashMap<>();
        algoritmos.put("LinearRegression", LinearRegression::new);
        algoritmos.put("REPTree", () -> {
            REPTree t = new REPTree();
            t.setSeed(SEMILLA);
            return t;
        });
        algoritmos.put("RandomForest", () -> {
            RandomForest rf = new RandomForest();
            rf.setNumIterations(100); // numero de arboles (opcion -I de Weka)
            rf.setSeed(SEMILLA);
            return rf;
        });

        List<Resultado> resultados = new ArrayList<>();
        for (Map.Entry<String, Supplier<Classifier>> e : algoritmos.entrySet()) {
            String nombre = e.getKey();
            System.out.println("== Validacion cruzada " + PARTICIONES + " particiones: " + nombre + " ==");
            Resultado r = validarCV(nombre, e.getValue(), base, dirInforme);
            resultados.add(r);

            Classifier modeloFinal = e.getValue().get();
            long t0 = System.nanoTime();
            modeloFinal.buildClassifier(base);
            System.out.printf(Locale.US, "Modelo final entrenado con %d filas en %d s%n",
                    base.numInstances(), (System.nanoTime() - t0) / 1_000_000_000L);
            Instances cabecera = new Instances(base, 0);
            SerializationHelper.write(dirModelos.resolve(nombre + ".model").toString(),
                    new Object[]{modeloFinal, cabecera});
            System.out.println("Modelo guardado: " + dirModelos.resolve(nombre + ".model").toAbsolutePath());
        }

        escribirResumen(resultados, dirInforme.resolve("resultados_modelos.txt"), base.numInstances());
        System.out.println("Resumen guardado en: " + dirInforme.resolve("resultados_modelos.txt").toAbsolutePath());
    }

    static Resultado validarCV(String nombre, Supplier<Classifier> fabrica, Instances base, Path dirInforme)
            throws Exception {
        int n = base.numInstances();
        double[] reales = new double[n];
        double[] predichas = new double[n];
        List<Double> maeFolds = new ArrayList<>();
        int pos = 0;
        long inicio = System.nanoTime();

        for (int k = 0; k < PARTICIONES; k++) {
            Instances entrenamiento = base.trainCV(PARTICIONES, k);
            Instances prueba = base.testCV(PARTICIONES, k);
            Classifier c = fabrica.get();
            c.buildClassifier(entrenamiento);

            double sumaAbs = 0;
            for (int i = 0; i < prueba.numInstances(); i++) {
                Instance in = prueba.instance(i);
                double real = in.classValue();
                double pred = c.classifyInstance(in);
                reales[pos] = real;
                predichas[pos] = pred;
                pos++;
                sumaAbs += Math.abs(pred - real);
            }
            double maeFold = sumaAbs / prueba.numInstances();
            maeFolds.add(maeFold);
            System.out.printf(Locale.US, "  particion %2d: MAE=%.4f (%d de entrenamiento, %d de prueba)%n",
                    k + 1, maeFold, entrenamiento.numInstances(), prueba.numInstances());
        }
        double segundos = (System.nanoTime() - inicio) / 1_000_000_000.0;

        double sumaAbs = 0, sumaCuad = 0;
        for (int i = 0; i < n; i++) {
            double err = predichas[i] - reales[i];
            sumaAbs += Math.abs(err);
            sumaCuad += err * err;
        }
        double mae = sumaAbs / n;
        double rmse = Math.sqrt(sumaCuad / n);
        double correlacion = pearson(reales, predichas);
        double mediaReal = Arrays.stream(reales).average().orElse(Double.NaN);
        double sst = Arrays.stream(reales).map(x -> (x - mediaReal) * (x - mediaReal)).sum();
        double r2 = 1 - sumaCuad / sst;
        double maeDesv = desviacion(maeFolds);

        double[] errAbs = new double[n];
        for (int i = 0; i < n; i++) errAbs[i] = Math.abs(predichas[i] - reales[i]);
        double[] ordenado = errAbs.clone();
        Arrays.sort(ordenado);
        double p50 = percentil(ordenado, 0.50);
        double p90 = percentil(ordenado, 0.90);

        double[] maePorRango = new double[RANGOS.length];
        int[] nPorRango = new int[RANGOS.length];
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < RANGOS.length; r++) {
                double limInf = r == 0 ? -1 : LIMITES[r - 1];
                if (reales[i] > limInf && reales[i] <= LIMITES[r]) {
                    maePorRango[r] += errAbs[i];
                    nPorRango[r]++;
                    break;
                }
            }
        }
        for (int r = 0; r < RANGOS.length; r++) {
            maePorRango[r] = nPorRango[r] == 0 ? Double.NaN : maePorRango[r] / nPorRango[r];
        }

        Classifier cfg = fabrica.get();
        String configuracion = nombre + " " + Utils.joinOptions(((OptionHandler) cfg).getOptions());

        escribirPredicciones(dirInforme.resolve("predicciones_" + nombre + ".csv"), reales, predichas);

        return new Resultado(nombre, configuracion.trim(), mae, rmse, correlacion, r2, maeDesv, p50, p90,
                segundos, maePorRango, nPorRango, reales, predichas);
    }

    static void escribirResumen(List<Resultado> rs, Path ruta, int filas) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("RESULTADOS DE MODELOS - SmartBody (regresion de semanas_estimadas)\n");
        sb.append("Validacion cruzada: ").append(PARTICIONES).append(" particiones, semilla ").append(SEMILLA)
          .append(", ").append(filas).append(" filas (dataset limpio).\n\n");

        sb.append("== Tabla comparativa ==\n");
        sb.append(String.format(Locale.US, "%-18s %8s %8s %8s %8s %10s %8s %8s %10s%n",
                "algoritmo", "MAE", "RMSE", "CC", "R2", "MAE desv", "P50 err", "P90 err", "tiempo(s)"));
        for (Resultado r : rs) {
            sb.append(String.format(Locale.US, "%-18s %8.3f %8.3f %8.4f %8.4f %10.3f %8.3f %8.3f %10.1f%n",
                    r.nombre(), r.mae(), r.rmse(), r.correlacion(), r.r2(), r.maeDesviacion(), r.p50(), r.p90(), r.segundos()));
        }
        sb.append("\nMAE desv: desviacion estandar del MAE entre las 10 particiones (estabilidad).\n");
        sb.append("P50/P90 err: error absoluto mediano y percentil 90, en semanas.\n\n");

        sb.append("== MAE por rango de semanas reales (analisis de errores) ==\n");
        sb.append(String.format(Locale.US, "%-18s", "algoritmo"));
        for (String rango : RANGOS) sb.append(String.format(Locale.US, " %14s", rango + " sem"));
        sb.append('\n');
        for (Resultado r : rs) {
            sb.append(String.format(Locale.US, "%-18s", r.nombre()));
            for (int i = 0; i < RANGOS.length; i++) {
                sb.append(String.format(Locale.US, " %8.3f (n=%d)", r.maePorRango()[i], r.nPorRango()[i]));
            }
            sb.append('\n');
        }

        sb.append("\n== Configuracion ==\n");
        for (Resultado r : rs) sb.append(r.configuracion()).append('\n');

        Files.writeString(ruta, sb.toString(), StandardCharsets.UTF_8);
        System.out.println(sb);
    }

    static void escribirPredicciones(Path ruta, double[] reales, double[] predichas) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            w.write("semanas_reales,semanas_predichas,error_absoluto");
            w.newLine();
            for (int i = 0; i < reales.length; i++) {
                w.write(String.format(Locale.US, "%.2f,%.2f,%.2f", reales[i], predichas[i], Math.abs(predichas[i] - reales[i])));
                w.newLine();
            }
        }
    }

    static double pearson(double[] x, double[] y) {
        int n = x.length;
        double mx = Arrays.stream(x).average().orElse(0), my = Arrays.stream(y).average().orElse(0);
        double sxy = 0, sxx = 0, syy = 0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - mx, dy = y[i] - my;
            sxy += dx * dy;
            sxx += dx * dx;
            syy += dy * dy;
        }
        return sxy / Math.sqrt(sxx * syy);
    }

    static double desviacion(List<Double> valores) {
        double media = valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return Math.sqrt(valores.stream().mapToDouble(v -> (v - media) * (v - media)).average().orElse(0));
    }

    static double percentil(double[] ordenado, double p) {
        double pos = p * (ordenado.length - 1);
        int bajo = (int) Math.floor(pos);
        int alto = (int) Math.ceil(pos);
        return ordenado[bajo] + (pos - bajo) * (ordenado[alto] - ordenado[bajo]);
    }
}
