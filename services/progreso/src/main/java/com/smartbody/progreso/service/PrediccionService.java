package com.smartbody.progreso.service;

import com.smartbody.comun.error.ApiException;
import com.smartbody.progreso.dto.PrediccionRequest;
import com.smartbody.progreso.dto.PrediccionResponse;
import com.smartbody.progreso.dto.PrediccionResumenResponse;
import com.smartbody.progreso.dto.SeguimientoPrediccionResponse;
import com.smartbody.progreso.entity.EstadoSeguimiento;
import com.smartbody.progreso.entity.Medida;
import com.smartbody.progreso.entity.Prediccion;
import com.smartbody.progreso.repository.MedidaRepository;
import com.smartbody.progreso.repository.PrediccionRepository;
import com.smartbody.progreso.service.prediccion.ModeloPrediccion;
import com.smartbody.progreso.service.prediccion.ReglasPrediccion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Estimación de semanas hasta el peso objetivo y seguimiento frente a las medidas reales. */
@Service
public class PrediccionService {

    private static final Logger log = LoggerFactory.getLogger(PrediccionService.class);
    private static final String MODELO_NO_DISPONIBLE = "El modelo de predicción no está disponible en este momento.";
    private static final String CALCULO_FALLIDO = "No se pudo calcular la estimación. Intenta más tarde.";

    private final ModeloPrediccion modelo;
    private final PrediccionRepository predicciones;
    private final MedidaRepository medidas;

    public PrediccionService(ModeloPrediccion modelo, PrediccionRepository predicciones, MedidaRepository medidas) {
        this.modelo = modelo;
        this.predicciones = predicciones;
        this.medidas = medidas;
    }

    @Transactional
    public PrediccionResponse predecir(Long usuarioId, PrediccionRequest peticion) {
        if (!modelo.disponible()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, MODELO_NO_DISPONIBLE);
        }
        ReglasPrediccion.Derivados derivados = ReglasPrediccion.validarYDerivar(peticion);

        double semanas;
        try {
            semanas = modelo.predecir(ReglasPrediccion.aAtributos(peticion, derivados));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Fallo al calcular la predicción del usuario {}: {}", usuarioId, e.getMessage(), e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, CALCULO_FALLIDO);
        }
        if (Double.isNaN(semanas) || Double.isInfinite(semanas) || semanas <= 0) {
            log.error("El modelo devolvió un valor no válido: {}", semanas);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, CALCULO_FALLIDO);
        }

        Prediccion p = new Prediccion();
        p.setUsuarioId(usuarioId);
        p.setObjetivo(peticion.objetivo());
        p.setPesoInicialKg(peticion.pesoInicial());
        p.setPesoObjetivoKg(peticion.pesoObjetivo());
        p.setAlturaCm(peticion.alturaCm());
        p.setEdad(peticion.edad());
        p.setSexo(peticion.sexo());
        p.setSemanasEstimadas(redondear1(semanas));
        p.setModelo(ModeloPrediccion.NOMBRE);
        p.setVersionModelo(ModeloPrediccion.VERSION);
        p.setFechaPrediccion(LocalDateTime.now());
        p.setEstadoSeguimiento(EstadoSeguimiento.PENDIENTE);
        return PrediccionResponse.desde(predicciones.save(p));
    }

    @Transactional(readOnly = true)
    public List<PrediccionResumenResponse> historial(Long usuarioId) {
        return predicciones.findByUsuarioIdOrderByFechaPrediccionDesc(usuarioId).stream()
                .map(PrediccionResumenResponse::desde)
                .toList();
    }

    /** Compara la estimación con las medidas registradas después de la predicción. */
    @Transactional
    public SeguimientoPrediccionResponse seguimiento(Long usuarioId, Long id) {
        Prediccion p = predicciones.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("No existe una predicción con ese identificador."));

        List<Medida> desde = medidas.findByUsuarioIdOrderByRegistradoEnAsc(usuarioId).stream()
                .filter(m -> !m.getRegistradoEn().isBefore(p.getFechaPrediccion()))
                .toList();
        if (desde.isEmpty()) {
            return SeguimientoPrediccionResponse.sinMedidas(p);
        }

        boolean perder = "PERDER_PESO".equals(p.getObjetivo());
        double inicial = p.getPesoInicialKg();
        double objetivo = p.getPesoObjetivoKg();
        double recorrido = Math.abs(inicial - objetivo);

        Medida ultima = desde.get(desde.size() - 1);
        double pesoActual = ultima.getPesoKg();
        // El tiempo transcurrido se mide hasta hoy: si el usuario tarda en registrar su peso, el ritmo y
        // las semanas restantes deben reflejarlo, no quedarse congelados en la ultima medida.
        double semanasTranscurridas = semanas(p.getFechaPrediccion(), LocalDateTime.now());
        double avance = perder ? (inicial - pesoActual) / recorrido : (pesoActual - inicial) / recorrido;
        double avancePct = Math.round(Math.max(0, Math.min(1, avance)) * 1000) / 10.0;
        // Un ritmo en kg/semana solo tiene sentido tras al menos una semana; antes un denominador casi
        // cero produce valores absurdos.
        Double ritmo = semanasTranscurridas >= 1
                ? redondear2((perder ? inicial - pesoActual : pesoActual - inicial) / semanasTranscurridas)
                : null;

        Medida primeraEnObjetivo = desde.stream()
                .filter(m -> perder ? m.getPesoKg() <= objetivo : m.getPesoKg() >= objetivo)
                .findFirst()
                .orElse(null);

        Double semanasHastaObjetivo = null;
        Double diferencia = null;
        Double restantes = null;
        EstadoSeguimiento estado = EstadoSeguimiento.EN_SEGUIMIENTO;
        if (primeraEnObjetivo != null) {
            estado = EstadoSeguimiento.OBJETIVO_ALCANZADO;
            semanasHastaObjetivo = redondear1(semanas(p.getFechaPrediccion(), primeraEnObjetivo.getRegistradoEn()));
            diferencia = redondear1(semanasHastaObjetivo - p.getSemanasEstimadas());
        } else {
            restantes = redondear1(Math.max(0, p.getSemanasEstimadas() - semanasTranscurridas));
        }
        p.setEstadoSeguimiento(estado);

        List<SeguimientoPrediccionResponse.PuntoPeso> puntos = desde.stream()
                .map(m -> new SeguimientoPrediccionResponse.PuntoPeso(m.getRegistradoEn(), m.getPesoKg()))
                .toList();

        return new SeguimientoPrediccionResponse(p.getId(), p.getObjetivo(), inicial, objetivo, p.getSemanasEstimadas(),
                pesoActual, redondear1(semanasTranscurridas), avancePct, ritmo, estado, semanasHastaObjetivo,
                diferencia, restantes, ultima.getRegistradoEn(), puntos, PrediccionResponse.AVISO);
    }

    static double semanas(LocalDateTime desde, LocalDateTime hasta) {
        return Duration.between(desde, hasta).toMinutes() / (7.0 * 24 * 60);
    }

    private static double redondear1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private static double redondear2(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
