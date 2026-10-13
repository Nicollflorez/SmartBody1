package com.smartbody.controller;

import com.smartbody.dto.MedidaRequest;
import com.smartbody.dto.MedidaResponse;
import com.smartbody.dto.ProgresoResponse;
import com.smartbody.service.ProgresoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RF10 - actualizacion de medidas e historial de progreso. */
@RestController
@RequestMapping("/api/progress")
public class ProgresoController {

    private final ProgresoService progresoService;

    public ProgresoController(ProgresoService progresoService) {
        this.progresoService = progresoService;
    }

    @GetMapping
    public ResponseEntity<ProgresoResponse> consultar(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.ok(progresoService.consultar(autenticado.getUsername()));
    }

    @PostMapping
    public ResponseEntity<MedidaResponse> registrar(@AuthenticationPrincipal UserDetails autenticado,
                                                    @Valid @RequestBody MedidaRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progresoService.registrar(autenticado.getUsername(), peticion));
    }
}
