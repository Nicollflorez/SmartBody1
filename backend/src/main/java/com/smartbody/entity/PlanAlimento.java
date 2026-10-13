package com.smartbody.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Alimento recomendado dentro de una comida del plan nutricional. */
@Entity
@Table(name = "plan_alimentos")
public class PlanAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanNutricional plan;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "alimento_id", nullable = false)
    private Alimento alimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MomentoComida momento;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false, length = 20)
    private String unidad = "g";

    /** Aporte de un nutriente (expresado por 100 g) segun la cantidad recomendada. */
    public double aporte(Double valorPor100g) {
        return valorPor100g == null ? 0 : valorPor100g * cantidad / 100.0;
    }

    public Long getId() {
        return id;
    }

    public PlanNutricional getPlan() {
        return plan;
    }

    void setPlan(PlanNutricional plan) {
        this.plan = plan;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public MomentoComida getMomento() {
        return momento;
    }

    public void setMomento(MomentoComida momento) {
        this.momento = momento;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }
}
