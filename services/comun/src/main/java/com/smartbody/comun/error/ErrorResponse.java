package com.smartbody.comun.error;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de error de la API. El mensaje esta pensado para mostrarse al usuario final:
 * nunca expone detalles tecnicos (stack traces, SQL, etc.).
 */
public record ErrorResponse(int estado,
                            String mensaje,
                            Map<String, String> errores,
                            LocalDateTime momento) {

    public static ErrorResponse de(int estado, String mensaje) {
        return new ErrorResponse(estado, mensaje, null, LocalDateTime.now());
    }

    public static ErrorResponse de(int estado, String mensaje, Map<String, String> errores) {
        return new ErrorResponse(estado, mensaje, errores, LocalDateTime.now());
    }
}
