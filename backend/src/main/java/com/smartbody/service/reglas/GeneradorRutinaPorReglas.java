package com.smartbody.service.reglas;

import com.smartbody.entity.Ejercicio;
import com.smartbody.entity.NivelExperiencia;
import com.smartbody.entity.RutinaEjercicio;
import com.smartbody.entity.TipoEjercicio;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Motor de reglas de Smartbody (RF06):
 * objetivo + nivel + tiempo + dias + restricciones -> rutina personalizada.
 *
 * <ul>
 *   <li>El objetivo define series, repeticiones, descanso y la proporcion de cardio.</li>
 *   <li>El nivel limita los ejercicios disponibles y ajusta el numero de series.</li>
 *   <li>Los dias definen la division: cuerpo completo, superior/inferior o empuje/traccion/pierna.</li>
 *   <li>El tiempo por sesion define cuantos ejercicios caben en cada dia.</li>
 *   <li>Los ejercicios contraindicados para las restricciones de salud nunca se incluyen.</li>
 * </ul>
 */
@Component
public class GeneradorRutinaPorReglas implements GeneradorRutina {

    static final int SEGUNDOS_BLOQUE_MOVILIDAD = 180;
    static final int MAXIMO_EJERCICIOS_FUERZA = 8;
    private static final int SESION_MINIMA_CON_MOVILIDAD_SEG = 30 * 60;
    private static final int MINIMO_SEGUNDOS_CARDIO = 300;
    private static final int TRABAJO_INTERVALO_SEG = 40;
    private static final int DESCANSO_INTERVALO_SEG = 20;
    private static final int MAXIMO_INTERVALOS = 15;

    private static final List<String> CUERPO_COMPLETO = List.of("PIERNAS", "ESPALDA", "PECHO", "HOMBROS", "CORE", "BRAZOS");
    private static final List<String> TREN_SUPERIOR = List.of("PECHO", "ESPALDA", "HOMBROS", "BRAZOS");
    private static final List<String> TREN_INFERIOR = List.of("PIERNAS", "CORE");
    private static final List<String> EMPUJE = List.of("PECHO", "HOMBROS", "BRAZOS");
    private static final List<String> TRACCION = List.of("ESPALDA", "BRAZOS", "CORE");
    private static final List<String> PIERNA = List.of("PIERNAS", "CORE");

    /** Variables de entrenamiento que dependen del objetivo y del nivel. */
    record Parametros(int series, int repeticiones, int descansoSeg, double proporcionCardio) {
    }

    private final Random azar;

    public GeneradorRutinaPorReglas() {
        this(new Random());
    }

    GeneradorRutinaPorReglas(Random azar) {
        this.azar = azar;
    }

    @Override
    public List<EjercicioPlanificado> generar(SolicitudRutina solicitud, List<Ejercicio> catalogo) {
        Parametros parametros = parametrosPara(solicitud.objetivo(), solicitud.nivel());
        List<Ejercicio> compatibles = catalogo.stream()
                .filter(e -> esCompatible(e, solicitud))
                .sorted(Comparator.comparing(Ejercicio::getId))
                .toList();

        Map<String, List<Ejercicio>> fuerzaPorGrupo = compatibles.stream()
                .filter(e -> e.getTipo() == TipoEjercicio.FUERZA)
                .collect(Collectors.groupingBy(Ejercicio::getGrupoMuscular, LinkedHashMap::new, Collectors.toList()));
        List<Ejercicio> cardio = delTipo(compatibles, TipoEjercicio.CARDIO);
        List<Ejercicio> movilidad = delTipo(compatibles, TipoEjercicio.MOVILIDAD);

        // Desplazamientos aleatorios para que dos rutinas generadas no sean identicas.
        Map<String, Integer> desplazamientos = new HashMap<>();
        fuerzaPorGrupo.keySet().forEach(grupo -> desplazamientos.put(grupo, azar.nextInt(1000)));
        int desplazamientoCardio = azar.nextInt(1000);
        int desplazamientoMovilidad = azar.nextInt(1000);

        int totalSeg = solicitud.minutosPorSesion() * 60;
        int movilidadSeg = totalSeg >= SESION_MINIMA_CON_MOVILIDAD_SEG && !movilidad.isEmpty() ? SEGUNDOS_BLOQUE_MOVILIDAD : 0;
        int cardioSeg = 0;
        if (!cardio.isEmpty()) {
            if (fuerzaPorGrupo.isEmpty()) {
                cardioSeg = totalSeg - movilidadSeg;
            } else if (parametros.proporcionCardio() > 0) {
                cardioSeg = Math.max(MINIMO_SEGUNDOS_CARDIO, redondearAMinutos(totalSeg * parametros.proporcionCardio()));
            }
        }
        int fuerzaSeg = totalSeg - movilidadSeg - cardioSeg;
        int cantidadFuerza = fuerzaPorGrupo.isEmpty() ? 0
                : Math.max(1, Math.min(MAXIMO_EJERCICIOS_FUERZA, fuerzaSeg / segundosPorEjercicioDeFuerza(parametros)));

        List<EjercicioPlanificado> plan = new ArrayList<>();
        for (int dia = 1; dia <= solicitud.diasPorSemana(); dia++) {
            List<String> grupos = gruposDelDia(solicitud.diasPorSemana(), dia);
            Set<Long> usadosEnElDia = new HashSet<>();
            int orden = 1;

            for (int indice = 0; indice < cantidadFuerza; indice++) {
                Ejercicio elegido = elegirFuerza(grupos, indice, dia, fuerzaPorGrupo, desplazamientos, usadosEnElDia);
                if (elegido == null) {
                    break;
                }
                usadosEnElDia.add(elegido.getId());
                plan.add(prescribir(elegido, dia, orden++, parametros, solicitud.nivel(), 0));
            }
            if (cardioSeg > 0) {
                Ejercicio elegido = cardio.get(Math.floorMod(desplazamientoCardio + dia, cardio.size()));
                plan.add(prescribir(elegido, dia, orden++, parametros, solicitud.nivel(), cardioSeg));
            }
            if (movilidadSeg > 0) {
                Ejercicio elegido = movilidad.get(Math.floorMod(desplazamientoMovilidad + dia, movilidad.size()));
                plan.add(prescribir(elegido, dia, orden, parametros, solicitud.nivel(), movilidadSeg));
            }
        }
        return plan;
    }

    @Override
    public List<Ejercicio> alternativas(Ejercicio actual, Set<Long> excluidos, SolicitudRutina solicitud, List<Ejercicio> catalogo) {
        List<Ejercicio> mismoTipo = catalogo.stream()
                .filter(e -> e.getTipo() == actual.getTipo())
                .filter(e -> !e.getId().equals(actual.getId()) && !excluidos.contains(e.getId()))
                .filter(e -> esCompatible(e, solicitud))
                .sorted(Comparator.comparing(Ejercicio::getId))
                .toList();
        List<Ejercicio> mismoGrupo = mismoTipo.stream()
                .filter(e -> e.getGrupoMuscular().equals(actual.getGrupoMuscular()))
                .toList();
        return mismoGrupo.isEmpty() ? mismoTipo : mismoGrupo;
    }

    @Override
    public EjercicioPlanificado sustituir(EjercicioPlanificado actual, Ejercicio nuevo, SolicitudRutina solicitud) {
        Parametros parametros = parametrosPara(solicitud.objetivo(), solicitud.nivel());
        return prescribir(nuevo, actual.dia(), actual.orden(), parametros, solicitud.nivel(), actual.segundosEstimados());
    }

    static Parametros parametrosPara(String objetivo, NivelExperiencia nivel) {
        Parametros base = switch (objetivo) {
            case "GANAR_MASA" -> new Parametros(4, 10, 90, 0.0);
            case "PERDER_PESO" -> new Parametros(3, 15, 45, 0.35);
            case "MEJORAR_RESISTENCIA" -> new Parametros(3, 18, 30, 0.40);
            case "MANTENER_PESO" -> new Parametros(3, 12, 60, 0.20);
            default -> new Parametros(3, 12, 60, 0.25);
        };
        int series = switch (nivel) {
            case PRINCIPIANTE -> Math.min(base.series(), 3);
            case INTERMEDIO -> base.series();
            case AVANZADO -> Math.min(base.series() + 1, 5);
        };
        return new Parametros(series, base.repeticiones(), base.descansoSeg(), base.proporcionCardio());
    }

    static List<String> gruposDelDia(int diasPorSemana, int dia) {
        if (diasPorSemana <= 3) {
            return CUERPO_COMPLETO;
        }
        if (diasPorSemana == 4) {
            return dia % 2 == 1 ? TREN_SUPERIOR : TREN_INFERIOR;
        }
        return switch (dia % 3) {
            case 1 -> EMPUJE;
            case 2 -> TRACCION;
            default -> PIERNA;
        };
    }

    private static boolean esCompatible(Ejercicio ejercicio, SolicitudRutina solicitud) {
        return ejercicio.getNivelMinimo().ordinal() <= solicitud.nivel().ordinal()
                && !ejercicio.estaContraindicadoPara(solicitud.restriccionIds());
    }

    private Ejercicio elegirFuerza(List<String> grupos, int indice, int dia, Map<String, List<Ejercicio>> porGrupo,
                                   Map<String, Integer> desplazamientos, Set<Long> usados) {
        for (int intento = 0; intento < grupos.size(); intento++) {
            String grupo = grupos.get((indice + intento) % grupos.size());
            List<Ejercicio> opciones = porGrupo.getOrDefault(grupo, List.of()).stream()
                    .filter(e -> !usados.contains(e.getId()))
                    .toList();
            if (!opciones.isEmpty()) {
                return opciones.get(Math.floorMod(desplazamientos.get(grupo) + dia + indice, opciones.size()));
            }
        }
        return null;
    }

    private static EjercicioPlanificado prescribir(Ejercicio ejercicio, int dia, int orden, Parametros parametros,
                                                   NivelExperiencia nivel, int segundosBloque) {
        return switch (ejercicio.getTipo()) {
            case FUERZA -> ejercicio.esIsometrico()
                    ? new EjercicioPlanificado(ejercicio, dia, orden, parametros.series(), null,
                            segundosIsometrico(nivel), parametros.descansoSeg())
                    : new EjercicioPlanificado(ejercicio, dia, orden, parametros.series(), parametros.repeticiones(),
                            null, parametros.descansoSeg());
            case CARDIO -> ejercicio.esCardioContinuo()
                    ? new EjercicioPlanificado(ejercicio, dia, orden, 1, null, segundosBloque, 0)
                    : new EjercicioPlanificado(ejercicio, dia, orden, intervalosPara(segundosBloque), null,
                            TRABAJO_INTERVALO_SEG, DESCANSO_INTERVALO_SEG);
            case MOVILIDAD -> new EjercicioPlanificado(ejercicio, dia, orden, 1, null, SEGUNDOS_BLOQUE_MOVILIDAD, 0);
        };
    }

    private static int intervalosPara(int segundos) {
        int intervalos = segundos / (TRABAJO_INTERVALO_SEG + DESCANSO_INTERVALO_SEG);
        return Math.max(1, Math.min(MAXIMO_INTERVALOS, intervalos));
    }

    private static int segundosIsometrico(NivelExperiencia nivel) {
        return switch (nivel) {
            case PRINCIPIANTE -> 30;
            case INTERMEDIO -> 45;
            case AVANZADO -> 60;
        };
    }

    private static int segundosPorEjercicioDeFuerza(Parametros parametros) {
        return RutinaEjercicio.estimarSegundos(parametros.series(), null, parametros.descansoSeg());
    }

    private static int redondearAMinutos(double segundos) {
        return (int) Math.round(segundos / 60.0) * 60;
    }

    private static List<Ejercicio> delTipo(List<Ejercicio> ejercicios, TipoEjercicio tipo) {
        return ejercicios.stream().filter(e -> e.getTipo() == tipo).toList();
    }
}
