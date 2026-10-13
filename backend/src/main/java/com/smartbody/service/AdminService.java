package com.smartbody.service;

import com.smartbody.dto.AdminDtos;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.MedidaRepository;
import com.smartbody.repository.PlanNutricionalRepository;
import com.smartbody.repository.RutinaRepository;
import com.smartbody.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Historia de usuario del administrador: resumen de uso, historial de actividad y gestion de cuentas. */
@Service
public class AdminService {

    static final int LIMITE_ACTIVIDAD = 20;
    static final int LIMITE_RESTRICCIONES = 5;

    private final UsuarioRepository usuarioRepository;
    private final RutinaRepository rutinaRepository;
    private final PlanNutricionalRepository planRepository;
    private final MedidaRepository medidaRepository;

    public AdminService(UsuarioRepository usuarioRepository,
                        RutinaRepository rutinaRepository,
                        PlanNutricionalRepository planRepository,
                        MedidaRepository medidaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rutinaRepository = rutinaRepository;
        this.planRepository = planRepository;
        this.medidaRepository = medidaRepository;
    }

    @Transactional(readOnly = true)
    public AdminDtos.Resumen resumen() {
        return new AdminDtos.Resumen(
                usuarioRepository.count(),
                usuarioRepository.countByActivoTrue(),
                usuarioRepository.countByCreadoEnAfter(LocalDateTime.now().minusDays(30)),
                usuarioRepository.contarPerfilesCompletos(),
                rutinaRepository.count(),
                planRepository.count(),
                medidaRepository.count(),
                conteos(usuarioRepository.contarPorObjetivo()),
                conteos(usuarioRepository.contarPorRestriccion()).stream().limit(LIMITE_RESTRICCIONES).toList(),
                actividadReciente());
    }

    /** Historial de uso: los eventos mas recientes de todos los usuarios, del mas nuevo al mas antiguo. */
    @Transactional(readOnly = true)
    public List<AdminDtos.Actividad> actividadReciente() {
        Stream<AdminDtos.Actividad> registros = usuarioRepository.findTop20ByOrderByCreadoEnDesc().stream()
                .map(u -> new AdminDtos.Actividad("REGISTRO", "Creó su cuenta", u.getNombre(), u.getEmail(), u.getCreadoEn()));
        Stream<AdminDtos.Actividad> rutinas = rutinaRepository.findRecientesConUsuario().stream()
                .map(r -> new AdminDtos.Actividad("RUTINA", "Generó una " + r.getNombre().toLowerCase(),
                        r.getUsuario().getNombre(), r.getUsuario().getEmail(), r.getGeneradaEn()));
        Stream<AdminDtos.Actividad> planes = planRepository.findRecientesConUsuario().stream()
                .map(p -> new AdminDtos.Actividad("PLAN", "Generó un plan nutricional de "
                        + (p.getCaloriasObjetivo() == null ? "" : p.getCaloriasObjetivo() + " kcal"),
                        p.getUsuario().getNombre(), p.getUsuario().getEmail(), p.getGeneradoEn()));
        Stream<AdminDtos.Actividad> medidas = medidaRepository.findRecientesConUsuario().stream()
                .map(m -> new AdminDtos.Actividad("MEDIDA", "Registró un peso de " + formatoPeso(m.getPesoKg()) + " kg",
                        m.getUsuario().getNombre(), m.getUsuario().getEmail(), m.getRegistradoEn()));

        return Stream.of(registros, rutinas, planes, medidas)
                .flatMap(s -> s)
                .sorted(Comparator.comparing(AdminDtos.Actividad::fecha).reversed())
                .limit(LIMITE_ACTIVIDAD)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.UsuarioAdmin> usuarios() {
        Map<Long, Long> rutinasPorUsuario = porUsuario(rutinaRepository.contarPorUsuario());
        Map<Long, Long> medidasPorUsuario = porUsuario(medidaRepository.contarPorUsuario());

        return usuarioRepository.findAllParaAdministracion().stream()
                .map(u -> new AdminDtos.UsuarioAdmin(
                        u.getId(),
                        u.getNombre(),
                        u.getEmail(),
                        u.isActivo(),
                        u.esAdministrador(),
                        u.getCreadoEn(),
                        u.getPerfil() != null && u.getPerfil().estaCompleto(),
                        u.getPerfil() == null || u.getPerfil().getObjetivo() == null ? null : u.getPerfil().getObjetivo().getNombre(),
                        rutinasPorUsuario.getOrDefault(u.getId(), 0L),
                        medidasPorUsuario.getOrDefault(u.getId(), 0L)))
                .toList();
    }

    /** Activa o desactiva una cuenta. Una cuenta desactivada no puede iniciar sesion y su token deja de ser valido. */
    @Transactional
    public void cambiarEstado(Long usuarioId, boolean activo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario."));
        if (usuario.esAdministrador() && !activo) {
            throw ApiException.solicitudInvalida("No se puede desactivar una cuenta de administrador.");
        }
        usuario.setActivo(activo);
    }

    private static List<AdminDtos.Conteo> conteos(List<Object[]> filas) {
        List<AdminDtos.Conteo> lista = new ArrayList<>();
        for (Object[] fila : filas) {
            lista.add(new AdminDtos.Conteo((String) fila[0], ((Number) fila[1]).longValue()));
        }
        return lista;
    }

    private static Map<Long, Long> porUsuario(List<Object[]> filas) {
        return filas.stream().collect(Collectors.toMap(f -> ((Number) f[0]).longValue(), f -> ((Number) f[1]).longValue()));
    }

    private static String formatoPeso(Double peso) {
        if (peso == null) {
            return "-";
        }
        return peso % 1 == 0 ? String.valueOf(peso.intValue()) : String.valueOf(peso).replace('.', ',');
    }
}
