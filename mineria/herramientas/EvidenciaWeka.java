import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Genera la evidencia de Weka en archivos, con la misma informacion que muestra Weka Explorer.
 *
 * Usa los valores por defecto del Explorer (semilla 1 en la validacion cruzada, 10 particiones),
 * asi que las cifras coinciden con las que aparecen en la ventana.
 *
 * Desde mineria/:
 *   java -Xmx2g -cp "lib/*" herramientas/EvidenciaWeka.java datos
 *   java -Xmx2g -cp "lib/*" herramientas/EvidenciaWeka.java REPTree LinearRegression
 *   java -Xmx2g -cp "lib/*" herramientas/EvidenciaWeka.java RandomForest
 */
public class EvidenciaWeka {

    static final Path CARPETA = Path.of("informe", "evidencia");
    static final Path ORIGINAL = Path.of("datos", "smartbody_dataset_100000.arff");
    static final Path LIMPIO = Path.of("datos", "smartbody_limpio.arff");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(CARPETA);
        if (args.length == 0 || args[0].equals("datos")) {
            generarResumenesYGraficas();
            return;
        }
        Instances limpio = cargar(LIMPIO);
        for (String algoritmo : args) {
            evaluar(algoritmo, limpio);
        }
    }

    // ------------------------------------------------------------------
    // Fases 2 y 3: lo que se ve en Preprocess (resumen de atributos y "Visualize All")
    // ------------------------------------------------------------------

    static void generarResumenesYGraficas() throws IOException {
        Instances original = cargar(ORIGINAL);
        Instances limpio = cargar(LIMPIO);
        escribir("01_fase2_resumen_original.txt", resumen("Dataset original (Preprocess)", original));
        escribir("02_fase3_resumen_limpio.txt", resumen("Dataset limpio (Preprocess)", limpio));
        graficarAtributos(original, "03_fase2_visualize_all_original.png");
        graficarAtributos(limpio, "04_fase3_visualize_all_limpio.png");
        System.out.println("Resumenes y graficas de Preprocess generados en " + CARPETA.toAbsolutePath());
    }

    static String resumen(String titulo, Instances d) {
        StringBuilder sb = new StringBuilder();
        sb.append("== ").append(titulo).append(" ==\n");
        sb.append("Relation: ").append(d.relationName()).append('\n');
        sb.append("Instances: ").append(d.numInstances()).append('\n');
        sb.append("Attributes: ").append(d.numAttributes()).append("\n\n");
        sb.append(String.format(Locale.US, "%-30s %-8s %8s %8s %8s %12s %12s %12s %12s%n",
                "Atributo", "Tipo", "Faltan", "%Faltan", "Distintos", "Minimo", "Maximo", "Media", "Desv.Est."));
        for (int i = 0; i < d.numAttributes(); i++) {
            Attribute a = d.attribute(i);
            AttributeStats st = d.attributeStats(i);
            double pctFaltan = 100.0 * st.missingCount / d.numInstances();
            if (a.isNumeric()) {
                sb.append(String.format(Locale.US, "%-30s %-8s %8d %7.2f%% %8d %12.2f %12.2f %12.2f %12.2f%n",
                        a.name(), "NUMERIC", st.missingCount, pctFaltan, st.distinctCount,
                        st.numericStats.min, st.numericStats.max, st.numericStats.mean, st.numericStats.stdDev));
            } else {
                sb.append(String.format(Locale.US, "%-30s %-8s %8d %7.2f%% %8d   categorias: ",
                        a.name(), "NOMINAL", st.missingCount, pctFaltan, st.distinctCount));
                for (int v = 0; v < a.numValues(); v++) {
                    sb.append(a.value(v)).append('=').append(st.nominalCounts[v]);
                    if (v < a.numValues() - 1) sb.append(", ");
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /** Equivalente al boton "Visualize All" del Explorer: un histograma por atributo. */
    static void graficarAtributos(Instances d, String nombre) throws IOException {
        int columnas = 4;
        int n = d.numAttributes();
        int filas = (int) Math.ceil(n / (double) columnas);
        int celdaW = 340, celdaH = 230, margen = 12;
        BufferedImage img = new BufferedImage(columnas * celdaW, filas * celdaH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        for (int i = 0; i < n; i++) {
            int x0 = (i % columnas) * celdaW + margen;
            int y0 = (i / columnas) * celdaH + margen;
            int ancho = celdaW - 2 * margen;
            int alto = celdaH - 2 * margen - 30;
            Attribute a = d.attribute(i);
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString(a.name() + (a.isNominal() ? " (nominal)" : ""), x0, y0 + 14);

            int base = y0 + 30 + alto;
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(x0, base, x0 + ancho, base);

            if (a.isNumeric()) {
                dibujarHistogramaNumerico(g, d, i, x0, y0 + 30, ancho, alto);
            } else {
                dibujarBarrasNominales(g, d, i, x0, y0 + 30, ancho, alto);
            }
        }
        g.dispose();
        ImageIO.write(img, "png", CARPETA.resolve(nombre).toFile());
    }

    static void dibujarHistogramaNumerico(Graphics2D g, Instances d, int i, int x0, int y0, int ancho, int alto) {
        List<Double> valores = new ArrayList<>();
        for (int k = 0; k < d.numInstances(); k++) {
            if (!d.instance(k).isMissing(i)) valores.add(d.instance(k).value(i));
        }
        if (valores.isEmpty()) return;
        double min = valores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = valores.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (max == min) max = min + 1;
        int bins = 24;
        int[] conteo = new int[bins];
        for (double v : valores) {
            int b = (int) ((v - min) / (max - min) * bins);
            conteo[Math.min(bins - 1, b)]++;
        }
        int maximo = 1;
        for (int c : conteo) maximo = Math.max(maximo, c);
        int ancho1 = ancho / bins;
        g.setColor(new Color(40, 110, 190));
        for (int b = 0; b < bins; b++) {
            int h = (int) Math.round(conteo[b] / (double) maximo * alto);
            g.fillRect(x0 + b * ancho1, y0 + alto - h, Math.max(1, ancho1 - 1), h);
        }
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString(String.format(Locale.US, "min %.2f", min), x0, y0 + alto + 16);
        String max1 = String.format(Locale.US, "max %.2f", max);
        g.drawString(max1, x0 + ancho - g.getFontMetrics().stringWidth(max1), y0 + alto + 16);
    }

    static void dibujarBarrasNominales(Graphics2D g, Instances d, int i, int x0, int y0, int ancho, int alto) {
        Attribute a = d.attribute(i);
        int[] conteo = new int[a.numValues()];
        for (int k = 0; k < d.numInstances(); k++) {
            if (!d.instance(k).isMissing(i)) conteo[(int) d.instance(k).value(i)]++;
        }
        int maximo = 1;
        for (int c : conteo) maximo = Math.max(maximo, c);
        int ancho1 = ancho / Math.max(1, conteo.length);
        g.setColor(new Color(60, 160, 110));
        for (int v = 0; v < conteo.length; v++) {
            int h = (int) Math.round(conteo[v] / (double) maximo * alto);
            g.fillRect(x0 + v * ancho1 + 4, y0 + alto - h, ancho1 - 8, h);
        }
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int v = 0; v < conteo.length; v++) {
            g.drawString(a.value(v), x0 + v * ancho1 + 4, y0 + alto + 14);
        }
    }

    // ------------------------------------------------------------------
    // Fases 4 y 5: lo que se ve en Classify con validacion cruzada de 10 particiones
    // ------------------------------------------------------------------

    static void evaluar(String nombre, Instances datos) throws Exception {
        Supplier<Classifier> fabrica = fabricaDe(nombre);
        System.out.println("== Evaluando " + nombre + " ==");
        long inicio = System.currentTimeMillis();

        // Como el Explorer: primero el modelo con todo el conjunto, luego la validacion cruzada.
        Classifier modeloCompleto = fabrica.get();
        modeloCompleto.buildClassifier(datos);
        String textoModelo = modeloCompleto.toString();

        Evaluation evaluacion = new Evaluation(datos);
        evaluacion.crossValidateModel(fabrica.get(), datos, 10, new Random(1));
        long segundos = (System.currentTimeMillis() - inicio) / 1000;

        String opciones = Utils.joinOptions(((OptionHandler) fabrica.get()).getOptions());
        StringBuilder sb = new StringBuilder();
        sb.append("=== Run information ===\n\n");
        sb.append("Scheme:       weka.classifiers.").append(carpetaDe(nombre)).append('.').append(nombre)
          .append(' ').append(opciones).append('\n');
        sb.append("Relation:     ").append(datos.relationName()).append('\n');
        sb.append("Instances:    ").append(datos.numInstances()).append('\n');
        sb.append("Attributes:   ").append(datos.numAttributes()).append('\n');
        sb.append("Test mode:    10-fold cross-validation (semilla 1, valores por defecto del Explorer)\n");
        sb.append("Tiempo total: ").append(segundos).append(" s\n\n");
        sb.append("=== Classifier model (full training set) ===\n\n");
        sb.append(textoModelo).append('\n');
        sb.append(evaluacion.toSummaryString("\n=== Summary ===\n", false));

        escribir("salida_" + nombre + ".txt", sb.toString());
        escribir("metricas_" + nombre + ".csv",
                "algoritmo,MAE,RMSE,CC,R2\n" + String.format(Locale.US, "%s,%.4f,%.4f,%.4f,%.4f%n", nombre,
                        evaluacion.meanAbsoluteError(), evaluacion.rootMeanSquaredError(),
                        evaluacion.correlationCoefficient(), r2(evaluacion)));

        List<Prediction> predicciones = evaluacion.predictions();
        graficarRealVsPredicho(predicciones, "05_" + nombre + "_real_vs_predicho.png", nombre);
        graficarResiduos(predicciones, "06_" + nombre + "_residuos.png", nombre);

        if (modeloCompleto instanceof REPTree arbol) {
            escribir("arbol_REPTree.dot", arbol.graph());
        }
        System.out.printf(Locale.US, "  MAE=%.4f RMSE=%.4f CC=%.4f  (%d s)%n",
                evaluacion.meanAbsoluteError(), evaluacion.rootMeanSquaredError(),
                evaluacion.correlationCoefficient(), segundos);
    }

    static Supplier<Classifier> fabricaDe(String nombre) {
        return switch (nombre) {
            case "LinearRegression" -> LinearRegression::new;
            case "REPTree" -> REPTree::new;
            case "RandomForest" -> RandomForest::new;
            default -> throw new IllegalArgumentException("Algoritmo no soportado: " + nombre);
        };
    }

    static String carpetaDe(String nombre) {
        return switch (nombre) {
            case "LinearRegression" -> "functions";
            default -> "trees";
        };
    }

    static double r2(Evaluation e) {
        // R2 = 1 - SSE/SST a partir de las predicciones de validacion cruzada.
        List<Prediction> p = e.predictions();
        double media = p.stream().mapToDouble(Prediction::actual).average().orElse(0);
        double sse = 0, sst = 0;
        for (Prediction x : p) {
            sse += Math.pow(x.actual() - x.predicted(), 2);
            sst += Math.pow(x.actual() - media, 2);
        }
        return 1 - sse / sst;
    }

    /** Equivalente a "Visualize classifier errors": valor real frente al predicho. */
    static void graficarRealVsPredicho(List<Prediction> p, String nombre, String algoritmo) throws IOException {
        int w = 700, h = 700;
        int izq = 90, arriba = 60, lado = 560;
        double max = 110;
        int step = Math.max(1, p.size() / 3000);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        // Ejes y marco
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(izq, arriba, lado, lado);
        // Linea de prediccion perfecta (y = x)
        g.setColor(new Color(170, 170, 170));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 6f}, 0f));
        g.drawLine(izq, arriba + lado, izq + lado, arriba);
        g.setStroke(new BasicStroke(1f));

        g.setColor(new Color(40, 110, 190, 90));
        for (int i = 0; i < p.size(); i += step) {
            double real = p.get(i).actual();
            double pred = p.get(i).predicted();
            int x = izq + (int) Math.round(real / max * lado);
            int y = arriba + lado - (int) Math.round(pred / max * lado);
            g.fillOval(x - 2, y - 2, 4, 4);
        }

        // Textos fuera del area de la grafica
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString(algoritmo + " - valor real frente a predicho (validacion cruzada)", izq, 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("Semanas reales", izq + lado / 2 - 50, arriba + lado + 30);
        java.awt.geom.AffineTransform original = g.getTransform();
        g.rotate(-Math.PI / 2, 30, arriba + lado / 2);
        g.drawString("Semanas predichas", 30, arriba + lado / 2);
        g.setTransform(original);
        g.drawString("Linea gris discontinua: prediccion perfecta (predicho = real)", izq, h - 20);
        g.dispose();
        ImageIO.write(img, "png", CARPETA.resolve(nombre).toFile());
    }

    /** Histograma del error de prediccion (predicho - real). */
    static void graficarResiduos(List<Prediction> p, String nombre, String algoritmo) throws IOException {
        int w = 700, h = 460, m = 60;
        double min = -40, max = 40;
        int bins = 80;
        int[] conteo = new int[bins];
        for (Prediction x : p) {
            double e = x.predicted() - x.actual();
            int b = (int) Math.floor((e - min) / (max - min) * bins);
            if (b >= 0 && b < bins) conteo[b]++;
        }
        int maximo = 1;
        for (int c : conteo) maximo = Math.max(maximo, c);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        int area = w - 2 * m;
        int alto = h - 2 * m;
        g.setColor(new Color(200, 110, 40));
        for (int b = 0; b < bins; b++) {
            int hb = (int) Math.round(conteo[b] / (double) maximo * alto);
            g.fillRect(m + b * area / bins, m + alto - hb, Math.max(1, area / bins - 1), hb);
        }
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString(algoritmo + " - distribucion del error (predicho - real), en semanas", m, 35);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("-40", m - 5, h - m + 16);
        g.drawString("0", m + area / 2 - 4, h - m + 16);
        g.drawString("+40", m + area - 20, h - m + 16);
        g.dispose();
        ImageIO.write(img, "png", CARPETA.resolve(nombre).toFile());
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    static Instances cargar(Path ruta) throws IOException {
        try (BufferedReader r = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            Instances d = new Instances(r);
            d.setClassIndex(d.numAttributes() - 1);
            return d;
        }
    }

    static void escribir(String nombre, String contenido) throws IOException {
        Files.writeString(CARPETA.resolve(nombre), contenido, StandardCharsets.UTF_8);
    }
}
