package com.smartbody.service;

import com.smartbody.dto.EjercicioResponse;
import com.smartbody.dto.ObjetivoResponse;
import com.smartbody.dto.RestriccionResponse;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.EjercicioRepository;
import com.smartbody.repository.ObjetivoRepository;
import com.smartbody.repository.RestriccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Catalogos de solo lectura: objetivos (RF04), restricciones (RF05) y ejercicios (RF08). */
@Service
public class CatalogoService {

    private final ObjetivoRepository objetivoRepository;
    private final RestriccionRepository restriccionRepository;
    private final EjercicioRepository ejercicioRepository;

    public CatalogoService(ObjetivoRepository objetivoRepository,
                           RestriccionRepository restriccionRepository,
                           EjercicioRepository ejercicioRepository) {
        this.objetivoRepository = objetivoRepository;
        this.restriccionRepository = restriccionRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    @Transactional(readOnly = true)
    public List<ObjetivoResponse> objetivos() {
        return objetivoRepository.findAll().stream().map(ObjetivoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<RestriccionResponse> restricciones() {
        return restriccionRepository.findAllByOrderByTipoAscNombreAsc().stream().map(RestriccionResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public EjercicioResponse ejercicio(Long id) {
        return ejercicioRepository.findById(id)
                .map(EjercicioResponse::desde)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el ejercicio solicitado."));
    }
}
