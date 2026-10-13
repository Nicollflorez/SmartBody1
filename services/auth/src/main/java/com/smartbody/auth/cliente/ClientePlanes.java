package com.smartbody.auth.cliente;

import com.smartbody.comun.cliente.ClienteServicios;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/** Consulta al servicio planes la rutina y el plan activos del usuario (con su propio token). */
@Component
public class ClientePlanes {

    public record RutinaActiva(Long id, LocalDateTime generadaEn) {
    }

    public record PlanActivo(Long id) {
    }

    private final ClienteServicios cliente;
    private final String urlPlanes;

    public ClientePlanes(ClienteServicios cliente, @Value("${smartbody.servicios.planes}") String urlPlanes) {
        this.cliente = cliente;
        this.urlPlanes = urlPlanes;
    }

    public Optional<RutinaActiva> rutinaActiva() {
        return cliente.comoUsuario().getSiExiste(urlPlanes + "/api/routines/active", RutinaActiva.class);
    }

    public Optional<PlanActivo> planActivo() {
        return cliente.comoUsuario().getSiExiste(urlPlanes + "/api/nutrition/active", PlanActivo.class);
    }
}
