package com.smartbody.auth.service;

import com.smartbody.auth.dto.EstadisticasUsuariosResponse;
import com.smartbody.auth.dto.UsuarioInternoResponse;
import com.smartbody.auth.entity.Usuario;
import com.smartbody.auth.repository.UsuarioRepository;
import com.smartbody.comun.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Operaciones sobre usuarios que solo usa el servicio admin (nunca expuestas por el gateway). */
@Service
public class UsuariosInternosService {

    private final UsuarioRepository usuarioRepository;

    public UsuariosInternosService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioInternoResponse> listar() {
        return usuarioRepository.findAllParaAdministracion().stream().map(UsuarioInternoResponse::desde).toList();
    }

    /** Ultimos registros, para el historial de actividad del panel. */
    @Transactional(readOnly = true)
    public List<UsuarioInternoResponse> recientes() {
        return usuarioRepository.findTop20ByOrderByCreadoEnDesc().stream().map(UsuarioInternoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioInternoResponse detalle(Long id) {
        return UsuarioInternoResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioInternoResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = buscar(id);
        if (usuario.esAdministrador() && !activo) {
            throw ApiException.solicitudInvalida("No es posible desactivar una cuenta de administrador.");
        }
        usuario.setActivo(activo);
        return UsuarioInternoResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public EstadisticasUsuariosResponse estadisticas() {
        return new EstadisticasUsuariosResponse(
                usuarioRepository.count(),
                usuarioRepository.countByActivoTrue(),
                usuarioRepository.countByCreadoEnAfter(LocalDateTime.now().minusDays(30)),
                usuarioRepository.contarPerfilesCompletos(),
                aMapa(usuarioRepository.contarPorObjetivo()),
                aMapa(usuarioRepository.contarPorRestriccion()));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la cuenta del usuario."));
    }

    private static Map<String, Long> aMapa(List<Object[]> filas) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            mapa.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
        }
        return mapa;
    }
}
