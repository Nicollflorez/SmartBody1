package com.smartbody.planes.repository;

import com.smartbody.planes.entity.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    /** Catalogo completo con sus contraindicaciones en una sola consulta. */
    @Query("select distinct e from Ejercicio e left join fetch e.contraindicaciones")
    List<Ejercicio> findAllConContraindicaciones();
}
