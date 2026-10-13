package com.smartbody.controller;

import com.smartbody.dto.MensajeResponse;
import com.smartbody.dto.PerfilRequest;
import com.smartbody.dto.PerfilResponse;
import com.smartbody.dto.ResumenResponse;
import com.smartbody.dto.UsuarioResponse;
import com.smartbody.entity.FotoPerfil;
import com.smartbody.service.FotoPerfilService;
import com.smartbody.service.PerfilService;
import com.smartbody.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final FotoPerfilService fotoPerfilService;

    public UsuarioController(UsuarioService usuarioService, PerfilService perfilService,
                             FotoPerfilService fotoPerfilService) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
        this.fotoPerfilService = fotoPerfilService;
    }

    /** Datos de la cuenta autenticada. Requiere JWT valido. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> yo(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.ok(usuarioService.datosBasicos(autenticado.getUsername()));
    }

    /** Resumen para la pantalla de inicio (incluye el aviso de renovacion de rutina, RF11). */
    @GetMapping("/summary")
    public ResponseEntity<ResumenResponse> resumen(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.ok(perfilService.resumen(autenticado.getUsername()));
    }

    /** RF03, RF04, RF05 - consulta del perfil. */
    @GetMapping("/profile")
    public ResponseEntity<PerfilResponse> perfil(@AuthenticationPrincipal UserDetails autenticado) {
        return ResponseEntity.ok(perfilService.consultar(autenticado.getUsername()));
    }

    /** Sube o reemplaza la foto de perfil (campo multipart "foto"). */
    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensajeResponse> subirFoto(@AuthenticationPrincipal UserDetails autenticado,
                                                     @RequestParam("foto") MultipartFile foto) {
        fotoPerfilService.guardar(autenticado.getUsername(), foto);
        return ResponseEntity.ok(new MensajeResponse("Foto de perfil actualizada."));
    }

    /** Devuelve la imagen de la foto de perfil del usuario autenticado (404 si no tiene). */
    @GetMapping("/photo")
    public ResponseEntity<byte[]> foto(@AuthenticationPrincipal UserDetails autenticado) {
        FotoPerfil foto = fotoPerfilService.obtener(autenticado.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(foto.getContenido());
    }

    @DeleteMapping("/photo")
    public ResponseEntity<MensajeResponse> eliminarFoto(@AuthenticationPrincipal UserDetails autenticado) {
        fotoPerfilService.eliminar(autenticado.getUsername());
        return ResponseEntity.ok(new MensajeResponse("Foto de perfil eliminada."));
    }

    /** RF03, RF04, RF05 - registro o actualizacion del perfil. */
    @PutMapping("/profile")
    public ResponseEntity<PerfilResponse> actualizarPerfil(@AuthenticationPrincipal UserDetails autenticado,
                                                           @Valid @RequestBody PerfilRequest peticion) {
        return ResponseEntity.ok(perfilService.actualizar(autenticado.getUsername(), peticion));
    }
}
