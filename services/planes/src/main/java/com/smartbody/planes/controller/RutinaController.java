package com.smartbody.planes.controller;

import com.smartbody.comun.seguridad.UsuarioAutenticado;
import com.smartbody.planes.cliente.PerfilCliente;
import com.smartbody.planes.dto.EjercicioResponse;
import com.smartbody.planes.dto.RutinaResponse;
import com.smartbody.planes.dto.RutinaResumenResponse;
import com.smartbody.planes.dto.SustitucionRequest;
import com.smartbody.planes.service.RutinaService;
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
@RequestMapping("/api/routines")
public class RutinaController {

    private final RutinaService rutinaService;
    private final PerfilCliente perfilCliente;

    public RutinaController(RutinaService rutinaService, PerfilCliente perfilCliente) {
        this.rutinaService = rutinaService;
        this.perfilCliente = perfilCliente;
    }

    /** RF06 - genera una nueva rutina y la deja como activa. */
    @PostMapping("/generate")
    public ResponseEntity<RutinaResponse> generar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rutinaService.generar(usuario.id(), perfilCliente.delUsuarioActual()));
    }

    /** RF07 - rutina vigente del usuario. */
    @GetMapping("/active")
    public ResponseEntity<RutinaResponse> activa(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(rutinaService.activa(usuario.id()));
    }

    @GetMapping
    public ResponseEntity<List<RutinaResumenResponse>> historial(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(rutinaService.historial(usuario.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutinaResponse> detalle(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(rutinaService.detalle(usuario.id(), id));
    }

    /** RF09 - ejercicios que pueden reemplazar a uno de la rutina. */
    @GetMapping("/{id}/items/{itemId}/alternatives")
    public ResponseEntity<List<EjercicioResponse>> alternativas(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                                @PathVariable Long id,
                                                                @PathVariable Long itemId) {
        return ResponseEntity.ok(rutinaService.alternativas(usuario.id(), perfilCliente.delUsuarioActual(), id, itemId));
    }

    /** RF09 - sustitucion de un ejercicio. */
    @PostMapping("/{id}/replace-exercise")
    public ResponseEntity<RutinaResponse> sustituir(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody SustitucionRequest peticion) {
        return ResponseEntity.ok(rutinaService.sustituir(usuario.id(), perfilCliente.delUsuarioActual(), id, peticion));
    }
}
