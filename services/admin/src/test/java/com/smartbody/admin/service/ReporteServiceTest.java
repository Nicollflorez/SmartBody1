package com.smartbody.admin.service;

import com.smartbody.admin.cliente.ClientesInternos;
import com.smartbody.admin.cliente.ClientesInternos.Evaluacion;
import com.smartbody.admin.cliente.ClientesInternos.Objetivo;
import com.smartbody.admin.cliente.ClientesInternos.Perfil;
import com.smartbody.admin.cliente.ClientesInternos.Usuario;
import com.smartbody.admin.dto.AdminDtos;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** El reporte agrega las evaluaciones que calcula progreso por objetivo y deja fuera a los administradores. */
class ReporteServiceTest {

    @Test
    void agrupaPorObjetivoYCalculaPorcentajes() {
        ClientesInternos clientes = mock(ClientesInternos.class);
        Objetivo perder = new Objetivo(1L, "PERDER_PESO", "Perder peso");
        when(clientes.usuarios()).thenReturn(List.of(
                usuario(1L, "Laura", perder, false),
                usuario(2L, "Andrés", perder, false),
                usuario(3L, "Admin", perder, true)));
        when(clientes.evaluar(any())).thenReturn(List.of(
                new Evaluacion(1L, 80.0, 76.0, -4.0, 27.0, 25.6, true, 60),
                new Evaluacion(2L, 70.0, 71.0, 1.0, 24.0, 24.3, false, 30)));

        AdminDtos.ReporteEfectividad reporte = new ReporteService(clientes).efectividad();

        assertThat(reporte.usuariosConPerfil()).isEqualTo(2);
        assertThat(reporte.usuariosConSeguimiento()).isEqualTo(2);
        assertThat(reporte.usuariosQueMejoraron()).isEqualTo(1);
        assertThat(reporte.porcentajeMejora()).isEqualTo(50.0);
        assertThat(reporte.porObjetivo()).hasSize(1);
        assertThat(reporte.porObjetivo().get(0).objetivo()).isEqualTo("Perder peso");
        assertThat(reporte.usuarios().get(0).nombre()).isEqualTo("Laura");
    }

    private static Usuario usuario(Long id, String nombre, Objetivo objetivo, boolean admin) {
        return new Usuario(id, nombre, nombre.toLowerCase() + "@prueba.smartbody.com", true, admin,
                LocalDateTime.now().minusDays(90), new Perfil(objetivo, true));
    }
}
