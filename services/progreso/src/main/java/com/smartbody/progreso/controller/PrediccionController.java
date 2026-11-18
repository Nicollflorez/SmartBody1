package com.smartbody.progreso.controller;

import com.smartbody.comun.seguridad.UsuarioAutenticado;
import com.smartbody.progreso.dto.PrediccionRequest;
import com.smartbody.progreso.dto.PrediccionResponse;
import com.smartbody.progreso.dto.PrediccionResumenResponse;
import com.smartbody.progreso.dto.SeguimientoPrediccionResponse;
import com.smartbody.progreso.service.PrediccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Estimación orientativa del tiempo hasta el peso objetivo (mineria de datos). Requiere JWT. */
@RestController
@RequestMapping("/api/predicciones")
public class PrediccionController {

    private final PrediccionService prediccionService;

    public PrediccionController(PrediccionService prediccionService) {
        this.prediccionService = prediccionService;
    }

    @PostMapping("/tiempo-perdida-peso")
    public ResponseEntity<PrediccionResponse> predecir(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                       @Valid @RequestBody PrediccionRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prediccionService.predecir(usuario.id(), peticion));
    }

    @GetMapping
    public List<PrediccionResumenResponse> historial(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return prediccionService.historial(usuario.id());
    }

    @GetMapping("/{id}/seguimiento")
    public SeguimientoPrediccionResponse seguimiento(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                     @PathVariable Long id) {
        return prediccionService.seguimiento(usuario.id(), id);
    }
}
