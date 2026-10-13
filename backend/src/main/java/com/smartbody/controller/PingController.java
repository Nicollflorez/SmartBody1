package com.smartbody.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/** Endpoint publico para comprobar que la aplicacion Android alcanza el backend. */
@RestController
public class PingController {

    @GetMapping("/api/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "servicio", "Smartbody API",
                "estado", "ACTIVO",
                "momento", LocalDateTime.now().toString());
    }
}
