package com.smartbody.comun.seguridad;

import java.util.List;

/** Rutas adicionales sin autenticacion que declara cada servicio (por ejemplo auth: registro y login). */
public record RutasPublicas(List<String> patrones) {

    public static RutasPublicas de(String... patrones) {
        return new RutasPublicas(List.of(patrones));
    }

    public static RutasPublicas ninguna() {
        return new RutasPublicas(List.of());
    }
}
