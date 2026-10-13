package com.smartbody.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Catalogo de alimentos del plan nutricional. Los valores nutricionales son por cada 100 g. */
@Entity
@Table(name = "alimentos")
public class Alimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String grupo;

    @Column(name = "calorias_100g", nullable = false)
    private Double caloriasPor100g;

    @Column(name = "proteina_g", nullable = false)
    private Double proteinaG;

    @Column(name = "carbohidratos_g", nullable = false)
    private Double carbohidratosG;

    @Column(name = "grasas_g", nullable = false)
    private Double grasasG;

    @Column(length = 255)
    private String descripcion;

    /** Instruccion breve de preparacion, usada para armar las recetas de cada comida. */
    @Column(length = 500)
    private String preparacion;

    /** Emoji ilustrativo del alimento para la interfaz. */
    @Column(length = 16)
    private String icono;

    /** Comidas en las que se recomienda, separadas por coma (por ejemplo "DESAYUNO,SNACK"). Null: cualquiera. */
    @Column(length = 60)
    private String momentos;

    public boolean esAdecuadoPara(MomentoComida momento) {
        return momentos == null || momentos.isBlank()
                || Arrays.stream(momentos.split(",")).map(String::trim).anyMatch(m -> m.equals(momento.name()));
    }

    /** Restricciones alimentarias con las que el alimento es incompatible. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "alimento_restricciones",
            joinColumns = @JoinColumn(name = "alimento_id"),
            inverseJoinColumns = @JoinColumn(name = "restriccion_id"))
    private Set<Restriccion> incompatibilidades = new HashSet<>();

    public boolean esIncompatibleCon(Set<Long> restriccionIds) {
        return incompatibilidades.stream().anyMatch(r -> restriccionIds.contains(r.getId()));
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupo() {
        return grupo;
    }

    public Double getCaloriasPor100g() {
        return caloriasPor100g;
    }

    public Double getProteinaG() {
        return proteinaG;
    }

    public Double getCarbohidratosG() {
        return carbohidratosG;
    }

    public Double getGrasasG() {
        return grasasG;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPreparacion() {
        return preparacion;
    }

    public String getIcono() {
        return icono;
    }

    public Set<Restriccion> getIncompatibilidades() {
        return incompatibilidades;
    }
}
