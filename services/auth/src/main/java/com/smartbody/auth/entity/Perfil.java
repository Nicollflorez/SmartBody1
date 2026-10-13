package com.smartbody.auth.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Datos personales y fisicos del usuario (RF03, RF04). */
@Entity
@Table(name = "perfiles")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Column(name = "altura_cm")
    private Double alturaCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_experiencia", length = 20)
    private NivelExperiencia nivelExperiencia;

    @Column(name = "minutos_por_sesion")
    private Integer minutosPorSesion;

    @Column(name = "dias_por_semana")
    private Integer diasPorSemana;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "objetivo_id")
    private Objetivo objetivo;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @PreUpdate
    void marcarActualizacion() {
        this.actualizadoEn = LocalDateTime.now();
    }

    /** Indice de masa corporal, o null si faltan datos (RF03). */
    public Double calcularImc() {
        return calcularImc(pesoKg, alturaCm);
    }

    public static Double calcularImc(Double pesoKg, Double alturaCm) {
        if (pesoKg == null || alturaCm == null || alturaCm <= 0) {
            return null;
        }
        double alturaMetros = alturaCm / 100.0;
        return Math.round((pesoKg / (alturaMetros * alturaMetros)) * 100.0) / 100.0;
    }

    /** Rangos de referencia de la OMS para adultos. */
    public static String clasificarImc(Double imc) {
        if (imc == null) {
            return null;
        }
        if (imc < 18.5) {
            return "Bajo peso";
        }
        if (imc < 25) {
            return "Peso saludable";
        }
        if (imc < 30) {
            return "Sobrepeso";
        }
        return "Obesidad";
    }

    /** true cuando el perfil tiene lo minimo para generar una rutina. */
    public boolean estaCompleto() {
        return edad != null && pesoKg != null && alturaCm != null && nivelExperiencia != null
                && minutosPorSesion != null && diasPorSemana != null && objetivo != null;
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

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Double getAlturaCm() {
        return alturaCm;
    }

    public void setAlturaCm(Double alturaCm) {
        this.alturaCm = alturaCm;
    }

    public NivelExperiencia getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNivelExperiencia(NivelExperiencia nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }

    public Integer getMinutosPorSesion() {
        return minutosPorSesion;
    }

    public void setMinutosPorSesion(Integer minutosPorSesion) {
        this.minutosPorSesion = minutosPorSesion;
    }

    public Integer getDiasPorSemana() {
        return diasPorSemana;
    }

    public void setDiasPorSemana(Integer diasPorSemana) {
        this.diasPorSemana = diasPorSemana;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }
}
