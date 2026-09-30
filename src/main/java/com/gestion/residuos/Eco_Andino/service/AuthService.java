package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.LoginRequest;
import com.gestion.residuos.Eco_Andino.dto.LoginResponse;
import com.gestion.residuos.Eco_Andino.dto.UsuarioResponse;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    // Lanza BadCredentialsException (credenciales inválidas) o DisabledException (usuario INACTIVO)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username().strip(), request.password()));

        UsuarioResponse usuario = UsuarioResponse.from(cargarUsuario(authentication.getName()));
        Jwt jwt = jwtService.generarToken(usuario);
        long expiresIn = Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).toSeconds();
        return new LoginResponse(jwt.getTokenValue(), "Bearer", expiresIn, usuario);
    }

    // El token sigue siendo válido aunque el usuario se desactive después, por eso se vuelve a leer de la BD
    public UsuarioResponse perfil(String username) {
        Usuario usuario = cargarUsuario(username);
        if (!usuario.isActivo()) {
            throw new DisabledException("Usuario desactivado");
        }
        return UsuarioResponse.from(usuario);
    }

    private Usuario cargarUsuario(String login) {
        return usuarioRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + login));
    }
}
