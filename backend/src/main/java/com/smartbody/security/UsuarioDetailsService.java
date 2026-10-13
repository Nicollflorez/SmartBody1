package com.smartbody.security;

import com.smartbody.entity.Rol;
import com.smartbody.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Carga el usuario autenticado a partir del correo contenido en el JWT. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .map(usuario -> {
                    List<SimpleGrantedAuthority> autoridades = usuario.getRoles().stream()
                            .map(Rol::getNombre)
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                    return User.withUsername(usuario.getEmail())
                            .password(usuario.getPasswordHash())
                            .authorities(autoridades)
                            .disabled(!usuario.isActivo())
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
