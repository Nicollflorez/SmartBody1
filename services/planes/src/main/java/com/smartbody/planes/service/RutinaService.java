package com.smartbody.planes.service;

import com.smartbody.comun.error.ApiException;
import com.smartbody.planes.cliente.PerfilCliente;
import com.smartbody.planes.cliente.PerfilRemoto;
import com.smartbody.planes.dto.EjercicioResponse;
import com.smartbody.planes.dto.RutinaResponse;
import com.smartbody.planes.dto.RutinaResumenResponse;
import com.smartbody.planes.dto.SustitucionRequest;
import com.smartbody.planes.entity.Ejercicio;
import com.smartbody.planes.entity.Objetivo;
import com.smartbody.planes.entity.Rutina;
import com.smartbody.planes.entity.RutinaEjercicio;
import com.smartbody.planes.repository.EjercicioRepository;
import com.smartbody.planes.repository.ObjetivoRepository;
import com.smartbody.planes.repository.RutinaRepository;
import com.smartbody.planes.service.reglas.EjercicioPlanificado;
import com.smartbody.planes.service.reglas.GeneradorRutina;
import com.smartbody.planes.service.reglas.SolicitudRutina;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RF06, RF07 y RF09: generacion, consulta y sustitucion de ejercicios de la rutina.
 * El usuario se identifica por el {@code uid} del JWT y su perfil llega del servicio auth.
 */
@Service
public class RutinaService {

    private final ObjetivoRepository objetivoRepository;
    private final EjercicioRepository ejercicioRepository;
    private final RutinaRepository rutinaRepository;
    private final GeneradorRutina generador;

    public RutinaService(ObjetivoRepository objetivoRepository,
                         EjercicioRepository ejercicioRepository,
                         RutinaRepository rutinaRepository,
                         GeneradorRutina generador) {
        this.objetivoRepository = objetivoRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.rutinaRepository = rutinaRepository;
        this.generador = generador;
    }

    @Transactional
    public RutinaResponse generar(Long usuarioId, PerfilRemoto perfil) {
        PerfilCliente.exigirCompleto(perfil);
        Objetivo objetivo = objetivoRepository.findById(perfil.objetivo().id())
                .orElseThrow(() -> ApiException.solicitudInvalida("El objetivo del perfil no existe en el catálogo."));
        List<EjercicioPlanificado> planificados = generador.generar(solicitudDe(perfil),
                ejercicioRepository.findAllConContraindicaciones());
        if (planificados.isEmpty()) {
            throw ApiException.solicitudInvalida(
                    "No encontramos ejercicios compatibles con tus restricciones. Revisa tu perfil.");
        }

        // Solo una rutina activa por usuario: las anteriores quedan en el historial.
        rutinaRepository.desactivarRutinasActivas(usuarioId);
        Rutina rutina = new Rutina();
        rutina.setUsuarioId(usuarioId);
        rutina.setObjetivo(objetivo);
        rutina.setNombre("Rutina para " + objetivo.getNombre().toLowerCase());
        rutina.setDiasPorSemana(perfil.diasPorSemana());
        rutina.setMinutosPorSesion(perfil.minutosPorSesion());
        planificados.forEach(p -> rutina.agregarEjercicio(aEntidad(p)));
        return RutinaResponse.desde(rutinaRepository.save(rutina));
    }

    @Transactional(readOnly = true)
    public RutinaResponse activa(Long usuarioId) {
        return rutinaRepository.findFirstByUsuarioIdAndActivaTrueOrderByGeneradaEnDesc(usuarioId)
                .map(RutinaResponse::desde)
                .orElseThrow(() -> ApiException.noEncontrado("Aún no tienes una rutina generada."));
    }

    @Transactional(readOnly = true)
    public List<RutinaResumenResponse> historial(Long usuarioId) {
        return rutinaRepository.findByUsuarioIdOrderByGeneradaEnDesc(usuarioId).stream()
                .map(RutinaResumenResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public RutinaResponse detalle(Long usuarioId, Long rutinaId) {
        return RutinaResponse.desde(rutinaDelUsuario(usuarioId, rutinaId));
    }

    @Transactional(readOnly = true)
    public List<EjercicioResponse> alternativas(Long usuarioId, PerfilRemoto perfil, Long rutinaId, Long itemId) {
        Rutina rutina = rutinaDelUsuario(usuarioId, rutinaId);
        RutinaEjercicio item = itemDeRutina(rutina, itemId);
        return alternativasPara(perfil, rutina, item).stream().map(EjercicioResponse::desde).toList();
    }

    @Transactional
    public RutinaResponse sustituir(Long usuarioId, PerfilRemoto perfil, Long rutinaId, SustitucionRequest peticion) {
        Rutina rutina = rutinaDelUsuario(usuarioId, rutinaId);
        RutinaEjercicio item = itemDeRutina(rutina, peticion.itemId());
        List<Ejercicio> alternativas = alternativasPara(perfil, rutina, item);
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
        EjercicioPlanificado reemplazo = generador.sustituir(actual, nuevo, solicitudDe(perfil));
        item.setEjercicio(reemplazo.ejercicio());
        item.setSeries(reemplazo.series());
        item.setRepeticiones(reemplazo.repeticiones());
        item.setDuracionSeg(reemplazo.duracionSeg());
        item.setDescansoSeg(reemplazo.descansoSeg());
        return RutinaResponse.desde(rutina);
    }

    private List<Ejercicio> alternativasPara(PerfilRemoto perfil, Rutina rutina, RutinaEjercicio item) {
        // No se proponen ejercicios que ya estan en el mismo dia.
        Set<Long> delMismoDia = rutina.getEjercicios().stream()
                .filter(e -> e.getDiaNumero() == item.getDiaNumero())
                .map(e -> e.getEjercicio().getId())
                .collect(Collectors.toSet());
        return generador.alternativas(item.getEjercicio(), delMismoDia, solicitudDe(perfil),
                ejercicioRepository.findAllConContraindicaciones());
    }

    private static SolicitudRutina solicitudDe(PerfilRemoto perfil) {
        PerfilCliente.exigirCompleto(perfil);
        return new SolicitudRutina(perfil.objetivo().codigo(), perfil.nivelExperiencia(),
                perfil.minutosPorSesion(), perfil.diasPorSemana(), perfil.idsRestricciones());
    }

    /** Busca la rutina filtrando por usuario: una rutina ajena se responde como inexistente (RNF06). */
    private Rutina rutinaDelUsuario(Long usuarioId, Long rutinaId) {
        return rutinaRepository.findByIdAndUsuarioId(rutinaId, usuarioId)
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
