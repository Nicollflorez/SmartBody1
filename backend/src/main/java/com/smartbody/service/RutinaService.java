package com.smartbody.service;

import com.smartbody.dto.EjercicioResponse;
import com.smartbody.dto.RutinaResponse;
import com.smartbody.dto.RutinaResumenResponse;
import com.smartbody.dto.SustitucionRequest;
import com.smartbody.entity.Ejercicio;
import com.smartbody.entity.Perfil;
import com.smartbody.entity.Rutina;
import com.smartbody.entity.RutinaEjercicio;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.EjercicioRepository;
import com.smartbody.repository.RutinaRepository;
import com.smartbody.service.reglas.EjercicioPlanificado;
import com.smartbody.service.reglas.GeneradorRutina;
import com.smartbody.service.reglas.SolicitudRutina;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** RF06, RF07 y RF09: generacion, consulta y sustitucion de ejercicios de la rutina. */
@Service
public class RutinaService {

    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final EjercicioRepository ejercicioRepository;
    private final RutinaRepository rutinaRepository;
    private final GeneradorRutina generador;

    public RutinaService(UsuarioService usuarioService,
                         PerfilService perfilService,
                         EjercicioRepository ejercicioRepository,
                         RutinaRepository rutinaRepository,
                         GeneradorRutina generador) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
        this.ejercicioRepository = ejercicioRepository;
        this.rutinaRepository = rutinaRepository;
        this.generador = generador;
    }

    @Transactional
    public RutinaResponse generar(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Perfil perfil = perfilService.exigirPerfilCompleto(usuario);

        List<EjercicioPlanificado> planificados = generador.generar(solicitudDe(usuario), ejercicioRepository.findAllConContraindicaciones());
        if (planificados.isEmpty()) {
            throw ApiException.solicitudInvalida(
                    "No encontramos ejercicios compatibles con tus restricciones. Revisa tu perfil.");
        }

        // Solo una rutina activa por usuario: las anteriores quedan en el historial.
        rutinaRepository.desactivarRutinasActivas(usuario.getId());

        Rutina rutina = new Rutina();
        rutina.setUsuario(usuario);
        rutina.setObjetivo(perfil.getObjetivo());
        rutina.setNombre("Rutina para " + perfil.getObjetivo().getNombre().toLowerCase());
        rutina.setDiasPorSemana(perfil.getDiasPorSemana());
        rutina.setMinutosPorSesion(perfil.getMinutosPorSesion());
        planificados.forEach(p -> rutina.agregarEjercicio(aEntidad(p)));

        return RutinaResponse.desde(rutinaRepository.save(rutina));
    }

    @Transactional(readOnly = true)
    public RutinaResponse activa(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return rutinaRepository.findFirstByUsuarioIdAndActivaTrueOrderByGeneradaEnDesc(usuario.getId())
                .map(RutinaResponse::desde)
                .orElseThrow(() -> ApiException.noEncontrado("Aún no tienes una rutina generada."));
    }

    @Transactional(readOnly = true)
    public List<RutinaResumenResponse> historial(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return rutinaRepository.findByUsuarioIdOrderByGeneradaEnDesc(usuario.getId()).stream()
                .map(RutinaResumenResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public RutinaResponse detalle(String email, Long rutinaId) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return RutinaResponse.desde(rutinaDelUsuario(usuario, rutinaId));
    }

    @Transactional(readOnly = true)
    public List<EjercicioResponse> alternativas(String email, Long rutinaId, Long itemId) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Rutina rutina = rutinaDelUsuario(usuario, rutinaId);
        RutinaEjercicio item = itemDeRutina(rutina, itemId);

        return alternativasPara(usuario, rutina, item).stream().map(EjercicioResponse::desde).toList();
    }

    @Transactional
    public RutinaResponse sustituir(String email, Long rutinaId, SustitucionRequest peticion) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Rutina rutina = rutinaDelUsuario(usuario, rutinaId);
        RutinaEjercicio item = itemDeRutina(rutina, peticion.itemId());

        List<Ejercicio> alternativas = alternativasPara(usuario, rutina, item);
        if (alternativas.isEmpty()) {
            throw ApiException.solicitudInvalida("No hay ejercicios alternativos compatibles con tu perfil.");
        }
        Ejercicio nuevo = peticion.nuevoId() == null ? alternativas.get(0)
                : alternativas.stream()
                        .filter(e -> e.getId().equals(peticion.nuevoId()))
                        .findFirst()
                        .orElseThrow(() -> ApiException.solicitudInvalida(
                                "El ejercicio elegido no es compatible con tu rutina o tus restricciones."));

        EjercicioPlanificado actual = new EjercicioPlanificado(item.getEjercicio(), item.getDiaNumero(), item.getOrden(),
                item.getSeries(), item.getRepeticiones(), item.getDuracionSeg(), item.getDescansoSeg());
        EjercicioPlanificado reemplazo = generador.sustituir(actual, nuevo, solicitudDe(usuario));

        item.setEjercicio(reemplazo.ejercicio());
        item.setSeries(reemplazo.series());
        item.setRepeticiones(reemplazo.repeticiones());
        item.setDuracionSeg(reemplazo.duracionSeg());
        item.setDescansoSeg(reemplazo.descansoSeg());
        return RutinaResponse.desde(rutina);
    }

    private List<Ejercicio> alternativasPara(Usuario usuario, Rutina rutina, RutinaEjercicio item) {
        // No se proponen ejercicios que ya estan en el mismo dia.
        Set<Long> delMismoDia = rutina.getEjercicios().stream()
                .filter(e -> e.getDiaNumero() == item.getDiaNumero())
                .map(e -> e.getEjercicio().getId())
                .collect(Collectors.toSet());
        return generador.alternativas(item.getEjercicio(), delMismoDia, solicitudDe(usuario),
                ejercicioRepository.findAllConContraindicaciones());
    }

    private SolicitudRutina solicitudDe(Usuario usuario) {
        Perfil perfil = perfilService.exigirPerfilCompleto(usuario);
        return new SolicitudRutina(perfil.getObjetivo().getCodigo(), perfil.getNivelExperiencia(),
                perfil.getMinutosPorSesion(), perfil.getDiasPorSemana(), PerfilService.idsDeRestricciones(usuario));
    }

    /** Busca la rutina filtrando por usuario: una rutina ajena se responde como inexistente (RNF06). */
    private Rutina rutinaDelUsuario(Usuario usuario, Long rutinaId) {
        return rutinaRepository.findByIdAndUsuarioId(rutinaId, usuario.getId())
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la rutina solicitada."));
    }

    private static RutinaEjercicio itemDeRutina(Rutina rutina, Long itemId) {
        return rutina.getEjercicios().stream()
                .filter(e -> e.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("El ejercicio no pertenece a esta rutina."));
    }

    private static RutinaEjercicio aEntidad(EjercicioPlanificado planificado) {
        RutinaEjercicio item = new RutinaEjercicio();
        item.setEjercicio(planificado.ejercicio());
        item.setDiaNumero(planificado.dia());
        item.setOrden(planificado.orden());
        item.setSeries(planificado.series());
        item.setRepeticiones(planificado.repeticiones());
        item.setDuracionSeg(planificado.duracionSeg());
        item.setDescansoSeg(planificado.descansoSeg());
        return item;
    }
}
