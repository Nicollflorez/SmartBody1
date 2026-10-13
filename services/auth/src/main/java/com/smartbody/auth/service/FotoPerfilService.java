package com.smartbody.auth.service;

import com.smartbody.auth.entity.FotoPerfil;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.comun.error.ApiException;
import com.smartbody.auth.repository.FotoPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Foto de perfil: cada usuario solo puede ver, cambiar o quitar la suya (RNF06). */
@Service
public class FotoPerfilService {

    /** Limite por debajo del max_allowed_packet de 1 MB que trae MySQL/MariaDB en XAMPP. */
    static final long TAMANO_MAXIMO_BYTES = 700 * 1024;

    private final UsuarioService usuarioService;
    private final FotoPerfilRepository fotoPerfilRepository;

    public FotoPerfilService(UsuarioService usuarioService, FotoPerfilRepository fotoPerfilRepository) {
        this.usuarioService = usuarioService;
        this.fotoPerfilRepository = fotoPerfilRepository;
    }

    @Transactional
    public void guardar(String email, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw ApiException.solicitudInvalida("Selecciona una foto.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw ApiException.solicitudInvalida("La foto no puede superar 700 KB.");
        }

        byte[] contenido = leer(archivo);
        // El tipo se decide por el contenido real del archivo, no por lo que declara el cliente.
        String tipo = tipoDeImagen(contenido);
        if (tipo == null) {
            throw ApiException.solicitudInvalida("El archivo debe ser una imagen JPG, PNG o WEBP.");
        }

        Usuario usuario = usuarioService.buscarPorEmail(email);
        FotoPerfil foto = fotoPerfilRepository.findById(usuario.getId())
                .orElseGet(() -> new FotoPerfil(usuario.getId()));
        foto.reemplazar(contenido, tipo);
        fotoPerfilRepository.save(foto);
    }

    @Transactional(readOnly = true)
    public FotoPerfil obtener(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return fotoPerfilRepository.findById(usuario.getId())
                .orElseThrow(() -> ApiException.noEncontrado("Aún no tienes foto de perfil."));
    }

    @Transactional
    public void eliminar(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        if (fotoPerfilRepository.existsById(usuario.getId())) {
            fotoPerfilRepository.deleteById(usuario.getId());
        }
    }

    /** Reconoce JPG, PNG y WEBP por sus bytes iniciales. */
    static String tipoDeImagen(byte[] contenido) {
        if (contenido.length >= 3
                && (contenido[0] & 0xFF) == 0xFF && (contenido[1] & 0xFF) == 0xD8 && (contenido[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (contenido.length >= 8 && Arrays.equals(Arrays.copyOfRange(contenido, 0, 8),
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A})) {
            return "image/png";
        }
        if (contenido.length >= 12
                && "RIFF".equals(new String(contenido, 0, 4, StandardCharsets.US_ASCII))
                && "WEBP".equals(new String(contenido, 8, 4, StandardCharsets.US_ASCII))) {
            return "image/webp";
        }
        return null;
    }

    private static byte[] leer(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw ApiException.solicitudInvalida("No fue posible leer la foto. Intenta con otra imagen.");
        }
    }
}
