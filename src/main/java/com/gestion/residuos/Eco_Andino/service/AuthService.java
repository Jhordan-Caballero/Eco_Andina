package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.LoginRequest;
import com.gestion.residuos.Eco_Andino.dto.LoginResponse;
import com.gestion.residuos.Eco_Andino.dto.UsuarioResponse;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.exception.DemasiadosIntentosException;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import com.gestion.residuos.Eco_Andino.security.LoginAttemptService;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final LoginAttemptService intentos;
    private final AuditoriaService auditoria;
    private final SesionService sesion;

    /**
     * Lanza BadCredentialsException (credenciales inválidas), DisabledException (usuario INACTIVO) o
     * DemasiadosIntentosException (bloqueo temporal tras varios intentos fallidos: frena ataques de fuerza bruta).
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String login = request.username().strip();
        if (intentos.estaBloqueado(login)) {
            log.warn("Inicio de sesión bloqueado temporalmente para {}", TextoUtil.paraLog(login));
            throw new DemasiadosIntentosException();
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(login, request.password()));
        } catch (BadCredentialsException e) {
            intentos.registrarFallo(login);
            log.warn("Intento de inicio de sesión fallido para {}", TextoUtil.paraLog(login));
            throw e;
        }
        intentos.registrarExito(login);

        Usuario entidad = cargarUsuario(authentication.getName());
        entidad.setUltimoAcceso(sesion.ahora());
        auditoria.registrarComo(entidad, "ACCESO", "Sesión", entidad.getUsername(), "Inició sesión en el sistema");

        UsuarioResponse usuario = UsuarioResponse.from(entidad);
        Jwt jwt = jwtService.generarToken(usuario);
        long expiresIn = Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).toSeconds();
        return new LoginResponse(jwt.getTokenValue(), "Bearer", expiresIn, usuario);
    }

    // El token sigue siendo válido aunque el usuario se desactive después, por eso se vuelve a leer de la BD
    @Transactional(readOnly = true)
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
