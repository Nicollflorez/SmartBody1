package com.smartbody.progreso.controller;

import com.smartbody.progreso.dto.MedidaResponse;
import com.smartbody.progreso.dto.ProgresoResponse;
import com.smartbody.progreso.entity.Medida;
import com.smartbody.progreso.repository.MedidaRepository;
import com.smartbody.progreso.service.ProgresoService;
import com.smartbody.progreso.service.reglas.EvaluadorProgreso;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Rutas internas: las usan auth (medida desde el perfil) y admin (estadisticas, detalle, efectividad). */
@RestController
@RequestMapping("/internal/progreso")
public class ProgresoInternoController {

    public record MedidaInterna(@NotNull Double pesoKg, Double alturaCm) {
    }

    public record Estadisticas(long medidasRegistradas, Map<Long, Long> medidasPorUsuario) {
    }

    public record Actividad(Long usuarioId, Double pesoKg, LocalDateTime fecha) {
    }

    /** Usuario con su objetivo, para evaluar si su evolucion va en la direccion correcta. */
    public record UsuarioObjetivo(Long usuarioId, String objetivoCodigo) {
    }

    /** Progreso de un usuario entre su primera y su ultima medida (solo si tiene al menos dos). */
    public record Evaluacion(Long usuarioId, double pesoInicial, double pesoActual, double cambioPesoKg,
                             Double imcInicial, Double imcActual, boolean mejoro, long diasSeguimiento) {
    }

    private final ProgresoService progresoService;
    private final MedidaRepository medidaRepository;

    public ProgresoInternoController(ProgresoService progresoService, MedidaRepository medidaRepository) {
        this.progresoService = progresoService;
        this.medidaRepository = medidaRepository;
    }

    @PostMapping("/usuarios/{usuarioId}/medidas")
    public ResponseEntity<MedidaResponse> registrarDesdePerfil(@PathVariable Long usuarioId,
                                                               @RequestBody MedidaInterna medida) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progresoService.registrarDesdePerfil(usuarioId, medida.pesoKg(), medida.alturaCm()));
    }

    @GetMapping("/usuarios/{usuarioId}")
    public ProgresoResponse progreso(@PathVariable Long usuarioId) {
        return progresoService.consultar(usuarioId);
    }

    @GetMapping("/estadisticas")
    @Transactional(readOnly = true)
    public Estadisticas estadisticas() {
        Map<Long, Long> porUsuario = medidaRepository.contarPorUsuario().stream()
                .collect(Collectors.toMap(f -> ((Number) f[0]).longValue(), f -> ((Number) f[1]).longValue()));
        return new Estadisticas(medidaRepository.count(), porUsuario);
    }

    @GetMapping("/recientes")
    @Transactional(readOnly = true)
    public List<Actividad> recientes() {
        return medidaRepository.findRecientes().stream()
                .map(m -> new Actividad(m.getUsuarioId(), m.getPesoKg(), m.getRegistradoEn()))
                .toList();
    }

    /** Evalua a cada usuario con su objetivo; se omiten los que tienen menos de dos medidas. */
    @PostMapping("/evaluaciones")
    @Transactional(readOnly = true)
    public List<Evaluacion> evaluar(@RequestBody List<UsuarioObjetivo> usuarios) {
        Map<Long, List<Medida>> medidasPorUsuario = medidaRepository.findAllByOrderByRegistradoEnAsc().stream()
                .collect(Collectors.groupingBy(Medida::getUsuarioId, LinkedHashMap::new, Collectors.toList()));
        List<Evaluacion> evaluaciones = new ArrayList<>();
        for (UsuarioObjetivo usuario : usuarios) {
            List<Medida> medidas = medidasPorUsuario.getOrDefault(usuario.usuarioId(), List.of());
            if (medidas.size() < 2) {
                continue;
            }
            Medida primera = medidas.get(0);
            Medida ultima = medidas.get(medidas.size() - 1);
            evaluaciones.add(new Evaluacion(usuario.usuarioId(), primera.getPesoKg(), ultima.getPesoKg(),
                    Math.round((ultima.getPesoKg() - primera.getPesoKg()) * 10.0) / 10.0,
                    primera.getImc(), ultima.getImc(),
                    EvaluadorProgreso.mejoro(usuario.objetivoCodigo(), primera.getPesoKg(), ultima.getPesoKg(),
                            primera.getImc(), ultima.getImc()),
                    Duration.between(primera.getRegistradoEn(), ultima.getRegistradoEn()).toDays()));
        }
        return evaluaciones;
    }
}
