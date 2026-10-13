package com.smartbody.planes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Catalogo de ejercicios (RF06, RF08, RF09). */
@Entity
@Table(name = "ejercicios")
public class Ejercicio {

    /** A partir de esta duracion un ejercicio de cardio se realiza de forma continua y no por intervalos. */
    private static final int DURACION_CARDIO_CONTINUO_SEG = 600;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "grupo_muscular", nullable = false, length = 60)
    private String grupoMuscular;

    @Column(nullable = false, length = 60)
    private String equipamiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEjercicio tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_minimo", nullable = false, length = 20)
    private NivelExperiencia nivelMinimo;

    @Column(name = "duracion_estimada_seg", nullable = false)
    private int duracionEstimadaSeg;

    @Column(length = 1000)
    private String instrucciones;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    /** Restricciones de salud para las que el ejercicio esta contraindicado. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ejercicio_restricciones",
            joinColumns = @JoinColumn(name = "ejercicio_id"),
            inverseJoinColumns = @JoinColumn(name = "restriccion_id"))
    private Set<Restriccion> contraindicaciones = new HashSet<>();

    public boolean estaContraindicadoPara(Set<Long> restriccionIds) {
        return contraindicaciones.stream().anyMatch(r -> restriccionIds.contains(r.getId()));
    }

    /** Ejercicios que se sostienen en el tiempo (planchas) en lugar de contar repeticiones. */
    public boolean esIsometrico() {
        return nombre.toLowerCase(Locale.ROOT).contains("plancha");
    }

    public boolean esCardioContinuo() {
        return tipo == TipoEjercicio.CARDIO && duracionEstimadaSeg >= DURACION_CARDIO_CONTINUO_SEG;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public String getEquipamiento() {
        return equipamiento;
    }

    public TipoEjercicio getTipo() {
        return tipo;
    }

    public NivelExperiencia getNivelMinimo() {
        return nivelMinimo;
    }

    public int getDuracionEstimadaSeg() {
        return duracionEstimadaSeg;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public Set<Restriccion> getContraindicaciones() {
        return contraindicaciones;
    }
}
