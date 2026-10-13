package com.smartbody.service;

import com.smartbody.dto.AdminDtos;
import com.smartbody.entity.Medida;
import com.smartbody.entity.Perfil;
import com.smartbody.entity.Usuario;
import com.smartbody.repository.MedidaRepository;
import com.smartbody.repository.UsuarioRepository;
import com.smartbody.service.reglas.EvaluadorProgreso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Reportes del sistema para el administrador: efectividad de los planes segun el objetivo de cada usuario. */
@Service
public class ReporteService {

    private final UsuarioRepository usuarioRepository;
    private final MedidaRepository medidaRepository;

    public ReporteService(UsuarioRepository usuarioRepository, MedidaRepository medidaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.medidaRepository = medidaRepository;
    }

    /**
     * Compara la primera y la ultima medida de cada usuario con al menos dos registros.
     * Un usuario "mejoro" si su evolucion va en la direccion de su objetivo (ver {@link EvaluadorProgreso}).
     */
    @Transactional(readOnly = true)
    public AdminDtos.ReporteEfectividad efectividad() {
        List<Usuario> usuarios = usuarioRepository.findAllParaAdministracion().stream()
                .filter(u -> !u.esAdministrador())
                .toList();
        Map<Long, List<Medida>> medidasPorUsuario = medidaRepository.findAllByOrderByRegistradoEnAsc().stream()
                .collect(Collectors.groupingBy(m -> m.getUsuario().getId(), LinkedHashMap::new, Collectors.toList()));

        List<AdminDtos.ProgresoUsuario> progresos = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            List<Medida> medidas = medidasPorUsuario.getOrDefault(usuario.getId(), List.of());
            Perfil perfil = usuario.getPerfil();
            if (medidas.size() < 2 || perfil == null || perfil.getObjetivo() == null) {
                continue;
            }
            Medida primera = medidas.get(0);
            Medida ultima = medidas.get(medidas.size() - 1);
            String codigo = perfil.getObjetivo().getCodigo();
            progresos.add(new AdminDtos.ProgresoUsuario(
                    usuario.getId(),
                    usuario.getNombre(),
                    perfil.getObjetivo().getNombre(),
                    primera.getPesoKg(),
                    ultima.getPesoKg(),
                    redondear(ultima.getPesoKg() - primera.getPesoKg()),
                    primera.getImc(),
                    ultima.getImc(),
                    EvaluadorProgreso.mejoro(codigo, primera.getPesoKg(), ultima.getPesoKg(), primera.getImc(), ultima.getImc()),
                    Duration.between(primera.getRegistradoEn(), ultima.getRegistradoEn()).toDays()));
        }

        progresos.sort(Comparator.comparing(AdminDtos.ProgresoUsuario::mejoro).reversed()
                .thenComparing(p -> -Math.abs(p.cambioPesoKg())));

        List<AdminDtos.EfectividadObjetivo> porObjetivo = progresos.stream()
                .collect(Collectors.groupingBy(AdminDtos.ProgresoUsuario::objetivo, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    long mejoraron = e.getValue().stream().filter(AdminDtos.ProgresoUsuario::mejoro).count();
                    return new AdminDtos.EfectividadObjetivo(
                            e.getKey(),
                            e.getValue().size(),
                            mejoraron,
                            porcentaje(mejoraron, e.getValue().size()),
                            redondear(e.getValue().stream().mapToDouble(AdminDtos.ProgresoUsuario::cambioPesoKg).average().orElse(0)));
                })
                .sorted(Comparator.comparing(AdminDtos.EfectividadObjetivo::usuarios).reversed())
                .toList();

        long mejoraron = progresos.stream().filter(AdminDtos.ProgresoUsuario::mejoro).count();
        long conPerfil = usuarios.stream().filter(u -> u.getPerfil() != null && u.getPerfil().estaCompleto()).count();
        return new AdminDtos.ReporteEfectividad(
                conPerfil,
                progresos.size(),
                mejoraron,
                porcentaje(mejoraron, progresos.size()),
                redondear(progresos.stream().mapToLong(AdminDtos.ProgresoUsuario::diasSeguimiento).average().orElse(0)),
                porObjetivo,
                progresos);
    }

    private static double porcentaje(long parte, long total) {
        return total == 0 ? 0 : Math.round(parte * 1000.0 / total) / 10.0;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
