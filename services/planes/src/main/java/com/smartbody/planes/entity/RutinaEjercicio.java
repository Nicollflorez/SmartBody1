package com.smartbody.planes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Ejercicio asignado a un dia de una rutina, con su prescripcion (series, repeticiones, descanso). */
@Entity
@Table(name = "rutina_ejercicios")
public class RutinaEjercicio {

    /** Tiempo de trabajo estimado por serie cuando el ejercicio se mide en repeticiones. */
    public static final int SEGUNDOS_POR_SERIE = 45;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rutina_id", nullable = false)
    private Rutina rutina;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(name = "dia_numero", nullable = false)
    private int diaNumero;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private int series;

    private Integer repeticiones;

    @Column(name = "duracion_seg")
    private Integer duracionSeg;

    @Column(name = "descanso_seg", nullable = false)
    private int descansoSeg;

    /** Duracion aproximada de un ejercicio: cada serie suma su trabajo y su descanso. */
    public static int estimarSegundos(int series, Integer duracionSeg, int descansoSeg) {
        int trabajo = duracionSeg != null ? duracionSeg : SEGUNDOS_POR_SERIE;
        return series * (trabajo + descansoSeg);
    }

    public int segundosEstimados() {
        return estimarSegundos(series, duracionSeg, descansoSeg);
    }

    public Long getId() {
        return id;
    }

    public Rutina getRutina() {
        return rutina;
    }

    void setRutina(Rutina rutina) {
        this.rutina = rutina;
    }

    public Ejercicio getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Ejercicio ejercicio) {
        this.ejercicio = ejercicio;
    }

    public int getDiaNumero() {
        return diaNumero;
    }

    public void setDiaNumero(int diaNumero) {
        this.diaNumero = diaNumero;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    public Integer getDuracionSeg() {
        return duracionSeg;
    }

    public void setDuracionSeg(Integer duracionSeg) {
        this.duracionSeg = duracionSeg;
    }

    public int getDescansoSeg() {
        return descansoSeg;
    }

    public void setDescansoSeg(int descansoSeg) {
        this.descansoSeg = descansoSeg;
    }
}
