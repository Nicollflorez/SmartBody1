package com.smartbody.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Foto de perfil del usuario. Se guarda aparte para no cargar la imagen en cada consulta del usuario. */
@Entity
@Table(name = "fotos_perfil")
public class FotoPerfil {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Lob
    @Column(nullable = false)
    private byte[] contenido;

    @Column(name = "tipo_contenido", nullable = false, length = 30)
    private String tipoContenido;

    @Column(name = "actualizada_en", nullable = false)
    private LocalDateTime actualizadaEn = LocalDateTime.now();

    protected FotoPerfil() {
    }

    public FotoPerfil(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void reemplazar(byte[] contenido, String tipoContenido) {
        this.contenido = contenido;
        this.tipoContenido = tipoContenido;
        this.actualizadaEn = LocalDateTime.now();
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public LocalDateTime getActualizadaEn() {
        return actualizadaEn;
    }
}
