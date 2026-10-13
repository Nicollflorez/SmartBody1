package com.smartbody.dto;

import com.smartbody.entity.Rutina;
import com.smartbody.entity.RutinaEjercicio;
import com.smartbody.entity.TipoEjercicio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** RF07 - rutina organizada por dias de entrenamiento. */
public record RutinaResponse(Long id,
                             String nombre,
                             ObjetivoResponse objetivo,
                             int diasPorSemana,
                             int minutosPorSesion,
                             boolean activa,
                             LocalDateTime generadaEn,
                             List<Dia> dias) {

    public record Dia(int numero, String enfoque, int minutosEstimados, List<Item> ejercicios) {
    }

    /** Ejercicio dentro de la rutina con su prescripcion. {@code id} identifica el item, no el ejercicio. */
    public record Item(Long id,
                       Long ejercicioId,
                       String nombre,
                       String grupoMuscular,
                       TipoEjercicio tipo,
                       int series,
                       Integer repeticiones,
                       Integer duracionSeg,
                       int descansoSeg) {
    }

    private static final Set<String> GRUPOS_CUERPO_COMPLETO = Set.of("PIERNAS", "PECHO", "ESPALDA");

    public static RutinaResponse desde(Rutina rutina) {
        Map<Integer, List<RutinaEjercicio>> porDia = new LinkedHashMap<>();
        for (RutinaEjercicio item : rutina.getEjercicios()) {
            porDia.computeIfAbsent(item.getDiaNumero(), d -> new ArrayList<>()).add(item);
        }

        List<Dia> dias = porDia.entrySet().stream()
                .map(entrada -> new Dia(
                        entrada.getKey(),
                        enfoque(entrada.getValue()),
                        (int) Math.ceil(entrada.getValue().stream().mapToInt(RutinaEjercicio::segundosEstimados).sum() / 60.0),
                        entrada.getValue().stream().map(RutinaResponse::item).toList()))
                .toList();

        return new RutinaResponse(rutina.getId(), rutina.getNombre(), ObjetivoResponse.desde(rutina.getObjetivo()),
                rutina.getDiasPorSemana(), rutina.getMinutosPorSesion(), rutina.isActiva(), rutina.getGeneradaEn(), dias);
    }

    private static Item item(RutinaEjercicio item) {
        return new Item(item.getId(), item.getEjercicio().getId(), item.getEjercicio().getNombre(),
                item.getEjercicio().getGrupoMuscular(), item.getEjercicio().getTipo(), item.getSeries(),
                item.getRepeticiones(), item.getDuracionSeg(), item.getDescansoSeg());
    }

    /** Descripcion corta del dia a partir de los grupos musculares trabajados. */
    private static String enfoque(List<RutinaEjercicio> items) {
        Set<String> grupos = items.stream()
                .filter(i -> i.getEjercicio().getTipo() == TipoEjercicio.FUERZA)
                .map(i -> i.getEjercicio().getGrupoMuscular())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        if (grupos.isEmpty()) {
            return "Cardio y movilidad";
        }
        if (grupos.containsAll(GRUPOS_CUERPO_COMPLETO)) {
            return "Cuerpo completo";
        }
        return grupos.stream().map(RutinaResponse::legible).collect(Collectors.joining(" + "));
    }

    private static String legible(String codigo) {
        String texto = codigo.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
