package com.smartbody.service.reglas;

import com.smartbody.entity.Ejercicio;

import java.util.List;
import java.util.Set;

/**
 * Estrategia de generacion de rutinas. El MVP usa un motor de reglas; un agente IA
 * podra implementar esta misma interfaz desde el backend sin cambiar el resto del sistema.
 */
public interface GeneradorRutina {

    List<EjercicioPlanificado> generar(SolicitudRutina solicitud, List<Ejercicio> catalogo);

    /** Ejercicios compatibles que pueden reemplazar a {@code actual}, sin repetir los excluidos. */
    List<Ejercicio> alternativas(Ejercicio actual, Set<Long> excluidos, SolicitudRutina solicitud, List<Ejercicio> catalogo);

    /** Prescripcion del nuevo ejercicio conservando el dia, el orden y el tiempo del que reemplaza. */
    EjercicioPlanificado sustituir(EjercicioPlanificado actual, Ejercicio nuevo, SolicitudRutina solicitud);
}
