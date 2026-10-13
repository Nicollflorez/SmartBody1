package com.smartbody.auth.service;

import com.smartbody.auth.dto.AuthResponse;
import com.smartbody.auth.dto.LoginRequest;
import com.smartbody.auth.dto.RegistroRequest;
import com.smartbody.auth.dto.UsuarioResponse;
import com.smartbody.auth.entity.Rol;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.comun.error.ApiException;
import com.smartbody.auth.repository.RolRepository;
import com.smartbody.auth.repository.UsuarioRepository;
import com.smartbody.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF01 y RF02: registro e inicio de sesion. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest peticion) {
        String email = peticion.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("Ya existe una cuenta registrada con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(peticion.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(peticion.password()));
        usuario.agregarRol(rolUsuario());

        Usuario guardado = usuarioRepository.save(usuario);
        return construirRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest peticion) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(peticion.email().trim())
                .orElseThrow(() -> ApiException.noAutenticado("Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(peticion.password(), usuario.getPasswordHash())) {
            throw ApiException.noAutenticado("Correo o contraseña incorrectos.");
        }
        if (!usuario.isActivo()) {
            throw ApiException.noAutenticado("La cuenta se encuentra desactivada.");
        }
        return construirRespuesta(usuario);
    }

    private AuthResponse construirRespuesta(Usuario usuario) {
        String token = jwtService.generarToken(usuario);
        return AuthResponse.de(token, jwtService.segundosDeValidez(), UsuarioResponse.desde(usuario));
    }

    private Rol rolUsuario() {
        return rolRepository.findByNombre(Rol.USUARIO)
                .orElseGet(() -> rolRepository.save(new Rol(Rol.USUARIO)));
    }
}
