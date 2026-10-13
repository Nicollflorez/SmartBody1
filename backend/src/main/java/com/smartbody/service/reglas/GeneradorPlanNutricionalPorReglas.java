package com.smartbody.service.reglas;

import com.smartbody.entity.Alimento;
import com.smartbody.entity.MomentoComida;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Reglas del plan nutricional:
 * <ol>
 *   <li>Calorias diarias con la ecuacion de Mifflin-St Jeor, ajustadas por actividad y objetivo.</li>
 *   <li>Reparto en desayuno, almuerzo, cena y snack.</li>
 *   <li>Cada comida combina grupos de alimentos; los incompatibles con las restricciones se descartan.</li>
 *   <li>La cantidad de cada alimento se calcula a partir de las calorias que le corresponden.</li>
 * </ol>
 * No sustituye la valoracion de un profesional de la salud.
 */
@Component
public class GeneradorPlanNutricionalPorReglas implements GeneradorPlanNutricional {

    static final int CALORIAS_MINIMAS = 1200;
    private static final double DENSIDAD_MAXIMA_SNACK = 600;
    private static final double DENSIDAD_ACEITE = 800;
    private static final double LITROS_POR_VASO = 0.25;
    private static final Locale ESPANOL = Locale.forLanguageTag("es");

    private static final Map<MomentoComida, Double> REPARTO = new EnumMap<>(Map.of(
            MomentoComida.DESAYUNO, 0.25,
            MomentoComida.ALMUERZO, 0.35,
            MomentoComida.CENA, 0.25,
            MomentoComida.SNACK, 0.15));

    /** Peso relativo de cada grupo dentro de las calorias de una comida. */
    private static final Map<String, Double> PESO_CALORICO = Map.of(
            "PROTEINA", 0.40,
            "LACTEO", 0.30,
            "CARBOHIDRATO", 0.30,
            "FRUTA", 0.15,
            "GRASA", 0.12,
            "VERDURA", 0.08);

    private final Random azar;

    public GeneradorPlanNutricionalPorReglas() {
        this(new Random());
    }

    GeneradorPlanNutricionalPorReglas(Random azar) {
        this.azar = azar;
    }

    @Override
    public int caloriasObjetivo(SolicitudNutricion solicitud) {
        double ajusteSexo = switch (solicitud.sexo()) {
            case MASCULINO -> 5;
            case FEMENINO -> -161;
            case OTRO -> -78;
        };
        double metabolismoBasal = 10 * solicitud.pesoKg() + 6.25 * solicitud.alturaCm() - 5 * solicitud.edad() + ajusteSexo;
        double actividad = solicitud.diasPorSemana() <= 2 ? 1.375 : solicitud.diasPorSemana() <= 4 ? 1.55 : 1.725;
        double ajusteObjetivo = switch (solicitud.objetivo()) {
            case "PERDER_PESO" -> 0.85;
            case "GANAR_MASA" -> 1.10;
            default -> 1.0;
        };
        long calorias = Math.round(metabolismoBasal * actividad * ajusteObjetivo / 10.0) * 10;
        return (int) Math.max(CALORIAS_MINIMAS, calorias);
    }

    @Override
    public List<AlimentoPlanificado> generar(SolicitudNutricion solicitud, List<Alimento> catalogo) {
        int calorias = caloriasObjetivo(solicitud);
        List<Alimento> compatibles = catalogo.stream()
                .filter(a -> !a.esIncompatibleCon(solicitud.restriccionIds()))
                .sorted(Comparator.comparing(Alimento::getId))
                .toList();

        Map<String, Integer> desplazamientos = new HashMap<>();
        Set<Long> usados = new HashSet<>();
        List<AlimentoPlanificado> plan = new ArrayList<>();

        for (MomentoComida momento : MomentoComida.values()) {
            List<Alimento> elegidos = new ArrayList<>();
            for (List<String> opciones : composicion(momento, solicitud.objetivo())) {
                Alimento alimento = elegir(opciones, momento, compatibles, usados, desplazamientos);
                if (alimento != null) {
                    elegidos.add(alimento);
                    usados.add(alimento.getId());
                }
            }

            double caloriasComida = calorias * REPARTO.get(momento);
            double pesoTotal = elegidos.stream().mapToDouble(a -> pesoCalorico(a.getGrupo())).sum();
            for (Alimento alimento : elegidos) {
                double caloriasAlimento = caloriasComida * pesoCalorico(alimento.getGrupo()) / pesoTotal;
                plan.add(new AlimentoPlanificado(alimento, momento, gramosPara(alimento, caloriasAlimento)));
            }
        }
        return plan;
    }

    @Override
    public List<Alimento> alternativas(Alimento actual, MomentoComida momento, Set<Long> excluidos,
                                       SolicitudNutricion solicitud, List<Alimento> catalogo) {
        return preferirMomento(catalogo.stream()
                .filter(a -> a.getGrupo().equals(actual.getGrupo()))
                .filter(a -> !a.getId().equals(actual.getId()) && !excluidos.contains(a.getId()))
                .filter(a -> !a.esIncompatibleCon(solicitud.restriccionIds()))
                .sorted(Comparator.comparing(Alimento::getId))
                .toList(), momento);
    }

    @Override
    public double gramosEquivalentes(Alimento actual, double gramosActuales, Alimento nuevo) {
        double calorias = gramosActuales * actual.getCaloriasPor100g() / 100.0;
        return gramosPara(nuevo, calorias);
    }

    @Override
    public Receta receta(MomentoComida momento, List<AlimentoPlanificado> alimentos) {
        // El aceite se usa como alino: aparece en los ingredientes pero no en el nombre del plato.
        List<String> principales = alimentos.stream()
                .map(AlimentoPlanificado::alimento)
                .filter(a -> a.getCaloriasPor100g() <= DENSIDAD_ACEITE)
                .map(a -> a.getNombre().toLowerCase(ESPANOL))
                .toList();

        List<String> ingredientes = alimentos.stream()
                .map(a -> gramosLegibles(a.gramos()) + " g de " + a.alimento().getNombre().toLowerCase(ESPANOL))
                .toList();

        List<String> pasos = new ArrayList<>();
        alimentos.stream()
                .map(a -> a.alimento().getPreparacion())
                .filter(p -> p != null && !p.isBlank())
                .forEach(pasos::add);
        pasos.add(switch (momento) {
            case DESAYUNO -> "Sirve todo en un plato o bowl y acompaña con la bebida sugerida.";
            case ALMUERZO -> "Sirve la proteína junto al acompañamiento y las verduras, y agrega la grasa saludable al final.";
            case CENA -> "Arma el plato con la proteína y las verduras. Procura cenar al menos 2 horas antes de dormir.";
            case SNACK -> "Consúmelo entre comidas o de 1 a 2 horas antes de entrenar.";
        });

        String bebida = switch (momento) {
            case DESAYUNO -> "Café o infusión sin azúcar y un vaso de agua.";
            case ALMUERZO -> "Un vaso de agua o limonada natural sin azúcar.";
            case CENA -> "Agua o una infusión sin azúcar, como manzanilla o hierbabuena.";
            case SNACK -> "Un vaso de agua.";
        };

        return new Receta(titulo(principales), ingredientes, pasos, bebida);
    }

    @Override
    public Hidratacion hidratacion(SolicitudNutricion solicitud) {
        // Referencia de 35 ml por kg de peso, mas 500 ml por cada hora de entrenamiento.
        double litrosDescanso = redondearDecimal(solicitud.pesoKg() * 0.035);
        double litrosEntrenamiento = redondearDecimal(solicitud.pesoKg() * 0.035 + solicitud.minutosPorSesion() / 60.0 * 0.5);
        int vasos = (int) Math.ceil(litrosEntrenamiento / LITROS_POR_VASO);

        List<String> recomendaciones = new ArrayList<>(List.of(
                "Bebe un vaso de agua al despertar.",
                "Toma entre 400 y 600 ml de agua unas 2 horas antes de entrenar.",
                "Durante el entrenamiento bebe pequeños sorbos cada 15 a 20 minutos.",
                "Al terminar de entrenar, repón líquidos con 1 o 2 vasos de agua."));

        switch (solicitud.objetivo()) {
            case "PERDER_PESO" -> recomendaciones.add("Un vaso de agua antes de cada comida ayuda a controlar el apetito.");
            case "GANAR_MASA" -> recomendaciones.add("Después de entrenar puedes tomar un batido de fruta con "
                    + (evitaLacteos(solicitud) ? "bebida de soya sin azúcar." : "leche descremada."));
            case "MEJORAR_RESISTENCIA" -> recomendaciones.add(
                    "En sesiones de más de 60 minutos puedes usar una bebida hidratante con electrolitos.");
            default -> recomendaciones.add("Las infusiones sin azúcar también cuentan para tu hidratación diaria.");
        }
        if (evitaLacteos(solicitud)) {
            recomendaciones.add("Si usas bebidas vegetales, elige versiones sin azúcar y fortificadas con calcio.");
        }

        List<String> evitar = new ArrayList<>(List.of(
                "Gaseosas y jugos con azúcar añadida.",
                "Bebidas energéticas.",
                "Alcohol, especialmente el día que entrenas."));
        if (solicitud.restriccionCodigos().contains("HIPERTENSION")) {
            evitar.add("Exceso de café y bebidas con cafeína, por tu hipertensión.");
        }

        return new Hidratacion(litrosEntrenamiento, litrosDescanso, vasos, recomendaciones, evitar);
    }

    private static boolean evitaLacteos(SolicitudNutricion solicitud) {
        return solicitud.restriccionCodigos().contains("INTOLERANCIA_LACTOSA")
                || solicitud.restriccionCodigos().contains("VEGANO");
    }

    /** "pechuga de pollo", "arroz integral", "brocoli" -> "Pechuga de pollo con arroz integral y brocoli". */
    static String titulo(List<String> nombres) {
        if (nombres.isEmpty()) {
            return "Plato del día";
        }
        String primero = nombres.get(0);
        String titulo = Character.toUpperCase(primero.charAt(0)) + primero.substring(1);
        List<String> resto = nombres.subList(1, nombres.size());
        if (resto.isEmpty()) {
            return titulo;
        }
        if (resto.size() == 1) {
            return titulo + " con " + resto.get(0);
        }
        return titulo + " con " + String.join(", ", resto.subList(0, resto.size() - 1)) + " y " + resto.get(resto.size() - 1);
    }

    private static String gramosLegibles(double gramos) {
        return gramos % 1 == 0 ? String.valueOf((int) gramos) : String.valueOf(gramos);
    }

    private static double redondearDecimal(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    /** Grupos de alimentos de cada comida. Cada posicion lista alternativas en orden de preferencia. */
    static List<List<String>> composicion(MomentoComida momento, String objetivo) {
        return switch (momento) {
            case DESAYUNO -> List.of(List.of("LACTEO", "PROTEINA"), List.of("CARBOHIDRATO"), List.of("FRUTA"));
            case ALMUERZO -> List.of(List.of("PROTEINA"), List.of("CARBOHIDRATO"), List.of("VERDURA"), List.of("GRASA"));
            case CENA -> "GANAR_MASA".equals(objetivo) || "MEJORAR_RESISTENCIA".equals(objetivo)
                    ? List.of(List.of("PROTEINA"), List.of("VERDURA"), List.of("CARBOHIDRATO"))
                    : List.of(List.of("PROTEINA"), List.of("VERDURA"), List.of("GRASA"));
            case SNACK -> List.of(List.of("FRUTA"), List.of("LACTEO", "GRASA"));
        };
    }

    private Alimento elegir(List<String> grupos, MomentoComida momento, List<Alimento> compatibles,
                            Set<Long> usados, Map<String, Integer> desplazamientos) {
        for (String grupo : grupos) {
            List<Alimento> delGrupo = preferirMomento(compatibles.stream()
                    .filter(a -> a.getGrupo().equals(grupo))
                    .filter(a -> momento != MomentoComida.SNACK || a.getCaloriasPor100g() <= DENSIDAD_MAXIMA_SNACK)
                    .toList(), momento);
            if (delGrupo.isEmpty()) {
                continue;
            }
            List<Alimento> sinRepetir = delGrupo.stream().filter(a -> !usados.contains(a.getId())).toList();
            List<Alimento> opciones = sinRepetir.isEmpty() ? delGrupo : sinRepetir;
            int desplazamiento = desplazamientos.computeIfAbsent(grupo, g -> azar.nextInt(1000));
            return opciones.get(Math.floorMod(desplazamiento + momento.ordinal(), opciones.size()));
        }
        return null;
    }

    /**
     * Deja solo los alimentos recomendados para ese momento del dia (por ejemplo, avena en el desayuno).
     * Si las restricciones no dejan ninguno, conserva todos para que la comida no quede incompleta.
     */
    static List<Alimento> preferirMomento(List<Alimento> alimentos, MomentoComida momento) {
        List<Alimento> adecuados = alimentos.stream().filter(a -> a.esAdecuadoPara(momento)).toList();
        return adecuados.isEmpty() ? alimentos : adecuados;
    }

    private static double pesoCalorico(String grupo) {
        return PESO_CALORICO.getOrDefault(grupo, 0.2);
    }

    /** Gramos redondeados a multiplos de 5 y limitados a porciones razonables segun el grupo. */
    static double gramosPara(Alimento alimento, double calorias) {
        double gramos = calorias * 100.0 / alimento.getCaloriasPor100g();
        double[] limites = switch (alimento.getGrupo()) {
            case "VERDURA", "FRUTA" -> new double[]{80, 250};
            case "GRASA" -> new double[]{10, 40};
            default -> new double[]{30, 250};
        };
        double limitado = Math.max(limites[0], Math.min(limites[1], gramos));
        return Math.round(limitado / 5.0) * 5.0;
    }
}
