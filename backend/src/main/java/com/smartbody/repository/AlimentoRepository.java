package com.smartbody.repository;

import com.smartbody.entity.Alimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlimentoRepository extends JpaRepository<Alimento, Long> {

    /** Catalogo completo con sus incompatibilidades en una sola consulta. */
    @Query("select distinct a from Alimento a left join fetch a.incompatibilidades")
    List<Alimento> findAllConIncompatibilidades();
}
