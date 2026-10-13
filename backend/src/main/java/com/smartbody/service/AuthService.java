package com.smartbody.service;

import com.smartbody.dto.AuthResponse;
import com.smartbody.dto.LoginRequest;
import com.smartbody.dto.RegistroRequest;
import com.smartbody.dto.UsuarioResponse;
import com.smartbody.entity.Rol;
import com.smartbody.entity.Usuario;
import com.smartbody.exception.ApiException;
import com.smartbody.repository.RolRepository;
import com.smartbody.repository.UsuarioRepository;
import com.smartbody.security.JwtService;
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
