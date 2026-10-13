package com.smartbody.controller;

import com.smartbody.dto.AlimentoResponse;
import com.smartbody.dto.PlanNutricionalResponse;
import com.smartbody.dto.SustitucionRequest;
import com.smartbody.service.NutricionService;
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
@RequestMapping("/api/nutrition")
public class NutricionController {

    private final NutricionService nutricionService;

    public NutricionController(NutricionService nutricionService) {
        this.nutricionService = nutricionService;
    }

    @PostMapping("/generate")
    public ResponseEntity<PlanNutricionalResponse> generar(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nutricionService.generar(autenticado.getUsername()));
    }

    @GetMapping("/active")
    public ResponseEntity<PlanNutricionalResponse> activo(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.ok(nutricionService.activo(autenticado.getUsername()));
    }

    /** RF09 - alimentos que pueden reemplazar a uno del plan. */
    @GetMapping("/{id}/items/{itemId}/alternatives")
    public ResponseEntity<List<AlimentoResponse>> alternativas(@AuthenticationPrincipal UserDetails autenticado,
                                                               @PathVariable Long id,
                                                               @PathVariable Long itemId) {
        return ResponseEntity.ok(nutricionService.alternativas(autenticado.getUsername(), id, itemId));
    }

    /** RF09 - sustitucion de un alimento. */
    @PostMapping("/{id}/replace-food")
    public ResponseEntity<PlanNutricionalResponse> sustituir(@AuthenticationPrincipal UserDetails autenticado,
                                                             @PathVariable Long id,
                                                             @Valid @RequestBody SustitucionRequest peticion) {
        return ResponseEntity.ok(nutricionService.sustituir(autenticado.getUsername(), id, peticion));
    }
}
