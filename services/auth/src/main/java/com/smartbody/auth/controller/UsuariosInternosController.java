package com.smartbody.auth.controller;

import com.smartbody.auth.dto.EstadisticasUsuariosResponse;
import com.smartbody.auth.dto.UsuarioInternoResponse;
import com.smartbody.auth.service.PerfilService;
import com.smartbody.auth.service.UsuariosInternosService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rutas internas de usuarios: las consumen admin (listado, detalle, estado, estadisticas) y
 * progreso (actualizar el peso). Protegidas por X-Internal-Token y nunca expuestas por Nginx.
 */
@RestController
@RequestMapping("/internal/usuarios")
public class UsuariosInternosController {

    public record EstadoRequest(@NotNull Boolean activo) {
    }

    public record PesoRequest(@NotNull Double pesoKg) {
    }

    private final UsuariosInternosService usuariosInternosService;
    private final PerfilService perfilService;

    public UsuariosInternosController(UsuariosInternosService usuariosInternosService, PerfilService perfilService) {
        this.usuariosInternosService = usuariosInternosService;
        this.perfilService = perfilService;
    }

    @GetMapping
    public List<UsuarioInternoResponse> listar() {
        return usuariosInternosService.listar();
    }

    @GetMapping("/estadisticas")
    public EstadisticasUsuariosResponse estadisticas() {
        return usuariosInternosService.estadisticas();
    }

    @GetMapping("/recientes")
    public List<UsuarioInternoResponse> recientes() {
        return usuariosInternosService.recientes();
    }

    @GetMapping("/{id}")
    public UsuarioInternoResponse detalle(@PathVariable Long id) {
        return usuariosInternosService.detalle(id);
    }

    @PutMapping("/{id}/estado")
    public UsuarioInternoResponse cambiarEstado(@PathVariable Long id, @RequestBody EstadoRequest peticion) {
        return usuariosInternosService.cambiarEstado(id, peticion.activo());
    }

    @PutMapping("/{id}/peso")
    public ResponseEntity<Void> actualizarPeso(@PathVariable Long id, @RequestBody PesoRequest peticion) {
        perfilService.actualizarPeso(id, peticion.pesoKg());
        return ResponseEntity.noContent().build();
    }
}
