package com.smartbody.progreso.controller;

import com.smartbody.comun.seguridad.UsuarioAutenticado;
import com.smartbody.progreso.cliente.ClienteUsuarioYPlanes;
import com.smartbody.progreso.dto.MedidaRequest;
import com.smartbody.progreso.dto.MedidaResponse;
import com.smartbody.progreso.dto.ProgresoResponse;
import com.smartbody.progreso.service.ProgresoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final ClienteUsuarioYPlanes cliente;

    public ProgresoController(ProgresoService progresoService, ClienteUsuarioYPlanes cliente) {
        this.progresoService = progresoService;
        this.cliente = cliente;
    }

    @GetMapping
    public ResponseEntity<ProgresoResponse> consultar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(progresoService.consultar(usuario.id()));
    }

    @PostMapping
    public ResponseEntity<MedidaResponse> registrar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                    @Valid @RequestBody MedidaRequest peticion) {
        // La altura del perfil (en auth) completa el IMC cuando la medida no la trae.
        var perfil = peticion.alturaCm() != null ? null : cliente.perfilActual();
        return ResponseEntity.status(HttpStatus.CREATED).body(progresoService.registrar(usuario.id(), peticion, perfil));
    }
}
