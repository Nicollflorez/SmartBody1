package com.smartbody.progreso.repository;

import com.smartbody.progreso.entity.Medida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MedidaRepository extends JpaRepository<Medida, Long> {

    List<Medida> findByUsuarioIdOrderByRegistradoEnAsc(Long usuarioId);

    // --- Panel de administracion ---

    @Query("select m from Medida m order by m.registradoEn desc limit 20")
    List<Medida> findRecientes();

    @Query("select m.usuarioId, count(m) from Medida m group by m.usuarioId")
    List<Object[]> contarPorUsuario();

    List<Medida> findAllByOrderByRegistradoEnAsc();
}
