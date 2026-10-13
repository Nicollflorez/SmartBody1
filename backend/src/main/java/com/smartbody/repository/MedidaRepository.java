package com.smartbody.repository;

import com.smartbody.entity.Medida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MedidaRepository extends JpaRepository<Medida, Long> {

    List<Medida> findByUsuarioIdOrderByRegistradoEnAsc(Long usuarioId);

    // --- Panel de administracion ---

    @Query("select m from Medida m join fetch m.usuario order by m.registradoEn desc limit 20")
    List<Medida> findRecientesConUsuario();

    @Query("select m.usuario.id, count(m) from Medida m group by m.usuario.id")
    List<Object[]> contarPorUsuario();

    List<Medida> findAllByOrderByRegistradoEnAsc();
}
