package com.smartbody.repository;

import com.smartbody.entity.Restriccion;
import com.smartbody.entity.TipoRestriccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestriccionRepository extends JpaRepository<Restriccion, Long> {

    List<Restriccion> findByTipoOrderByNombreAsc(TipoRestriccion tipo);

    List<Restriccion> findAllByOrderByTipoAscNombreAsc();
}
