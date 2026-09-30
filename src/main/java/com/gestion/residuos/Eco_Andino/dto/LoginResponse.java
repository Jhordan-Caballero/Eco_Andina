package com.gestion.residuos.Eco_Andino.dto;

// expiresIn en segundos
public record LoginResponse(String accessToken, String tokenType, long expiresIn, UsuarioResponse user) {
}
