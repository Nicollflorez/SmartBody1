package com.smartbody.repository;

import com.smartbody.entity.Objetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ObjetivoRepository extends JpaRepository<Objetivo, Long> {

    Optional<Objetivo> findByCodigo(String codigo);
}
