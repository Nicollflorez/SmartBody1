package com.smartbody.auth.service;

import com.smartbody.auth.dto.UsuarioResponse;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.comun.error.ApiException;
import com.smartbody.auth.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Entidad del usuario autenticado; los datos de un usuario nunca se cruzan con los de otro. */
    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la cuenta del usuario."));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse datosBasicos(String email) {
        return UsuarioResponse.desde(buscarPorEmail(email));
    }
}
