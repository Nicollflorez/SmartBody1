package com.smartbody.service;

import com.smartbody.entity.FotoPerfil;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.FotoPerfilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas de la foto de perfil: solo imagenes reales, con tamano limitado y por usuario. */
class FotoPerfilServiceTest {

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};

    private FotoPerfilRepository repositorio;
    private FotoPerfilService servicio;

    @BeforeEach
    void preparar() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        repositorio = mock(FotoPerfilRepository.class);
        servicio = new FotoPerfilService(usuarioService, repositorio);

        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 7L);
        when(usuarioService.buscarPorEmail("ana@smartbody.com")).thenReturn(usuario);
    }

    @Test
    void guardaUnaFotoJpgDelUsuario() {
        when(repositorio.findById(7L)).thenReturn(Optional.empty());

        servicio.guardar("ana@smartbody.com", new MockMultipartFile("foto", "yo.jpg", "image/jpeg", JPG));

        ArgumentCaptor<FotoPerfil> guardada = ArgumentCaptor.forClass(FotoPerfil.class);
        verify(repositorio).save(guardada.capture());
        assertThat(guardada.getValue().getUsuarioId()).isEqualTo(7L);
        assertThat(guardada.getValue().getTipoContenido()).isEqualTo("image/jpeg");
    }

    @Test
    void detectaElTipoPorElContenidoYNoPorLaExtension() {
        assertThat(FotoPerfilService.tipoDeImagen(PNG)).isEqualTo("image/png");
        assertThat(FotoPerfilService.tipoDeImagen("hola".getBytes())).isNull();
    }

    @Test
    void rechazaArchivosQueNoSonImagenesAunqueDigan_ser_jpg() {
        MockMultipartFile falsa = new MockMultipartFile("foto", "virus.jpg", "image/jpeg", "<script>".getBytes());

        assertThatThrownBy(() -> servicio.guardar("ana@smartbody.com", falsa))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("JPG, PNG o WEBP");
        verify(repositorio, never()).save(any());
    }

    @Test
    void rechazaFotosDemasiadoGrandes() {
        byte[] grande = new byte[(int) FotoPerfilService.TAMANO_MAXIMO_BYTES + 1];
        System.arraycopy(JPG, 0, grande, 0, JPG.length);

        assertThatThrownBy(() -> servicio.guardar("ana@smartbody.com", new MockMultipartFile("foto", "g.jpg", "image/jpeg", grande)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("700 KB");
    }

    @Test
    void sinFotoRespondeNoEncontrado() {
        when(repositorio.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener("ana@smartbody.com"))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getEstado())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
