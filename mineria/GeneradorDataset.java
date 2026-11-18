import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Generador del dataset SINTETICO de SmartBody para mineria de datos (regresion).
 *
 * Produce 100.000 filas con la distribucion pedida:
 *   80.000 LIMPIO                 (80%)
 *   10.000 OUTLIER                (10%): valores extremos pero plausibles
 *    3.500 PROBLEMATICO_DUPLICADO  \
 *    3.500 PROBLEMATICO_FALTANTE    > 10% problematicos (3,5% + 3,5% + 3%)
 *    3.000 PROBLEMATICO_INVALIDO  /
 *
 * La variable objetivo semanas_estimadas se calcula con una formula documentada
 * (ver mineria/README.md, seccion "Generacion"). Es un modelo simplificado y NO es
 * informacion clinica real.
 *
 * Reproducible: semilla fija. Ejecutar desde la carpeta mineria/:
 *     java GeneradorDataset.java datos
 */
public class GeneradorDataset {

    static final long SEMILLA = 42L;
    static final int N_LIMPIOS = 80_000;
    static final int N_OUTLIERS = 10_000;
    static final int N_DUPLICADOS = 3_500;
    static final int N_FALTANTES = 3_500;
    static final int N_INVALIDOS = 3_000;

    /** Columnas del CSV completo (incluye identificadores y la etiqueta de control tipo_registro). */
    static final String[] COLUMNAS = {
        "id_registro", "usuario_id", "tipo_registro",
        "edad", "sexo", "altura_cm", "peso_inicial_kg", "peso_objetivo_kg", "diferencia_peso_kg", "imc_inicial",
        "objetivo", "nivel_actividad", "nivel_experiencia", "entrenamientos_semana", "minutos_sesion", "tipo_actividad",
        "consumo_energetico_kcal", "balance_energetico_kcal", "restricciones_total",
        "adherencia_nutricional_pct", "adherencia_ejercicio_pct", "semanas_estimadas"
    };

    /** Atributos del ARFF: predictores + clase (ultimo). Sin identificadores ni etiqueta de control. */
    static final String[][] ATRIBUTOS_ARFF = {
        {"edad", "NUMERIC"},
        {"sexo", "{MASCULINO,FEMENINO}"},
        {"altura_cm", "NUMERIC"},
        {"peso_inicial_kg", "NUMERIC"},
        {"peso_objetivo_kg", "NUMERIC"},
        {"diferencia_peso_kg", "NUMERIC"},
        {"imc_inicial", "NUMERIC"},
        {"objetivo", "{PERDER_PESO,GANAR_MASA}"},
        {"nivel_actividad", "{SEDENTARIO,LIGERO,MODERADO,ACTIVO}"},
        {"nivel_experiencia", "{PRINCIPIANTE,INTERMEDIO,AVANZADO}"},
        {"entrenamientos_semana", "NUMERIC"},
        {"minutos_sesion", "NUMERIC"},
        {"tipo_actividad", "{FUERZA,CARDIO,MIXTO}"},
        {"consumo_energetico_kcal", "NUMERIC"},
        {"balance_energetico_kcal", "NUMERIC"},
        {"restricciones_total", "NUMERIC"},
        {"adherencia_nutricional_pct", "NUMERIC"},
        {"adherencia_ejercicio_pct", "NUMERIC"},
        {"semanas_estimadas", "NUMERIC"}
    };

    static final String[] CAMPOS_FALTANTES = {
        "altura_cm", "edad", "minutos_sesion", "restricciones_total", "adherencia_nutricional_pct",
        "adherencia_ejercicio_pct", "entrenamientos_semana", "nivel_actividad", "tipo_actividad"
    };

    static final Random rnd = new Random(SEMILLA);

    public static void main(String[] args) throws IOException {
        Path salida = Path.of(args.length > 0 ? args[0] : "datos");
        Files.createDirectories(salida);

        int usuario = 1;
        List<Map<String, String>> limpios = new ArrayList<>();
        for (int i = 0; i < N_LIMPIOS; i++) {
            limpios.add(generar(false, usuario++, "LIMPIO"));
        }

        List<Map<String, String>> filas = new ArrayList<>(limpios);

        for (int i = 0; i < N_OUTLIERS; i++) {
            filas.add(generar(true, usuario++, "OUTLIER"));
        }

        // Duplicados: copias exactas de registros limpios (mismo usuario, mismo contenido).
        for (int i = 0; i < N_DUPLICADOS; i++) {
            Map<String, String> copia = new LinkedHashMap<>(limpios.get(rnd.nextInt(limpios.size())));
            copia.put("tipo_registro", "PROBLEMATICO_DUPLICADO");
            filas.add(copia);
        }

        for (int i = 0; i < N_FALTANTES; i++) {
            Map<String, String> f = generar(false, usuario++, "PROBLEMATICO_FALTANTE");
            aplicarFaltantes(f);
            filas.add(f);
        }

        for (int i = 0; i < N_INVALIDOS; i++) {
            Map<String, String> f = generar(false, usuario++, "PROBLEMATICO_INVALIDO");
            aplicarInvalidos(f);
            filas.add(f);
        }

        Collections.shuffle(filas, rnd);
        int id = 1;
        for (Map<String, String> f : filas) {
            f.put("id_registro", String.valueOf(id++));
        }

        escribirCsv(salida.resolve("smartbody_dataset_100000.csv"), filas);
        escribirArff(salida.resolve("smartbody_dataset_100000.arff"), filas);

        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Map<String, String> f : filas) {
            conteo.merge(f.get("tipo_registro"), 1, Integer::sum);
        }
        System.out.println("Total de filas: " + filas.size());
        conteo.forEach((tipo, n) -> System.out.printf(Locale.US, "  %-26s %6d  (%.2f%%)%n", tipo, n, 100.0 * n / filas.size()));
        System.out.println("Archivos en: " + salida.toAbsolutePath());
    }

    /** Genera una persona con sus datos de inicio, su plan y su variable objetivo. */
    static Map<String, String> generar(boolean atipico, int usuarioId, String tipo) {
        boolean masc = rnd.nextDouble() < 0.5;
        String sexo = masc ? "MASCULINO" : "FEMENINO";
        double altura = r1(masc ? gauss(175, 7, 150, 200) : gauss(162, 6, 145, 190));

        boolean perder = rnd.nextDouble() < 0.75;
        String objetivo = perder ? "PERDER_PESO" : "GANAR_MASA";

        int edad = atipico ? (int) uni(62, 80) : (int) uni(18, 65);

        double imcBase = perder ? (atipico ? uni(38, 50) : uni(21, 38)) : uni(17.5, 24);
        double pesoIni = r1(imcBase * Math.pow(altura / 100.0, 2));
        pesoIni = Math.max(40, Math.min(atipico ? 160 : 150, pesoIni));

        double pesoObj;
        if (perder) {
            double dif = atipico ? uni(25, 45) : uni(2, 0.2 * pesoIni);
            // El objetivo siempre queda por debajo del inicial (coherencia), con un minimo de 40 kg.
            pesoObj = Math.min(pesoIni - 1, Math.max(40, r1(pesoIni - dif)));
        } else {
            double dif = atipico ? uni(10, 20) : uni(2, 0.15 * pesoIni);
            pesoObj = r1(pesoIni + dif);
        }
        double diferencia = r1(Math.abs(pesoIni - pesoObj));

        String nivelExp = pick(new double[]{0.5, 0.35, 0.15}, "PRINCIPIANTE", "INTERMEDIO", "AVANZADO");
        int entrena;
        if (atipico) {
            entrena = (int) uni(6, 7.99);
        } else if (nivelExp.equals("PRINCIPIANTE")) {
            entrena = (int) uni(2, 4.99);
        } else if (nivelExp.equals("INTERMEDIO")) {
            entrena = (int) uni(3, 5.99);
        } else {
            entrena = (int) uni(4, 6.99);
        }
        int minutos = atipico ? 120 + 5 * rnd.nextInt(13) : 20 + 5 * rnd.nextInt(15);
        String nivelAct = pick(new double[]{0.25, 0.30, 0.30, 0.15}, "SEDENTARIO", "LIGERO", "MODERADO", "ACTIVO");
        String tipoAct = pick(new double[]{0.35, 0.35, 0.30}, "FUERZA", "CARDIO", "MIXTO");
        int restr = atipico ? 3 : Integer.parseInt(pick(new double[]{0.60, 0.25, 0.10, 0.05}, "0", "1", "2", "3"));

        double adhN, adhE;
        if (atipico) {
            adhN = rnd.nextBoolean() ? uni(95, 100) : uni(15, 30);
            adhE = rnd.nextBoolean() ? uni(95, 100) : uni(15, 30);
        } else {
            adhN = gauss(75, 15, 20, 100);
            adhE = gauss(72, 18, 20, 100);
        }
        adhN = r1(adhN);
        adhE = r1(adhE);

        // Derivadas (calculadas con los valores ya redondeados, para que sean consistentes).
        double imc = r2(pesoIni / Math.pow(altura / 100.0, 2));
        double tmb = 10 * pesoIni + 6.25 * altura - 5 * edad + (masc ? 5 : -161);
        int tdee = (int) Math.round(tmb * factorActividad(nivelAct));

        int balance;
        if (perder) {
            int minCal = masc ? 1500 : 1200;
            balance = (int) Math.round(uni(150, Math.min(600, 0.2 * tdee)));
            if (tdee - balance < minCal) {
                balance = Math.max(0, tdee - minCal);
            }
        } else {
            balance = (int) Math.round(uni(150, 400));
        }

        double kgSemana;
        if (perder) {
            double ejercicioKcal = entrena * minutos * kcalPorMinuto(tipoAct);
            kgSemana = (balance * 7 * adhN / 100.0 + ejercicioKcal * adhE / 100.0) / 7700.0;
            kgSemana = Math.min(kgSemana, 0.01 * pesoIni);
        } else {
            double factorExp = nivelExp.equals("PRINCIPIANTE") ? 1.0 : nivelExp.equals("INTERMEDIO") ? 0.7 : 0.45;
            kgSemana = 0.25 * factorExp * (0.5 * adhN / 100.0 + 0.5 * adhE / 100.0);
        }
        double ruido = Math.max(0.75, Math.min(1.25, 1 + rnd.nextGaussian() * 0.10));
        double semanas = r1(Math.max(2, Math.min(104, diferencia / kgSemana * ruido)));

        Map<String, String> f = new LinkedHashMap<>();
        f.put("id_registro", "");
        f.put("usuario_id", String.valueOf(usuarioId));
        f.put("tipo_registro", tipo);
        f.put("edad", String.valueOf(edad));
        f.put("sexo", sexo);
        f.put("altura_cm", f1(altura));
        f.put("peso_inicial_kg", f1(pesoIni));
        f.put("peso_objetivo_kg", f1(pesoObj));
        f.put("diferencia_peso_kg", f1(diferencia));
        f.put("imc_inicial", f2(imc));
        f.put("objetivo", objetivo);
        f.put("nivel_actividad", nivelAct);
        f.put("nivel_experiencia", nivelExp);
        f.put("entrenamientos_semana", String.valueOf(entrena));
        f.put("minutos_sesion", String.valueOf(minutos));
        f.put("tipo_actividad", tipoAct);
        f.put("consumo_energetico_kcal", String.valueOf(tdee));
        f.put("balance_energetico_kcal", String.valueOf(balance));
        f.put("restricciones_total", String.valueOf(restr));
        f.put("adherencia_nutricional_pct", f1(adhN));
        f.put("adherencia_ejercicio_pct", f1(adhE));
        f.put("semanas_estimadas", f1(semanas));
        return f;
    }

    static void aplicarFaltantes(Map<String, String> f) {
        List<String> campos = new ArrayList<>(List.of(CAMPOS_FALTANTES));
        Collections.shuffle(campos, rnd);
        int k = 1 + rnd.nextInt(3);
        for (int i = 0; i < k; i++) {
            f.put(campos.get(i), "");
        }
    }

    static void aplicarInvalidos(Map<String, String> f) {
        List<Integer> tipos = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6));
        Collections.shuffle(tipos, rnd);
        int k = 1 + rnd.nextInt(2);
        for (int i = 0; i < k; i++) {
            aplicarInvalido(f, tipos.get(i));
        }
    }

    static void aplicarInvalido(Map<String, String> f, int tipo) {
        boolean bajo = rnd.nextBoolean();
        switch (tipo) {
            case 0 -> f.put("edad", bajo ? "-5" : "250");
            case 1 -> f.put("altura_cm", bajo ? "30.0" : "320.0");
            case 2 -> {
                // Objetivo PERDER_PESO pero con peso objetivo por encima del inicial (incoherente).
                double pesoIni = Double.parseDouble(f.get("peso_inicial_kg"));
                f.put("objetivo", "PERDER_PESO");
                f.put("peso_objetivo_kg", f1(pesoIni + 5));
            }
            case 3 -> f.put("adherencia_nutricional_pct", bajo ? "-10.0" : "150.0");
            case 4 -> f.put("minutos_sesion", bajo ? "0" : "600");
            case 5 -> f.put("imc_inicial", f2(Double.parseDouble(f.get("imc_inicial")) * 1.8));
            case 6 -> f.put("semanas_estimadas", bajo ? "0.0" : "-3.0");
            default -> throw new IllegalStateException("tipo invalido " + tipo);
        }
    }

    static void escribirCsv(Path ruta, List<Map<String, String>> filas) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            w.write(String.join(",", COLUMNAS));
            w.newLine();
            for (Map<String, String> f : filas) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < COLUMNAS.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(f.getOrDefault(COLUMNAS[i], ""));
                }
                w.write(sb.toString());
                w.newLine();
            }
        }
    }

    static void escribirArff(Path ruta, List<Map<String, String>> filas) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            w.write("% Dataset sintetico de SmartBody (no es informacion clinica real).");
            w.newLine();
            w.write("% Generado por GeneradorDataset.java con semilla " + SEMILLA + ". Ver mineria/README.md.");
            w.newLine();
            w.write("@relation smartbody_semanas_estimadas");
            w.newLine();
            w.newLine();
            for (String[] attr : ATRIBUTOS_ARFF) {
                w.write("@attribute " + attr[0] + " " + attr[1]);
                w.newLine();
            }
            w.newLine();
            w.write("@data");
            w.newLine();
            for (Map<String, String> f : filas) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ATRIBUTOS_ARFF.length; i++) {
                    if (i > 0) sb.append(',');
                    String v = f.get(ATRIBUTOS_ARFF[i][0]);
                    sb.append(v == null || v.isEmpty() ? "?" : v);
                }
                w.write(sb.toString());
                w.newLine();
            }
        }
    }

    // ---------- utilidades ----------

    static double factorActividad(String nivel) {
        return switch (nivel) {
            case "SEDENTARIO" -> 1.2;
            case "LIGERO" -> 1.375;
            case "MODERADO" -> 1.55;
            default -> 1.725;
        };
    }

    static double kcalPorMinuto(String tipo) {
        return switch (tipo) {
            case "FUERZA" -> 5.0;
            case "CARDIO" -> 8.0;
            default -> 6.5;
        };
    }

    static double uni(double a, double b) {
        return a + rnd.nextDouble() * (b - a);
    }

    static double gauss(double media, double sd, double min, double max) {
        return Math.max(min, Math.min(max, media + rnd.nextGaussian() * sd));
    }

    static String pick(double[] pesos, String... opciones) {
        double x = rnd.nextDouble();
        double acum = 0;
        for (int i = 0; i < pesos.length; i++) {
            acum += pesos[i];
            if (x < acum) return opciones[i];
        }
        return opciones[opciones.length - 1];
    }

    static double r1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    static double r2(double v) {
        return Math.round(v * 100) / 100.0;
    }

    static String f1(double v) {
        return String.format(Locale.US, "%.1f", v);
    }

    static String f2(double v) {
        return String.format(Locale.US, "%.2f", v);
    }
}
