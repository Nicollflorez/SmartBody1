package com.smartbody.progreso.repository;

import com.smartbody.progreso.entity.Prediccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrediccionRepository extends JpaRepository<Prediccion, Long> {

    List<Prediccion> findByUsuarioIdOrderByFechaPrediccionDesc(Long usuarioId);

    /** Solo devuelve la prediccion si pertenece al usuario: evita exponer datos de otros usuarios. */
    Optional<Prediccion> findByIdAndUsuarioId(Long id, Long usuarioId);
}
