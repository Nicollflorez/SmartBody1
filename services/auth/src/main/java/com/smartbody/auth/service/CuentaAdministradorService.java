package com.smartbody.auth.service;

import com.smartbody.auth.entity.Rol;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.auth.repository.RolRepository;
import com.smartbody.auth.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta de administrador a partir de variables de entorno. Si la cuenta ya existe solo
 * se asegura de que tenga el rol de administrador: nunca cambia una contrasena existente.
 */
@Service
public class CuentaAdministradorService {

    private static final Logger log = LoggerFactory.getLogger(CuentaAdministradorService.class);
    static final int LONGITUD_MINIMA_PASSWORD = 10;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaAdministradorService(UsuarioRepository usuarioRepository,
                                      RolRepository rolRepository,
                                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void asegurarAdministrador(String email, String password, String nombre) {
        if (email == null || email.isBlank()) {
            return;
        }
        String correo = email.trim().toLowerCase();
        Rol rolAdministrador = rol(Rol.ADMINISTRADOR);

        Usuario existente = usuarioRepository.findByEmailIgnoreCase(correo).orElse(null);
        if (existente != null) {
            if (!existente.esAdministrador()) {
                existente.agregarRol(rolAdministrador);
                log.info("Se asigno el rol de administrador a {}", correo);
            }
            return;
        }

        if (password == null || password.length() < LONGITUD_MINIMA_PASSWORD) {
            log.warn("No se creo la cuenta de administrador: SMARTBODY_ADMIN_PASSWORD debe tener al menos {} caracteres.",
                    LONGITUD_MINIMA_PASSWORD);
            return;
        }

        Usuario administrador = new Usuario();
        administrador.setNombre(nombre == null || nombre.isBlank() ? "Administrador" : nombre.trim());
        administrador.setEmail(correo);
        administrador.setPasswordHash(passwordEncoder.encode(password));
        administrador.agregarRol(rol(Rol.USUARIO));
        administrador.agregarRol(rolAdministrador);
        usuarioRepository.save(administrador);
        log.info("Cuenta de administrador creada: {}", correo);
    }

    private Rol rol(String nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> rolRepository.save(new Rol(nombre)));
    }
}
