package com.smartbody.repository;

import com.smartbody.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // --- Panel de administracion ---

    long countByActivoTrue();

    long countByCreadoEnAfter(LocalDateTime fecha);

    List<Usuario> findTop20ByOrderByCreadoEnDesc();

    /** Usuarios con perfil, objetivo y roles en una sola consulta. */
    @Query("select distinct u from Usuario u left join fetch u.perfil p left join fetch p.objetivo "
            + "left join fetch u.roles order by u.creadoEn desc")
    List<Usuario> findAllParaAdministracion();

    @Query("select count(p) from Perfil p where p.edad is not null and p.pesoKg is not null and p.alturaCm is not null "
            + "and p.nivelExperiencia is not null and p.minutosPorSesion is not null and p.diasPorSemana is not null "
            + "and p.objetivo is not null")
    long contarPerfilesCompletos();

    @Query("select p.objetivo.nombre, count(p) from Perfil p where p.objetivo is not null "
            + "group by p.objetivo.nombre order by count(p) desc")
    List<Object[]> contarPorObjetivo();

    @Query("select r.nombre, count(u) from Usuario u join u.restricciones r group by r.nombre order by count(u) desc")
    List<Object[]> contarPorRestriccion();
}
