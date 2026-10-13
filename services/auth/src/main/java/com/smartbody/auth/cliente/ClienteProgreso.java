package com.smartbody.auth.cliente;

import com.smartbody.comun.cliente.ClienteServicios;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Registra en el servicio progreso la medida que nace de un cambio de peso o altura del perfil (RF10). */
@Component
public class ClienteProgreso {

    public record MedidaInterna(Double pesoKg, Double alturaCm) {
    }

    private final ClienteServicios cliente;
    private final String urlProgreso;

    public ClienteProgreso(ClienteServicios cliente, @Value("${smartbody.servicios.progreso}") String urlProgreso) {
        this.cliente = cliente;
        this.urlProgreso = urlProgreso;
    }

    public void registrarMedida(Long usuarioId, Double pesoKg, Double alturaCm) {
        cliente.interno().post(urlProgreso + "/internal/progreso/usuarios/" + usuarioId + "/medidas",
                new MedidaInterna(pesoKg, alturaCm), Void.class);
    }
}
