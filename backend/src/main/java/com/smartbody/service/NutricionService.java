package com.smartbody.service;

import com.smartbody.dto.AlimentoResponse;
import com.smartbody.dto.PlanNutricionalResponse;
import com.smartbody.dto.SustitucionRequest;
import com.smartbody.entity.Alimento;
import com.smartbody.entity.MomentoComida;
import com.smartbody.entity.Restriccion;
import com.smartbody.entity.Perfil;
import com.smartbody.entity.PlanAlimento;
import com.smartbody.entity.PlanNutricional;
import com.smartbody.entity.Sexo;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.AlimentoRepository;
import com.smartbody.repository.PlanNutricionalRepository;
import com.smartbody.service.reglas.AlimentoPlanificado;
import com.smartbody.service.reglas.GeneradorPlanNutricional;
import com.smartbody.service.reglas.Hidratacion;
import com.smartbody.service.reglas.Receta;
import com.smartbody.service.reglas.SolicitudNutricion;
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

    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final AlimentoRepository alimentoRepository;
    private final PlanNutricionalRepository planRepository;
    private final GeneradorPlanNutricional generador;

    public NutricionService(UsuarioService usuarioService,
                            PerfilService perfilService,
                            AlimentoRepository alimentoRepository,
                            PlanNutricionalRepository planRepository,
                            GeneradorPlanNutricional generador) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
        this.alimentoRepository = alimentoRepository;
        this.planRepository = planRepository;
        this.generador = generador;
    }

    @Transactional
    public PlanNutricionalResponse generar(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Perfil perfil = perfilService.exigirPerfilCompleto(usuario);
        SolicitudNutricion solicitud = solicitudDe(usuario);
        List<Alimento> catalogo = alimentoRepository.findAllConIncompatibilidades();

        List<AlimentoPlanificado> planificados = generador.generar(solicitud, catalogo);
        if (planificados.isEmpty()) {
            throw ApiException.solicitudInvalida(
                    "No encontramos alimentos compatibles con tus restricciones. Revisa tu perfil.");
        }

        planRepository.desactivarPlanesActivos(usuario.getId());

        PlanNutricional plan = new PlanNutricional();
        plan.setUsuario(usuario);
        plan.setObjetivo(perfil.getObjetivo());
        plan.setCaloriasObjetivo(generador.caloriasObjetivo(solicitud));
        for (AlimentoPlanificado planificado : planificados) {
            PlanAlimento item = new PlanAlimento();
            item.setAlimento(planificado.alimento());
            item.setMomento(planificado.momento());
            item.setCantidad(planificado.gramos());
            plan.agregarAlimento(item);
        }

        PlanNutricional guardado = planRepository.save(plan);
        return responder(guardado, solicitud, catalogo);
    }

    @Transactional(readOnly = true)
    public PlanNutricionalResponse activo(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        PlanNutricional plan = planRepository.findFirstByUsuarioIdAndActivoTrueOrderByGeneradoEnDesc(usuario.getId())
                .orElseThrow(() -> ApiException.noEncontrado("Aún no tienes un plan nutricional generado."));
        return responder(plan, solicitudDe(usuario), alimentoRepository.findAllConIncompatibilidades());
    }

    /** Recomendacion de agua y bebidas del usuario (requiere perfil completo). */
    @Transactional(readOnly = true)
    public Hidratacion hidratacion(String email) {
        return generador.hidratacion(solicitudDe(usuarioService.buscarPorEmail(email)));
    }

    @Transactional(readOnly = true)
    public List<AlimentoResponse> alternativas(String email, Long planId, Long itemId) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        PlanNutricional plan = planDelUsuario(usuario, planId);
        PlanAlimento item = itemDelPlan(plan, itemId);

        return generador.alternativas(item.getAlimento(), item.getMomento(), idsDeLaMismaComida(plan, item), solicitudDe(usuario),
                        alimentoRepository.findAllConIncompatibilidades())
                .stream().map(AlimentoResponse::desde).toList();
    }

    @Transactional
    public PlanNutricionalResponse sustituir(String email, Long planId, SustitucionRequest peticion) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        PlanNutricional plan = planDelUsuario(usuario, planId);
        PlanAlimento item = itemDelPlan(plan, peticion.itemId());
        SolicitudNutricion solicitud = solicitudDe(usuario);
        List<Alimento> catalogo = alimentoRepository.findAllConIncompatibilidades();

        List<Alimento> alternativas = generador.alternativas(item.getAlimento(), item.getMomento(), idsDeLaMismaComida(plan, item), solicitud, catalogo);
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
            generador.alternativas(item.getAlimento(), item.getMomento(), idsDeLaMismaComida(plan, item), solicitud, catalogo).stream()
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

    private SolicitudNutricion solicitudDe(Usuario usuario) {
        Perfil perfil = perfilService.exigirPerfilCompleto(usuario);
        Sexo sexo = perfil.getSexo() == null ? Sexo.OTRO : perfil.getSexo();
        Set<String> codigos = usuario.getRestricciones().stream().map(Restriccion::getCodigo).collect(Collectors.toSet());
        return new SolicitudNutricion(perfil.getObjetivo().getCodigo(), sexo, perfil.getEdad(), perfil.getPesoKg(),
                perfil.getAlturaCm(), perfil.getDiasPorSemana(), perfil.getMinutosPorSesion(),
                PerfilService.idsDeRestricciones(usuario), codigos);
    }

    private static Set<Long> idsDeLaMismaComida(PlanNutricional plan, PlanAlimento item) {
        return plan.getAlimentos().stream()
                .filter(a -> a.getMomento() == item.getMomento())
                .map(a -> a.getAlimento().getId())
                .collect(Collectors.toSet());
    }

    private PlanNutricional planDelUsuario(Usuario usuario, Long planId) {
        return planRepository.findByIdAndUsuarioId(planId, usuario.getId())
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el plan nutricional solicitado."));
    }

    private static PlanAlimento itemDelPlan(PlanNutricional plan, Long itemId) {
        return plan.getAlimentos().stream()
                .filter(a -> a.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("El alimento no pertenece a este plan."));
    }
}
