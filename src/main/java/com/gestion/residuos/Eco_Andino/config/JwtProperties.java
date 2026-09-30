package com.gestion.residuos.Eco_Andino.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes, String issuer) {

    public Duration expiration() {
        return Duration.ofMinutes(expirationMinutes);
    }
}
