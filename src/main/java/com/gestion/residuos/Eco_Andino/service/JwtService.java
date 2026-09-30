package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.config.JwtProperties;
import com.gestion.residuos.Eco_Andino.dto.RolResponse;
import com.gestion.residuos.Eco_Andino.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public Jwt generarToken(UsuarioResponse usuario) {
        // El JWT solo guarda segundos: se trunca para que iat/exp coincidan con lo que lleva el token
        Instant ahora = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(usuario.username())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(jwtProperties.expiration()))
                .claim("uid", usuario.id())
                .claim("roles", usuario.roles().stream().map(RolResponse::codigo).toList())
                .claim("permisos", usuario.permisos())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims));
    }
}
