package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** DTOs de la gestión de usuarios, roles y permisos (CU02). */
public final class UsuarioDtos {
    private UsuarioDtos() {
    }

    public record RolDto(String codigo, String nombre, String descripcion, List<String> permisos, long usuarios) {
    }

    public record PermisoDto(String codigo, String descripcion, String casoUso) {
    }

    public record UsuarioDto(Integer idUsuario, String username, String nombres, String apellidos, String email,
                             String estado, List<String> roles, LocalDateTime ultimoAcceso, String nombreCompleto,
                             List<RolDto> rolesDetalle) {
    }

    /** Si no se envía contraseña al crear, se usa la contraseña inicial configurada (app.seed.default-password). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsuarioRequest(
            @NotBlank(message = "El usuario es obligatorio") @Size(max = 50) String username,
            @NotBlank(message = "Los nombres son obligatorios") @Size(max = 100) String nombres,
            @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 100) String apellidos,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 120) String email,
            @NotEmpty(message = "Asigna al menos un rol") List<String> roles,
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password) {
    }
}
