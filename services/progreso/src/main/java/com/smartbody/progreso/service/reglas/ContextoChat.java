package com.smartbody.progreso.service.reglas;

import com.smartbody.progreso.dto.PerfilResponse;
import com.smartbody.progreso.dto.PlanNutricionalResponse;
import com.smartbody.progreso.dto.ProgresoResponse;
import com.smartbody.progreso.dto.RutinaResponse;

/**
 * Datos del usuario autenticado con los que el asistente personaliza sus respuestas.
 * Cualquier elemento puede ser null si el usuario aun no lo tiene (por ejemplo, sin rutina).
 */
public record ContextoChat(String nombre,
                           PerfilResponse perfil,
                           RutinaResponse rutina,
                           PlanNutricionalResponse plan,
                           Hidratacion hidratacion,
                           ProgresoResponse progreso) {

    public boolean perfilCompleto() {
        return perfil != null && perfil.completo();
    }

    public String primerNombre() {
        return nombre == null || nombre.isBlank() ? "" : nombre.trim().split("\\s+")[0];
    }
}
