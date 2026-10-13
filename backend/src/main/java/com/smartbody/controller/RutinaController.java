package com.smartbody.controller;

import com.smartbody.dto.EjercicioResponse;
import com.smartbody.dto.RutinaResponse;
import com.smartbody.dto.RutinaResumenResponse;
import com.smartbody.dto.SustitucionRequest;
import com.smartbody.service.RutinaService;
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

@RestController
@RequestMapping("/api/routines")
public class RutinaController {

    private final RutinaService rutinaService;

    public RutinaController(RutinaService rutinaService) {
        this.rutinaService = rutinaService;
    }

    /** RF06 - genera una nueva rutina y la deja como activa. */
    @PostMapping("/generate")
    public ResponseEntity<RutinaResponse> generar(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rutinaService.generar(autenticado.getUsername()));
    }

    /** RF07 - rutina vigente del usuario. */
    @GetMapping("/active")
    public ResponseEntity<RutinaResponse> activa(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.ok(rutinaService.activa(autenticado.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<RutinaResumenResponse>> historial(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.ok(rutinaService.historial(autenticado.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutinaResponse> detalle(@AuthenticationPrincipal UserDetails autenticado,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(rutinaService.detalle(autenticado.getUsername(), id));
    }

    /** RF09 - ejercicios que pueden reemplazar a uno de la rutina. */
    @GetMapping("/{id}/items/{itemId}/alternatives")
    public ResponseEntity<List<EjercicioResponse>> alternativas(@AuthenticationPrincipal UserDetails autenticado,
                                                                @PathVariable Long id,
                                                                @PathVariable Long itemId) {
        return ResponseEntity.ok(rutinaService.alternativas(autenticado.getUsername(), id, itemId));
    }

    /** RF09 - sustitucion de un ejercicio. */
    @PostMapping("/{id}/replace-exercise")
    public ResponseEntity<RutinaResponse> sustituir(@AuthenticationPrincipal UserDetails autenticado,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody SustitucionRequest peticion) {
        return ResponseEntity.ok(rutinaService.sustituir(autenticado.getUsername(), id, peticion));
    }
}
