package com.smartbody.admin.service;

import com.smartbody.admin.cliente.ClientesInternos;
import com.smartbody.admin.cliente.ClientesInternos.Evaluacion;
import com.smartbody.admin.cliente.ClientesInternos.Usuario;
import com.smartbody.admin.cliente.ClientesInternos.UsuarioObjetivo;
import com.smartbody.admin.dto.AdminDtos;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reporte de efectividad: que tanto mejoran los usuarios segun su objetivo. La evaluacion de cada
 * usuario (primera vs. ultima medida) la hace el servicio progreso; aqui se agrega por objetivo.
 */
@Service
public class ReporteService {

    private final ClientesInternos clientes;

    public ReporteService(ClientesInternos clientes) {
        this.clientes = clientes;
    }

    public AdminDtos.ReporteEfectividad efectividad() {
        List<Usuario> usuarios = clientes.usuarios().stream().filter(u -> !u.esAdministrador()).toList();
        Map<Long, Usuario> porId = usuarios.stream().collect(Collectors.toMap(Usuario::id, Function.identity(), (a, b) -> a));
        List<UsuarioObjetivo> conObjetivo = usuarios.stream()
                .filter(u -> u.perfil() != null && u.perfil().objetivo() != null)
                .map(u -> new UsuarioObjetivo(u.id(), u.perfil().objetivo().codigo()))
                .toList();

        List<AdminDtos.ProgresoUsuario> progresos = new ArrayList<>();
        for (Evaluacion e : clientes.evaluar(conObjetivo)) {
            Usuario u = porId.get(e.usuarioId());
            if (u == null) {
                continue;
            }
            progresos.add(new AdminDtos.ProgresoUsuario(u.id(), u.nombre(), u.perfil().objetivo().nombre(),
                    e.pesoInicial(), e.pesoActual(), e.cambioPesoKg(), e.imcInicial(), e.imcActual(),
                    e.mejoro(), e.diasSeguimiento()));
        }
        progresos.sort(Comparator.comparing(AdminDtos.ProgresoUsuario::mejoro).reversed()
                .thenComparing(p -> -Math.abs(p.cambioPesoKg())));

        List<AdminDtos.EfectividadObjetivo> porObjetivo = progresos.stream()
                .collect(Collectors.groupingBy(AdminDtos.ProgresoUsuario::objetivo, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    long mejoraron = e.getValue().stream().filter(AdminDtos.ProgresoUsuario::mejoro).count();
                    return new AdminDtos.EfectividadObjetivo(e.getKey(), e.getValue().size(), mejoraron,
                            porcentaje(mejoraron, e.getValue().size()),
                            redondear(e.getValue().stream().mapToDouble(AdminDtos.ProgresoUsuario::cambioPesoKg).average().orElse(0)));
                })
                .sorted(Comparator.comparing(AdminDtos.EfectividadObjetivo::usuarios).reversed())
                .toList();

        long mejoraron = progresos.stream().filter(AdminDtos.ProgresoUsuario::mejoro).count();
        long conPerfil = usuarios.stream().filter(u -> u.perfil() != null && u.perfil().completo()).count();
        return new AdminDtos.ReporteEfectividad(conPerfil, progresos.size(), mejoraron,
                porcentaje(mejoraron, progresos.size()),
                redondear(progresos.stream().mapToLong(AdminDtos.ProgresoUsuario::diasSeguimiento).average().orElse(0)),
                porObjetivo, progresos);
    }

    private static double porcentaje(long parte, long total) {
        return total == 0 ? 0 : Math.round(parte * 1000.0 / total) / 10.0;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
