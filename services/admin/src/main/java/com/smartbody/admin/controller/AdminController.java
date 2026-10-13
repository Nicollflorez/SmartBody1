package com.smartbody.admin.controller;

import com.smartbody.admin.dto.AdminDtos;
import com.smartbody.admin.service.AdminService;
import com.smartbody.admin.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Panel de administracion. La libreria comun solo deja pasar a /api/admin/** a cuentas con
 * ROLE_ADMIN (claim del JWT); cualquier otro usuario autenticado recibe 403.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReporteService reporteService;

    public AdminController(AdminService adminService, ReporteService reporteService) {
        this.adminService = adminService;
        this.reporteService = reporteService;
    }

    /** Resumen de uso con estadisticas e historial de actividad reciente. */
    @GetMapping("/summary")
    public ResponseEntity<AdminDtos.Resumen> resumen() {
        return ResponseEntity.ok(adminService.resumen());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminDtos.UsuarioAdmin>> usuarios() {
        return ResponseEntity.ok(adminService.usuarios());
    }

    /** Perfil, progreso, rutina activa y plan nutricional de un usuario. */
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminDtos.DetalleUsuario> detalleUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.detalle(id));
    }

    /** Genera una rutina y un plan nutricional nuevos para el usuario. */
    @PostMapping("/users/{id}/plan")
    public ResponseEntity<AdminDtos.DetalleUsuario> generarPlan(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.generarPlan(id));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, String>> cambiarEstado(@PathVariable Long id,
                                                             @Valid @RequestBody AdminDtos.CambioEstado peticion) {
        adminService.cambiarEstado(id, peticion.activo());
        return ResponseEntity.ok(Map.of("mensaje", peticion.activo() ? "Cuenta activada." : "Cuenta desactivada."));
    }

    /** Reporte de efectividad: porcentaje de usuarios que mejoraron segun su objetivo. */
    @GetMapping("/reports/effectiveness")
    public ResponseEntity<AdminDtos.ReporteEfectividad> reporteEfectividad() {
        return ResponseEntity.ok(reporteService.efectividad());
    }
}
