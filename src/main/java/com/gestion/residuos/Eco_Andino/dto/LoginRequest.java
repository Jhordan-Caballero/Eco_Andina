package com.gestion.residuos.Eco_Andino.dto;

import jakarta.validation.constraints.NotBlank;

// username acepta el nombre de usuario o el correo
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contraseña es obligatoria") String password) {
}
