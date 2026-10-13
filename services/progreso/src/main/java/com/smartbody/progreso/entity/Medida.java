package com.smartbody.progreso.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Registro historico de medidas corporales (RF10). */
@Entity
@Table(name = "medidas")
public class Medida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del usuario (tabla en el servicio auth: sin clave foranea entre esquemas). */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "peso_kg", nullable = false)
    private Double pesoKg;

    @Column(name = "altura_cm")
    private Double alturaCm;

    @Column(name = "cintura_cm")
    private Double cinturaCm;

    @Column(name = "cadera_cm")
    private Double caderaCm;

    @Column(name = "pecho_cm")
    private Double pechoCm;

    @Column(name = "brazo_cm")
    private Double brazoCm;

    private Double imc;

    @Column(name = "registrado_en", nullable = false)
    private LocalDateTime registradoEn = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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

    public Double getCinturaCm() {
        return cinturaCm;
    }

    public void setCinturaCm(Double cinturaCm) {
        this.cinturaCm = cinturaCm;
    }

    public Double getCaderaCm() {
        return caderaCm;
    }

    public void setCaderaCm(Double caderaCm) {
        this.caderaCm = caderaCm;
    }

    public Double getPechoCm() {
        return pechoCm;
    }

    public void setPechoCm(Double pechoCm) {
        this.pechoCm = pechoCm;
    }

    public Double getBrazoCm() {
        return brazoCm;
    }

    public void setBrazoCm(Double brazoCm) {
        this.brazoCm = brazoCm;
    }

    public Double getImc() {
        return imc;
    }

    public void setImc(Double imc) {
        this.imc = imc;
    }

    public LocalDateTime getRegistradoEn() {
        return registradoEn;
    }
}
