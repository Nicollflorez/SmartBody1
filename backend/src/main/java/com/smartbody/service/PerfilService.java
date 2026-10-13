package com.smartbody.service;

import com.smartbody.dto.PerfilRequest;
import com.smartbody.dto.PerfilResponse;
import com.smartbody.dto.ResumenResponse;
import com.smartbody.entity.Medida;
import com.smartbody.entity.Objetivo;
import com.smartbody.entity.Perfil;
import com.smartbody.entity.PlanNutricional;
import com.smartbody.entity.Restriccion;
import com.smartbody.entity.Rutina;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.MedidaRepository;
import com.smartbody.repository.ObjetivoRepository;
import com.smartbody.repository.PlanNutricionalRepository;
import com.smartbody.repository.RestriccionRepository;
import com.smartbody.repository.RutinaRepository;
import com.smartbody.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** RF03, RF04 y RF05: perfil fisico, objetivo, disponibilidad y restricciones. */
@Service
public class PerfilService {

    /** RF11: despues de este tiempo se sugiere al usuario renovar su rutina. */
    static final long DIAS_PARA_RENOVAR_RUTINA = 28;

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ObjetivoRepository objetivoRepository;
    private final RestriccionRepository restriccionRepository;
    private final MedidaRepository medidaRepository;
    private final RutinaRepository rutinaRepository;
    private final PlanNutricionalRepository planNutricionalRepository;

    public PerfilService(UsuarioService usuarioService,
                         UsuarioRepository usuarioRepository,
                         ObjetivoRepository objetivoRepository,
                         RestriccionRepository restriccionRepository,
                         MedidaRepository medidaRepository,
                         RutinaRepository rutinaRepository,
                         PlanNutricionalRepository planNutricionalRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.objetivoRepository = objetivoRepository;
        this.restriccionRepository = restriccionRepository;
        this.medidaRepository = medidaRepository;
        this.rutinaRepository = rutinaRepository;
        this.planNutricionalRepository = planNutricionalRepository;
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

        // Cada cambio de peso o altura queda en el historial de progreso (RF10).
        if (cambiaronMedidas) {
            Medida medida = new Medida();
            medida.setUsuario(usuario);
            medida.setPesoKg(peticion.pesoKg());
            medida.setAlturaCm(peticion.alturaCm());
            medida.setImc(Perfil.calcularImc(peticion.pesoKg(), peticion.alturaCm()));
            medidaRepository.save(medida);
        }
        return PerfilResponse.desde(perfil, restricciones);
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Perfil perfil = usuario.getPerfil();
        Rutina rutina = rutinaRepository.findFirstByUsuarioIdAndActivaTrueOrderByGeneradaEnDesc(usuario.getId()).orElse(null);
        PlanNutricional plan = planNutricionalRepository.findFirstByUsuarioIdAndActivoTrueOrderByGeneradoEnDesc(usuario.getId()).orElse(null);

        Long diasDesdeRutina = rutina == null ? null
                : Duration.between(rutina.getGeneradaEn(), LocalDateTime.now()).toDays();
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
                rutina == null ? null : rutina.getId(),
                plan == null ? null : plan.getId(),
                diasDesdeRutina,
                diasDesdeRutina != null && diasDesdeRutina >= DIAS_PARA_RENOVAR_RUTINA,
                usuario.esAdministrador());
    }

    /** Perfil listo para generar planes; si falta informacion se informa al usuario. */
    public Perfil exigirPerfilCompleto(Usuario usuario) {
        Perfil perfil = usuario.getPerfil();
        if (perfil == null || !perfil.estaCompleto()) {
            throw ApiException.solicitudInvalida("Completa tu perfil antes de generar tu plan.");
        }
        return perfil;
    }

    public static Set<Long> idsDeRestricciones(Usuario usuario) {
        return usuario.getRestricciones().stream().map(Restriccion::getId).collect(Collectors.toSet());
    }
}
