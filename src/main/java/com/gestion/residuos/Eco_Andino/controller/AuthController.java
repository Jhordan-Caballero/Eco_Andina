package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.LoginRequest;
import com.gestion.residuos.Eco_Andino.dto.LoginResponse;
import com.gestion.residuos.Eco_Andino.dto.UsuarioResponse;
import com.gestion.residuos.Eco_Andino.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.perfil(jwt.getSubject());
    }
}
