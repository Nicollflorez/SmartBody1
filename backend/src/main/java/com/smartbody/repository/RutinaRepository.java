package com.smartbody.repository;

import com.smartbody.entity.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Todas las consultas filtran por usuario: nadie puede ver rutinas ajenas (RNF06). */
public interface RutinaRepository extends JpaRepository<Rutina, Long> {

    Optional<Rutina> findFirstByUsuarioIdAndActivaTrueOrderByGeneradaEnDesc(Long usuarioId);

    Optional<Rutina> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Rutina> findByUsuarioIdOrderByGeneradaEnDesc(Long usuarioId);

    @Modifying
    @Query("update Rutina r set r.activa = false where r.usuario.id = :usuarioId and r.activa = true")
    int desactivarRutinasActivas(@Param("usuarioId") Long usuarioId);

    // --- Panel de administracion ---

    @Query("select r from Rutina r join fetch r.usuario order by r.generadaEn desc limit 20")
    List<Rutina> findRecientesConUsuario();

    @Query("select r.usuario.id, count(r) from Rutina r group by r.usuario.id")
    List<Object[]> contarPorUsuario();
}
