package com.smartbody.comun.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/** Comprobacion rapida de que el servicio responde (publico). */
@RestController
public class PingController {

    private final String servicio;

    public PingController(@Value("${spring.application.name:smartbody}") String servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/api/ping")
    public Map<String, Object> ping() {
        return Map.of("servicio", "Smartbody API", "componente", servicio, "estado", "ACTIVO",
                "momento", LocalDateTime.now().toString());
    }
}
