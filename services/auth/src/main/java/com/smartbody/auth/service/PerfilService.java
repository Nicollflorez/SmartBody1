package com.smartbody.auth.service;

import com.smartbody.auth.cliente.ClientePlanes;
import com.smartbody.auth.cliente.ClienteProgreso;
import com.smartbody.auth.dto.PerfilRequest;
import com.smartbody.auth.dto.PerfilResponse;
import com.smartbody.auth.dto.ResumenResponse;
import com.smartbody.auth.entity.Objetivo;
import com.smartbody.auth.entity.Perfil;
import com.smartbody.auth.entity.Restriccion;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.auth.repository.ObjetivoRepository;
import com.smartbody.auth.repository.RestriccionRepository;
import com.smartbody.auth.repository.UsuarioRepository;
import com.smartbody.comun.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** RF03, RF04 y RF05: perfil fisico, objetivo, disponibilidad y restricciones. */
@Service
public class PerfilService {

    /** RF11: despues de este tiempo se sugiere al usuario renovar su rutina. */
    static final long DIAS_PARA_RENOVAR_RUTINA = 28;

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ObjetivoRepository objetivoRepository;
    private final RestriccionRepository restriccionRepository;
    private final ClienteProgreso clienteProgreso;
    private final ClientePlanes clientePlanes;

    public PerfilService(UsuarioService usuarioService,
                         UsuarioRepository usuarioRepository,
                         ObjetivoRepository objetivoRepository,
                         RestriccionRepository restriccionRepository,
                         ClienteProgreso clienteProgreso,
                         ClientePlanes clientePlanes) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.objetivoRepository = objetivoRepository;
        this.restriccionRepository = restriccionRepository;
        this.clienteProgreso = clienteProgreso;
        this.clientePlanes = clientePlanes;
    }

    @Transactional(readOnly = true)
    public PerfilResponse consultar(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return PerfilResponse.desde(usuario.getPerfil(), usuario.getRestricciones());
    }

    @Transactional
    public PerfilResponse actualizar(String email, PerfilRequest peticion) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Objetivo objetivo = objetivoRepository.findById(peticion.objetivoId())
                .orElseThrow(() -> ApiException.solicitudInvalida("El objetivo seleccionado no existe."));
        Set<Long> idsSolicitados = peticion.restriccionIds() == null ? Set.of() : new HashSet<>(peticion.restriccionIds());
        List<Restriccion> restricciones = restriccionRepository.findAllById(idsSolicitados);
        if (restricciones.size() != idsSolicitados.size()) {
            throw ApiException.solicitudInvalida("Alguna de las restricciones seleccionadas no existe.");
        }

        Perfil perfil = usuario.getPerfil();
        if (perfil == null) {
            perfil = new Perfil();
            usuario.setPerfil(perfil);
        }
        boolean cambiaronMedidas = !Objects.equals(perfil.getPesoKg(), peticion.pesoKg())
                || !Objects.equals(perfil.getAlturaCm(), peticion.alturaCm());
        perfil.setEdad(peticion.edad());
        perfil.setSexo(peticion.sexo());
        perfil.setPesoKg(peticion.pesoKg());
        perfil.setAlturaCm(peticion.alturaCm());
        perfil.setNivelExperiencia(peticion.nivelExperiencia());
        perfil.setMinutosPorSesion(peticion.minutosPorSesion());
        perfil.setDiasPorSemana(peticion.diasPorSemana());
        perfil.setObjetivo(objetivo);
        usuario.setRestricciones(new HashSet<>(restricciones));
        usuarioRepository.save(usuario);

        // Cada cambio de peso o altura queda en el historial de progreso (RF10), que vive en otro servicio.
        if (cambiaronMedidas) {
            clienteProgreso.registrarMedida(usuario.getId(), peticion.pesoKg(), peticion.alturaCm());
        }
        return PerfilResponse.desde(perfil, restricciones);
    }

    /** Actualiza solo el peso (lo llama el servicio progreso al registrar una medida nueva). */
    @Transactional
    public void actualizarPeso(Long usuarioId, Double pesoKg) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la cuenta del usuario."));
        Perfil perfil = usuario.getPerfil();
        if (perfil == null) {
            perfil = new Perfil();
            usuario.setPerfil(perfil);
        }
        perfil.setPesoKg(pesoKg);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Perfil perfil = usuario.getPerfil();
        ClientePlanes.RutinaActiva rutina = clientePlanes.rutinaActiva().orElse(null);
        ClientePlanes.PlanActivo plan = clientePlanes.planActivo().orElse(null);
        Long diasDesdeRutina = rutina == null || rutina.generadaEn() == null ? null
                : Duration.between(rutina.generadaEn(), LocalDateTime.now()).toDays();
        Double imc = perfil == null ? null : perfil.calcularImc();
        return new ResumenResponse(
                usuario.getNombre(),
                usuario.getEmail(),
                perfil != null && perfil.estaCompleto(),
                perfil == null || perfil.getObjetivo() == null ? null : perfil.getObjetivo().getNombre(),
                perfil == null ? null : perfil.getPesoKg(),
                imc,
                Perfil.clasificarImc(imc),
                perfil == null ? null : perfil.getDiasPorSemana(),
                perfil == null ? null : perfil.getMinutosPorSesion(),
                rutina == null ? null : rutina.id(),
                plan == null ? null : plan.id(),
                diasDesdeRutina,
                diasDesdeRutina != null && diasDesdeRutina >= DIAS_PARA_RENOVAR_RUTINA,
                usuario.esAdministrador());
    }
}
