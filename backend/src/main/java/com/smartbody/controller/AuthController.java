package com.smartbody.controller;

import com.smartbody.dto.AuthResponse;
import com.smartbody.dto.LoginRequest;
import com.smartbody.dto.MensajeResponse;
import com.smartbody.dto.RegistroRequest;
import com.smartbody.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** RF01 - registro de usuarios. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(peticion));
    }

    /** RF02 - inicio de sesion. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest peticion) {
        return ResponseEntity.ok(authService.login(peticion));
    }

    /**
     * RF12 - cierre de sesion. La autenticacion es stateless: el servidor confirma la
     * operacion y la aplicacion elimina el token almacenado localmente.
     */
    @PostMapping("/logout")
    public ResponseEntity<MensajeResponse> logout() {
        return ResponseEntity.ok(new MensajeResponse("Sesión cerrada correctamente."));
    }
}
