package com.smartbody.planes.controller;

import com.smartbody.planes.dto.EjercicioResponse;
import com.smartbody.planes.dto.ObjetivoResponse;
import com.smartbody.planes.dto.RestriccionResponse;
import com.smartbody.planes.service.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catalogos protegidos por JWT: objetivos, restricciones y detalle de ejercicios. */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /** RF04 - objetivos disponibles. */
    @GetMapping("/objectives")
    public ResponseEntity<List<ObjetivoResponse>> objetivos() {
        return ResponseEntity.ok(catalogoService.objetivos());
    }

    /** RF05 - restricciones de salud y alimentacion. */
    @GetMapping("/restrictions")
    public ResponseEntity<List<RestriccionResponse>> restricciones() {
        return ResponseEntity.ok(catalogoService.restricciones());
    }

    /** RF08 - detalle de un ejercicio. */
    @GetMapping("/exercises/{id}")
    public ResponseEntity<EjercicioResponse> ejercicio(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.ejercicio(id));
    }
}
