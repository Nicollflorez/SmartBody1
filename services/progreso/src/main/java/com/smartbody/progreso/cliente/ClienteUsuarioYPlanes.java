package com.smartbody.progreso.cliente;

import com.smartbody.comun.cliente.ClienteServicios;
import com.smartbody.progreso.dto.PerfilResponse;
import com.smartbody.progreso.dto.PlanNutricionalResponse;
import com.smartbody.progreso.dto.RutinaResponse;
import com.smartbody.progreso.service.reglas.Hidratacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Datos del usuario que viven en auth y planes; se consultan con el token del usuario o por via interna. */
@Component
public class ClienteUsuarioYPlanes {

    public record UsuarioRemoto(Long id, String nombre, String email) {
    }

    private record PesoRequest(Double pesoKg) {
    }

    private final ClienteServicios cliente;
    private final String urlAuth;
    private final String urlPlanes;

    public ClienteUsuarioYPlanes(ClienteServicios cliente,
                                 @Value("${smartbody.servicios.auth}") String urlAuth,
                                 @Value("${smartbody.servicios.planes}") String urlPlanes) {
        this.cliente = cliente;
        this.urlAuth = urlAuth;
        this.urlPlanes = urlPlanes;
    }

    public UsuarioRemoto usuarioActual() {
        return cliente.comoUsuario().get(urlAuth + "/api/users/me", UsuarioRemoto.class);
    }

    public PerfilResponse perfilActual() {
        return cliente.comoUsuario().get(urlAuth + "/api/users/profile", PerfilResponse.class);
    }

    public Optional<RutinaResponse> rutinaActiva() {
        return cliente.comoUsuario().getSiExiste(urlPlanes + "/api/routines/active", RutinaResponse.class);
    }

    public Optional<PlanNutricionalResponse> planActivo() {
        return cliente.comoUsuario().getSiExiste(urlPlanes + "/api/nutrition/active", PlanNutricionalResponse.class);
    }

    public Optional<Hidratacion> hidratacion() {
        return cliente.comoUsuario().getSiExiste(urlPlanes + "/api/nutrition/hydration", Hidratacion.class);
    }

    /** El perfil en auth siempre refleja el peso mas reciente para las proximas recomendaciones. */
    public void actualizarPeso(Long usuarioId, Double pesoKg) {
        cliente.interno().put(urlAuth + "/internal/usuarios/" + usuarioId + "/peso", new PesoRequest(pesoKg), Void.class);
    }
}
