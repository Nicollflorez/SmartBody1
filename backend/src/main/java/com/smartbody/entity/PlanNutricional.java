package com.smartbody.entity;

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

/** Plan nutricional diario generado para un usuario. */
@Entity
@Table(name = "planes_nutricionales")
public class PlanNutricional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "objetivo_id", nullable = false)
    private Objetivo objetivo;

    @Column(name = "calorias_objetivo")
    private Integer caloriasObjetivo;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "generado_en", nullable = false)
    private LocalDateTime generadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PlanAlimento> alimentos = new ArrayList<>();

    public void agregarAlimento(PlanAlimento alimento) {
        alimento.setPlan(this);
        alimentos.add(alimento);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public Integer getCaloriasObjetivo() {
        return caloriasObjetivo;
    }

    public void setCaloriasObjetivo(Integer caloriasObjetivo) {
        this.caloriasObjetivo = caloriasObjetivo;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public List<PlanAlimento> getAlimentos() {
        return alimentos;
    }
}
