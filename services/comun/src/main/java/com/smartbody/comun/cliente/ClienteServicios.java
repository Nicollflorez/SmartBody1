package com.smartbody.comun.cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbody.comun.error.ApiException;
import com.smartbody.comun.error.ErrorResponse;
import com.smartbody.comun.seguridad.TokenInternoFilter;
import com.smartbody.comun.seguridad.UsuarioAutenticado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

/**
 * Cliente HTTP entre microservicios.
 *
 * <ul>
 *   <li>{@link #comoUsuario()} reenvia el JWT de la peticion en curso: el servicio destino aplica
 *       sus propias reglas de autorizacion como si el usuario lo llamara directamente.</li>
 *   <li>{@link #interno()} envia el token de servicio para las rutas {@code /internal/**}.</li>
 * </ul>
 * Las respuestas de error {@link ErrorResponse} se convierten en {@link ApiException} con el mismo
 * codigo y mensaje, de modo que llegan al cliente final sin cambios.
 */
@Component
public class ClienteServicios {

    private static final Logger log = LoggerFactory.getLogger(ClienteServicios.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String tokenInterno;

    public ClienteServicios(RestClient.Builder builder, ObjectMapper objectMapper,
                            @Value("${smartbody.interno.token:}") String tokenInterno) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
        this.tokenInterno = tokenInterno;
    }

    /** Llamada con el JWT del usuario autenticado en la peticion en curso. */
    public Llamada comoUsuario() {
        UsuarioAutenticado usuario = UsuarioAutenticado.actual();
        if (usuario == null) {
            throw ApiException.noAutenticado("Debes iniciar sesión para continuar.");
        }
        return new Llamada(HttpHeaders.AUTHORIZATION, "Bearer " + usuario.token());
    }

    /** Llamada con un JWT concreto (por ejemplo, el recien emitido en el registro). */
    public Llamada conToken(String token) {
        return new Llamada(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    /** Llamada de servicio a servicio a una ruta /internal/**. */
    public Llamada interno() {
        return new Llamada(TokenInternoFilter.CABECERA, tokenInterno);
    }

    public final class Llamada {
        private final String cabecera;
        private final String valor;

        private Llamada(String cabecera, String valor) {
            this.cabecera = cabecera;
            this.valor = valor;
        }

        public <T> T get(String url, Class<T> tipo) {
            return ejecutar(() -> restClient.get().uri(url).header(cabecera, valor).retrieve().body(tipo));
        }

        /** GET que devuelve vacio cuando el destino responde 404 (por ejemplo "sin rutina activa"). */
        public <T> Optional<T> getSiExiste(String url, Class<T> tipo) {
            try {
                return Optional.ofNullable(get(url, tipo));
            } catch (ApiException e) {
                if (e.getEstado() == HttpStatus.NOT_FOUND) {
                    return Optional.empty();
                }
                throw e;
            }
        }

        public <T> T post(String url, Object cuerpo, Class<T> tipo) {
            return ejecutar(() -> {
                var peticion = restClient.post().uri(url).header(cabecera, valor);
                if (cuerpo != null) {
                    peticion = peticion.contentType(MediaType.APPLICATION_JSON).body(cuerpo);
                }
                return peticion.retrieve().body(tipo);
            });
        }

        public <T> T put(String url, Object cuerpo, Class<T> tipo) {
            return ejecutar(() -> restClient.put().uri(url).header(cabecera, valor)
                    .contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(tipo));
        }

        private <T> T ejecutar(java.util.function.Supplier<T> llamada) {
            try {
                return llamada.get();
            } catch (RestClientResponseException e) {
                throw traducir(e);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                log.error("Servicio no disponible: {}", e.getMessage());
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Un servicio no está disponible en este momento. Intenta nuevamente más tarde.");
            }
        }
    }

    private ApiException traducir(RestClientResponseException e) {
        HttpStatus estado = HttpStatus.resolve(e.getStatusCode().value());
        String mensaje = "Ocurrió un error en el servidor. Intenta nuevamente más tarde.";
        try {
            ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            if (error != null && error.mensaje() != null) {
                mensaje = error.mensaje();
            }
        } catch (Exception ignorada) {
            log.debug("Respuesta de error sin formato ErrorResponse: {}", e.getResponseBodyAsString());
        }
        return new ApiException(estado == null ? HttpStatus.BAD_GATEWAY : estado, mensaje);
    }
}
