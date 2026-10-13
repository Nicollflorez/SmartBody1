package com.smartbody.planes.service.reglas;

import com.smartbody.planes.entity.Alimento;
import com.smartbody.planes.entity.Ejercicio;
import com.smartbody.planes.entity.NivelExperiencia;
import com.smartbody.planes.entity.Restriccion;
import com.smartbody.planes.entity.TipoEjercicio;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Construye catalogos parecidos a los datos iniciales sin necesidad de base de datos. */
final class CatalogoDePrueba {

    static final long LESION_RODILLA = 1L;
    static final long INTOLERANCIA_LACTOSA = 7L;
    static final long VEGANO = 12L;

    private CatalogoDePrueba() {
    }

    static Restriccion restriccion(long id) {
        Restriccion restriccion = new Restriccion();
        ReflectionTestUtils.setField(restriccion, "id", id);
        return restriccion;
    }

    static Ejercicio ejercicio(long id, String nombre, String grupo, TipoEjercicio tipo, NivelExperiencia nivel,
                               int duracionSeg, Long... contraindicaciones) {
        Ejercicio ejercicio = new Ejercicio();
        ReflectionTestUtils.setField(ejercicio, "id", id);
        ReflectionTestUtils.setField(ejercicio, "nombre", nombre);
        ReflectionTestUtils.setField(ejercicio, "grupoMuscular", grupo);
        ReflectionTestUtils.setField(ejercicio, "tipo", tipo);
        ReflectionTestUtils.setField(ejercicio, "nivelMinimo", nivel);
        ReflectionTestUtils.setField(ejercicio, "duracionEstimadaSeg", duracionSeg);
        for (Long restriccionId : contraindicaciones) {
            ejercicio.getContraindicaciones().add(restriccion(restriccionId));
        }
        return ejercicio;
    }

    static List<Ejercicio> ejercicios() {
        TipoEjercicio f = TipoEjercicio.FUERZA;
        NivelExperiencia p = NivelExperiencia.PRINCIPIANTE;
        NivelExperiencia i = NivelExperiencia.INTERMEDIO;
        List<Ejercicio> lista = new ArrayList<>();
        lista.add(ejercicio(1, "Sentadilla libre", "PIERNAS", f, p, 240, LESION_RODILLA));
        lista.add(ejercicio(2, "Prensa de piernas", "PIERNAS", f, p, 240));
        lista.add(ejercicio(3, "Zancadas", "PIERNAS", f, i, 240, LESION_RODILLA));
        lista.add(ejercicio(4, "Puente de glúteos", "PIERNAS", f, p, 180));
        lista.add(ejercicio(5, "Flexiones de pecho", "PECHO", f, p, 210));
        lista.add(ejercicio(6, "Press banca", "PECHO", f, i, 270));
        lista.add(ejercicio(7, "Remo con mancuerna", "ESPALDA", f, p, 240));
        lista.add(ejercicio(8, "Jalón al pecho", "ESPALDA", f, p, 210));
        lista.add(ejercicio(9, "Elevaciones laterales", "HOMBROS", f, p, 180));
        lista.add(ejercicio(10, "Press militar", "HOMBROS", f, i, 240));
        lista.add(ejercicio(11, "Curl de bíceps", "BRAZOS", f, p, 180));
        lista.add(ejercicio(12, "Plancha abdominal", "CORE", f, p, 120));
        lista.add(ejercicio(13, "Crunch abdominal", "CORE", f, p, 150));
        lista.add(ejercicio(14, "Burpees", "CUERPO_COMPLETO", TipoEjercicio.CARDIO, i, 180, LESION_RODILLA));
        lista.add(ejercicio(15, "Bicicleta estática", "CARDIO", TipoEjercicio.CARDIO, p, 600));
        lista.add(ejercicio(16, "Caminata rápida", "CARDIO", TipoEjercicio.CARDIO, p, 600));
        lista.add(ejercicio(17, "Estiramiento general", "MOVILIDAD", TipoEjercicio.MOVILIDAD, p, 180));
        return lista;
    }

    static Alimento alimento(long id, String nombre, String grupo, double calorias, Long... incompatibilidades) {
        Alimento alimento = new Alimento();
        ReflectionTestUtils.setField(alimento, "id", id);
        ReflectionTestUtils.setField(alimento, "nombre", nombre);
        ReflectionTestUtils.setField(alimento, "grupo", grupo);
        ReflectionTestUtils.setField(alimento, "caloriasPor100g", calorias);
        ReflectionTestUtils.setField(alimento, "proteinaG", 10.0);
        ReflectionTestUtils.setField(alimento, "carbohidratosG", 10.0);
        ReflectionTestUtils.setField(alimento, "grasasG", 5.0);
        ReflectionTestUtils.setField(alimento, "preparacion", "Prepara " + nombre.toLowerCase() + ".");
        for (Long restriccionId : incompatibilidades) {
            alimento.getIncompatibilidades().add(restriccion(restriccionId));
        }
        return alimento;
    }

    static Alimento conMomentos(Alimento alimento, String momentos) {
        ReflectionTestUtils.setField(alimento, "momentos", momentos);
        return alimento;
    }

    static List<Alimento> alimentos() {
        return List.of(
                alimento(1, "Pechuga de pollo", "PROTEINA", 165, VEGANO),
                alimento(2, "Huevo", "PROTEINA", 155, VEGANO),
                alimento(3, "Lentejas", "PROTEINA", 116),
                alimento(4, "Tofu", "PROTEINA", 144),
                conMomentos(alimento(5, "Yogur griego", "LACTEO", 59, INTOLERANCIA_LACTOSA, VEGANO), "DESAYUNO,SNACK"),
                alimento(6, "Queso fresco", "LACTEO", 264, INTOLERANCIA_LACTOSA, VEGANO),
                conMomentos(alimento(7, "Arroz integral", "CARBOHIDRATO", 111), "ALMUERZO,CENA"),
                conMomentos(alimento(8, "Avena", "CARBOHIDRATO", 389), "DESAYUNO,SNACK"),
                alimento(9, "Papa", "CARBOHIDRATO", 77),
                alimento(10, "Plátano", "FRUTA", 89),
                alimento(11, "Manzana", "FRUTA", 52),
                alimento(12, "Brócoli", "VERDURA", 34),
                alimento(13, "Espinaca", "VERDURA", 23),
                alimento(14, "Aguacate", "GRASA", 160),
                alimento(15, "Aceite de oliva", "GRASA", 884),
                alimento(16, "Almendras", "GRASA", 579));
    }

    static Set<Long> sinRestricciones() {
        return Set.of();
    }
}
