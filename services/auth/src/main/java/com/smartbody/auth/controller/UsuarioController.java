package com.smartbody.auth.controller;

import com.smartbody.auth.dto.MensajeResponse;
import com.smartbody.auth.dto.PerfilRequest;
import com.smartbody.auth.dto.PerfilResponse;
import com.smartbody.auth.dto.ResumenResponse;
import com.smartbody.auth.dto.UsuarioResponse;
import com.smartbody.auth.entity.FotoPerfil;
import com.smartbody.auth.service.FotoPerfilService;
import com.smartbody.auth.service.PerfilService;
import com.smartbody.auth.service.UsuarioService;
import com.smartbody.comun.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<UsuarioResponse> yo(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(usuarioService.datosBasicos(autenticado.email()));
    }

    /** Resumen para la pantalla de inicio (incluye el aviso de renovacion de rutina, RF11). */
    @GetMapping("/summary")
    public ResponseEntity<ResumenResponse> resumen(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(perfilService.resumen(autenticado.email()));
    }

    /** RF03, RF04, RF05 - consulta del perfil. */
    @GetMapping("/profile")
    public ResponseEntity<PerfilResponse> perfil(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(perfilService.consultar(autenticado.email()));
    }

    /** RF03, RF04, RF05 - registro o actualizacion del perfil. */
    @PutMapping("/profile")
    public ResponseEntity<PerfilResponse> actualizarPerfil(@AuthenticationPrincipal UsuarioAutenticado autenticado,
                                                           @Valid @RequestBody PerfilRequest peticion) {
        return ResponseEntity.ok(perfilService.actualizar(autenticado.email(), peticion));
    }

    /** Sube o reemplaza la foto de perfil (campo multipart "foto"). */
    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensajeResponse> subirFoto(@AuthenticationPrincipal UsuarioAutenticado autenticado,
                                                     @RequestParam("foto") MultipartFile foto) {
        fotoPerfilService.guardar(autenticado.email(), foto);
        return ResponseEntity.ok(new MensajeResponse("Foto de perfil actualizada."));
    }

    /** Devuelve la imagen de la foto de perfil del usuario autenticado (404 si no tiene). */
    @GetMapping("/photo")
    public ResponseEntity<byte[]> foto(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        FotoPerfil foto = fotoPerfilService.obtener(autenticado.email());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(foto.getContenido());
    }

    @DeleteMapping("/photo")
    public ResponseEntity<MensajeResponse> eliminarFoto(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        fotoPerfilService.eliminar(autenticado.email());
        return ResponseEntity.ok(new MensajeResponse("Foto de perfil eliminada."));
    }
}
