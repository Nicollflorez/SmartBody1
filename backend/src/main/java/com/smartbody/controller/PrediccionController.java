package com.smartbody.controller;

import com.smartbody.dto.PrediccionRequest;
import com.smartbody.dto.PrediccionResponse;
import com.smartbody.dto.PrediccionResumenResponse;
import com.smartbody.dto.SeguimientoPrediccionResponse;
import com.smartbody.service.PrediccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<PrediccionResponse> predecir(@AuthenticationPrincipal UserDetails autenticado,
                                                       @Valid @RequestBody PrediccionRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prediccionService.predecir(autenticado.getUsername(), peticion));
    }

    @GetMapping
    public List<PrediccionResumenResponse> historial(@AuthenticationPrincipal UserDetails autenticado) {
        return prediccionService.historial(autenticado.getUsername());
    }

    @GetMapping("/{id}/seguimiento")
    public SeguimientoPrediccionResponse seguimiento(@AuthenticationPrincipal UserDetails autenticado,
                                                     @PathVariable Long id) {
        return prediccionService.seguimiento(autenticado.getUsername(), id);
    }
}
