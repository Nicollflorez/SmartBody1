package com.smartbody.planes.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Rutina de entrenamiento generada para un usuario (RF06, RF07). */
@Entity
@Table(name = "rutinas")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del usuario (tabla en el servicio auth: sin clave foranea entre esquemas). */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "objetivo_id", nullable = false)
    private Objetivo objetivo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "dias_por_semana", nullable = false)
    private int diasPorSemana;

    @Column(name = "minutos_por_sesion", nullable = false)
    private int minutosPorSesion;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "generada_en", nullable = false)
    private LocalDateTime generadaEn = LocalDateTime.now();

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("diaNumero ASC, orden ASC")
    private List<RutinaEjercicio> ejercicios = new ArrayList<>();

    public void agregarEjercicio(RutinaEjercicio ejercicio) {
        ejercicio.setRutina(this);
        ejercicios.add(ejercicio);
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getDiasPorSemana() {
        return diasPorSemana;
    }

    public void setDiasPorSemana(int diasPorSemana) {
        this.diasPorSemana = diasPorSemana;
    }

    public int getMinutosPorSesion() {
        return minutosPorSesion;
    }

    public void setMinutosPorSesion(int minutosPorSesion) {
        this.minutosPorSesion = minutosPorSesion;
    }

    public boolean isActiva() {
        return activa;
    }

    public LocalDateTime getGeneradaEn() {
        return generadaEn;
    }

    public List<RutinaEjercicio> getEjercicios() {
        return ejercicios;
    }
}
