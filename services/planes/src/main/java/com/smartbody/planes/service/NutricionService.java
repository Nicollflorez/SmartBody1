package com.smartbody.planes.service;

import com.smartbody.comun.error.ApiException;
import com.smartbody.planes.cliente.PerfilCliente;
import com.smartbody.planes.cliente.PerfilRemoto;
import com.smartbody.planes.dto.AlimentoResponse;
import com.smartbody.planes.dto.PlanNutricionalResponse;
import com.smartbody.planes.dto.SustitucionRequest;
import com.smartbody.planes.entity.Alimento;
import com.smartbody.planes.entity.MomentoComida;
import com.smartbody.planes.entity.Objetivo;
import com.smartbody.planes.entity.PlanAlimento;
import com.smartbody.planes.entity.PlanNutricional;
import com.smartbody.planes.entity.Sexo;
import com.smartbody.planes.repository.AlimentoRepository;
import com.smartbody.planes.repository.ObjetivoRepository;
import com.smartbody.planes.repository.PlanNutricionalRepository;
import com.smartbody.planes.service.reglas.AlimentoPlanificado;
import com.smartbody.planes.service.reglas.GeneradorPlanNutricional;
import com.smartbody.planes.service.reglas.Hidratacion;
import com.smartbody.planes.service.reglas.Receta;
import com.smartbody.planes.service.reglas.SolicitudNutricion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Plan nutricional: generacion, consulta y sustitucion de alimentos (RF06, RF07, RF09). */
@Service
public class NutricionService {

    private final ObjetivoRepository objetivoRepository;
    private final AlimentoRepository alimentoRepository;
    private final PlanNutricionalRepository planRepository;
    private final GeneradorPlanNutricional generador;

    public NutricionService(ObjetivoRepository objetivoRepository,
                            AlimentoRepository alimentoRepository,
                            PlanNutricionalRepository planRepository,
                            GeneradorPlanNutricional generador) {
        this.objetivoRepository = objetivoRepository;
        this.alimentoRepository = alimentoRepository;
        this.planRepository = planRepository;
        this.generador = generador;
    }

    @Transactional
    public PlanNutricionalResponse generar(Long usuarioId, PerfilRemoto perfil) {
        PerfilCliente.exigirCompleto(perfil);
        Objetivo objetivo = objetivoRepository.findById(perfil.objetivo().id())
                .orElseThrow(() -> ApiException.solicitudInvalida("El objetivo del perfil no existe en el catálogo."));
        SolicitudNutricion solicitud = solicitudDe(perfil);
        List<Alimento> catalogo = alimentoRepository.findAllConIncompatibilidades();
        List<AlimentoPlanificado> planificados = generador.generar(solicitud, catalogo);
        if (planificados.isEmpty()) {
            throw ApiException.solicitudInvalida(
                    "No encontramos alimentos compatibles con tus restricciones. Revisa tu perfil.");
        }

        planRepository.desactivarPlanesActivos(usuarioId);
        PlanNutricional plan = new PlanNutricional();
        plan.setUsuarioId(usuarioId);
        plan.setObjetivo(objetivo);
        plan.setCaloriasObjetivo(generador.caloriasObjetivo(solicitud));
        for (AlimentoPlanificado planificado : planificados) {
            PlanAlimento item = new PlanAlimento();
            item.setAlimento(planificado.alimento());
            item.setMomento(planificado.momento());
            item.setCantidad(planificado.gramos());
            plan.agregarAlimento(item);
        }
        return responder(planRepository.save(plan), solicitud, catalogo);
    }

    @Transactional(readOnly = true)
    public PlanNutricionalResponse activo(Long usuarioId, PerfilRemoto perfil) {
        PlanNutricional plan = planRepository.findFirstByUsuarioIdAndActivoTrueOrderByGeneradoEnDesc(usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("Aún no tienes un plan nutricional generado."));
        return responder(plan, solicitudDe(perfil), alimentoRepository.findAllConIncompatibilidades());
    }

    /** Recomendacion de agua y bebidas del usuario (requiere perfil completo). */
    public Hidratacion hidratacion(PerfilRemoto perfil) {
        return generador.hidratacion(solicitudDe(perfil));
    }

    @Transactional(readOnly = true)
    public List<AlimentoResponse> alternativas(Long usuarioId, PerfilRemoto perfil, Long planId, Long itemId) {
        PlanNutricional plan = planDelUsuario(usuarioId, planId);
        PlanAlimento item = itemDelPlan(plan, itemId);
        return generador.alternativas(item.getAlimento(), item.getMomento(), idsDeLaMismaComida(plan, item),
                        solicitudDe(perfil), alimentoRepository.findAllConIncompatibilidades())
                .stream().map(AlimentoResponse::desde).toList();
    }

    @Transactional
    public PlanNutricionalResponse sustituir(Long usuarioId, PerfilRemoto perfil, Long planId, SustitucionRequest peticion) {
        PlanNutricional plan = planDelUsuario(usuarioId, planId);
        PlanAlimento item = itemDelPlan(plan, peticion.itemId());
        SolicitudNutricion solicitud = solicitudDe(perfil);
        List<Alimento> catalogo = alimentoRepository.findAllConIncompatibilidades();
        List<Alimento> alternativas = generador.alternativas(item.getAlimento(), item.getMomento(),
                idsDeLaMismaComida(plan, item), solicitud, catalogo);
        if (alternativas.isEmpty()) {
            throw ApiException.solicitudInvalida("No hay alimentos alternativos compatibles con tus restricciones.");
        }
        Alimento nuevo = peticion.nuevoId() == null ? alternativas.get(0)
                : alternativas.stream()
                        .filter(a -> a.getId().equals(peticion.nuevoId()))
                        .findFirst()
                        .orElseThrow(() -> ApiException.solicitudInvalida(
                                "El alimento elegido no es compatible con tu plan o tus restricciones."));
        item.setCantidad(generador.gramosEquivalentes(item.getAlimento(), item.getCantidad(), nuevo));
        item.setAlimento(nuevo);
        return responder(plan, solicitud, catalogo);
    }

    /**
     * Completa la respuesta con una alternativa por alimento, la receta de cada comida y la
     * hidratacion. Las recetas se arman con los alimentos actuales, por lo que reflejan las sustituciones.
     */
    private PlanNutricionalResponse responder(PlanNutricional plan, SolicitudNutricion solicitud, List<Alimento> catalogo) {
        Map<Long, String> alternativas = new HashMap<>();
        for (PlanAlimento item : plan.getAlimentos()) {
            generador.alternativas(item.getAlimento(), item.getMomento(), idsDeLaMismaComida(plan, item), solicitud, catalogo)
                    .stream()
                    .findFirst()
                    .ifPresent(alternativa -> alternativas.put(item.getId(), alternativa.getNombre()));
        }
        Map<MomentoComida, Receta> recetas = new EnumMap<>(MomentoComida.class);
        plan.getAlimentos().stream()
                .sorted(Comparator.comparing(PlanAlimento::getId))
                .collect(Collectors.groupingBy(PlanAlimento::getMomento, LinkedHashMap::new,
                        Collectors.mapping(a -> new AlimentoPlanificado(a.getAlimento(), a.getMomento(), a.getCantidad()),
                                Collectors.toList())))
                .forEach((momento, alimentos) -> recetas.put(momento, generador.receta(momento, alimentos)));
        return PlanNutricionalResponse.desde(plan, alternativas, recetas, generador.hidratacion(solicitud));
    }

    private static SolicitudNutricion solicitudDe(PerfilRemoto perfil) {
        PerfilCliente.exigirCompleto(perfil);
        Sexo sexo = perfil.sexo() == null ? Sexo.OTRO : perfil.sexo();
        return new SolicitudNutricion(perfil.objetivo().codigo(), sexo, perfil.edad(), perfil.pesoKg(),
                perfil.alturaCm(), perfil.diasPorSemana(), perfil.minutosPorSesion(),
                perfil.idsRestricciones(), perfil.codigosRestricciones());
    }

    private static Set<Long> idsDeLaMismaComida(PlanNutricional plan, PlanAlimento item) {
        return plan.getAlimentos().stream()
                .filter(a -> a.getMomento() == item.getMomento())
                .map(a -> a.getAlimento().getId())
                .collect(Collectors.toSet());
    }

    private PlanNutricional planDelUsuario(Long usuarioId, Long planId) {
        return planRepository.findByIdAndUsuarioId(planId, usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el plan nutricional solicitado."));
    }

    private static PlanAlimento itemDelPlan(PlanNutricional plan, Long itemId) {
        return plan.getAlimentos().stream()
                .filter(a -> a.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("El alimento no pertenece a este plan."));
    }
}
