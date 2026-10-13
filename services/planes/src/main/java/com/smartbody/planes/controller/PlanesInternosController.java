package com.smartbody.planes.controller;

import com.smartbody.planes.cliente.PerfilCliente;
import com.smartbody.planes.cliente.PerfilRemoto;
import com.smartbody.planes.dto.PlanNutricionalResponse;
import com.smartbody.planes.dto.RutinaResponse;
import com.smartbody.planes.repository.PlanNutricionalRepository;
import com.smartbody.planes.repository.RutinaRepository;
import com.smartbody.planes.service.NutricionService;
import com.smartbody.planes.service.RutinaService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Rutas internas para el panel de administracion (servicio admin). Protegidas por X-Internal-Token. */
@RestController
@RequestMapping("/internal/planes")
public class PlanesInternosController {

    public record Estadisticas(long rutinasGeneradas, long planesGenerados, Map<Long, Long> rutinasPorUsuario) {
    }

    /** Evento reciente: una rutina o un plan generado por un usuario. */
    public record Actividad(String tipo, Long usuarioId, String descripcion, LocalDateTime fecha) {
    }

    private final RutinaService rutinaService;
    private final NutricionService nutricionService;
    private final PerfilCliente perfilCliente;
    private final RutinaRepository rutinaRepository;
    private final PlanNutricionalRepository planRepository;

    public PlanesInternosController(RutinaService rutinaService, NutricionService nutricionService,
                                    PerfilCliente perfilCliente, RutinaRepository rutinaRepository,
                                    PlanNutricionalRepository planRepository) {
        this.rutinaService = rutinaService;
        this.nutricionService = nutricionService;
        this.perfilCliente = perfilCliente;
        this.rutinaRepository = rutinaRepository;
        this.planRepository = planRepository;
    }

    @GetMapping("/estadisticas")
    @Transactional(readOnly = true)
    public Estadisticas estadisticas() {
        Map<Long, Long> porUsuario = rutinaRepository.contarPorUsuario().stream()
                .collect(Collectors.toMap(f -> ((Number) f[0]).longValue(), f -> ((Number) f[1]).longValue()));
        return new Estadisticas(rutinaRepository.count(), planRepository.count(), porUsuario);
    }

    @GetMapping("/recientes")
    @Transactional(readOnly = true)
    public List<Actividad> recientes() {
        List<Actividad> actividad = new ArrayList<>();
        rutinaRepository.findRecientes().forEach(r -> actividad.add(new Actividad("RUTINA", r.getUsuarioId(),
                "Generó una " + r.getNombre().toLowerCase(), r.getGeneradaEn())));
        planRepository.findRecientes().forEach(p -> actividad.add(new Actividad("PLAN", p.getUsuarioId(),
                "Generó un plan nutricional de " + (p.getCaloriasObjetivo() == null ? "" : p.getCaloriasObjetivo() + " kcal"),
                p.getGeneradoEn())));
        return actividad;
    }

    @GetMapping("/usuarios/{usuarioId}/rutina-activa")
    public RutinaResponse rutinaActiva(@PathVariable Long usuarioId) {
        return rutinaService.activa(usuarioId);
    }

    @GetMapping("/usuarios/{usuarioId}/plan-activo")
    public PlanNutricionalResponse planActivo(@PathVariable Long usuarioId) {
        return nutricionService.activo(usuarioId, perfilCliente.deUsuario(usuarioId));
    }

    /** Genera una rutina y un plan nutricional nuevos con el perfil actual del usuario. */
    @PostMapping("/usuarios/{usuarioId}/generar")
    public ResponseEntity<Void> generar(@PathVariable Long usuarioId) {
        PerfilRemoto perfil = perfilCliente.deUsuario(usuarioId);
        rutinaService.generar(usuarioId, perfil);
        nutricionService.generar(usuarioId, perfil);
        return ResponseEntity.noContent().build();
    }
}
