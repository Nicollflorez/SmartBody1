package com.smartbody.planes.repository;

import com.smartbody.planes.entity.PlanNutricional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Todas las consultas filtran por usuario: nadie puede ver planes ajenos (RNF06). */
public interface PlanNutricionalRepository extends JpaRepository<PlanNutricional, Long> {

    Optional<PlanNutricional> findFirstByUsuarioIdAndActivoTrueOrderByGeneradoEnDesc(Long usuarioId);

    Optional<PlanNutricional> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Modifying
    @Query("update PlanNutricional p set p.activo = false where p.usuarioId = :usuarioId and p.activo = true")
    int desactivarPlanesActivos(@Param("usuarioId") Long usuarioId);

    // --- Panel de administracion ---

    @Query("select p from PlanNutricional p order by p.generadoEn desc limit 20")
    List<PlanNutricional> findRecientes();
}
