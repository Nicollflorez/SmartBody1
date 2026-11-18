package com.smartbody.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Estimacion de semanas hasta el peso objetivo, con su seguimiento posterior (mineria de datos). */
@Entity
@Table(name = "predicciones")
public class Prediccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 20)
    private String objetivo;

    @Column(name = "peso_inicial_kg", nullable = false)
    private Double pesoInicialKg;

    @Column(name = "peso_objetivo_kg", nullable = false)
    private Double pesoObjetivoKg;

    @Column(name = "altura_cm", nullable = false)
    private Double alturaCm;

    @Column(nullable = false)
    private Integer edad;

    @Column(nullable = false, length = 12)
    private String sexo;

    @Column(name = "semanas_estimadas", nullable = false)
    private Double semanasEstimadas;

    @Column(nullable = false, length = 40)
    private String modelo;

    @Column(name = "version_modelo", nullable = false, length = 20)
    private String versionModelo;

    @Column(name = "fecha_prediccion", nullable = false)
    private LocalDateTime fechaPrediccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_seguimiento", nullable = false, length = 30)
    private EstadoSeguimiento estadoSeguimiento = EstadoSeguimiento.PENDIENTE;

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public Double getPesoInicialKg() {
        return pesoInicialKg;
    }

    public void setPesoInicialKg(Double pesoInicialKg) {
        this.pesoInicialKg = pesoInicialKg;
    }

    public Double getPesoObjetivoKg() {
        return pesoObjetivoKg;
    }

    public void setPesoObjetivoKg(Double pesoObjetivoKg) {
        this.pesoObjetivoKg = pesoObjetivoKg;
    }

    public Double getAlturaCm() {
        return alturaCm;
    }

    public void setAlturaCm(Double alturaCm) {
        this.alturaCm = alturaCm;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Double getSemanasEstimadas() {
        return semanasEstimadas;
    }

    public void setSemanasEstimadas(Double semanasEstimadas) {
        this.semanasEstimadas = semanasEstimadas;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getVersionModelo() {
        return versionModelo;
    }

    public void setVersionModelo(String versionModelo) {
        this.versionModelo = versionModelo;
    }

    public LocalDateTime getFechaPrediccion() {
        return fechaPrediccion;
    }

    public void setFechaPrediccion(LocalDateTime fechaPrediccion) {
        this.fechaPrediccion = fechaPrediccion;
    }

    public EstadoSeguimiento getEstadoSeguimiento() {
        return estadoSeguimiento;
    }

    public void setEstadoSeguimiento(EstadoSeguimiento estadoSeguimiento) {
        this.estadoSeguimiento = estadoSeguimiento;
    }
}
