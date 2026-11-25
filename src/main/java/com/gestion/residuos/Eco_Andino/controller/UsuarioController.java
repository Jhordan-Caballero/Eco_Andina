package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.UsuarioDtos.*;
import com.gestion.residuos.Eco_Andino.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Gestión de usuarios, roles y permisos (CU02). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public List<UsuarioDto> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest datos) {
        return usuarioService.guardar(null, datos);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioDto actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRequest datos) {
        return usuarioService.guardar(id, datos);
    }

    @PatchMapping("/usuarios/{id}/estado")
    public UsuarioDto cambiarEstado(@PathVariable Integer id) {
        return usuarioService.cambiarEstado(id);
    }

    @GetMapping("/roles")
    public List<RolDto> listarRoles() {
        return usuarioService.listarRoles();
    }

    @PutMapping("/roles/permisos")
    public List<RolDto> guardarPermisos(@RequestBody Map<String, List<String>> cambios) {
        return usuarioService.guardarPermisosRoles(cambios);
    }

    @GetMapping("/permisos")
    public List<PermisoDto> listarPermisos() {
        return usuarioService.listarPermisos();
    }
}
