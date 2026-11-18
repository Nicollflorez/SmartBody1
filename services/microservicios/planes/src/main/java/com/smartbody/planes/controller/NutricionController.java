package com.smartbody.planes.controller;

import com.smartbody.comun.seguridad.UsuarioAutenticado;
import com.smartbody.planes.cliente.PerfilCliente;
import com.smartbody.planes.dto.AlimentoResponse;
import com.smartbody.planes.dto.PlanNutricionalResponse;
import com.smartbody.planes.dto.SustitucionRequest;
import com.smartbody.planes.service.NutricionService;
import com.smartbody.planes.service.reglas.Hidratacion;
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

@RestController
@RequestMapping("/api/nutrition")
public class NutricionController {

    private final NutricionService nutricionService;
    private final PerfilCliente perfilCliente;

    public NutricionController(NutricionService nutricionService, PerfilCliente perfilCliente) {
        this.nutricionService = nutricionService;
        this.perfilCliente = perfilCliente;
    }

    @PostMapping("/generate")
    public ResponseEntity<PlanNutricionalResponse> generar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nutricionService.generar(usuario.id(), perfilCliente.delUsuarioActual()));
    }

    @GetMapping("/active")
    public ResponseEntity<PlanNutricionalResponse> activo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(nutricionService.activo(usuario.id(), perfilCliente.delUsuarioActual()));
    }

    /** Recomendacion de hidratacion del usuario (la usa el chatbot del servicio progreso). */
    @GetMapping("/hydration")
    public ResponseEntity<Hidratacion> hidratacion() {
        return ResponseEntity.ok(nutricionService.hidratacion(perfilCliente.delUsuarioActual()));
    }

    /** RF09 - alimentos que pueden reemplazar a uno del plan. */
    @GetMapping("/{id}/items/{itemId}/alternatives")
    public ResponseEntity<List<AlimentoResponse>> alternativas(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                               @PathVariable Long id,
                                                               @PathVariable Long itemId) {
        return ResponseEntity.ok(nutricionService.alternativas(usuario.id(), perfilCliente.delUsuarioActual(), id, itemId));
    }

    /** RF09 - sustitucion de un alimento. */
    @PostMapping("/{id}/replace-food")
    public ResponseEntity<PlanNutricionalResponse> sustituir(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                             @PathVariable Long id,
                                                             @Valid @RequestBody SustitucionRequest peticion) {
        return ResponseEntity.ok(nutricionService.sustituir(usuario.id(), perfilCliente.delUsuarioActual(), id, peticion));
    }
}
