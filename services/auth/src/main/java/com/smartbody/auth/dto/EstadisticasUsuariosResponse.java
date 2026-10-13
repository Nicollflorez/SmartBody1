package com.smartbody.auth.dto;

import java.util.Map;

/** Cifras de usuarios para el panel de administracion. */
public record EstadisticasUsuariosResponse(long totalUsuarios,
                                           long usuariosActivos,
                                           long nuevosUltimos30Dias,
                                           long perfilesCompletos,
                                           Map<String, Long> usuariosPorObjetivo,
                                           Map<String, Long> usuariosPorRestriccion) {
}
