package com.raizes.nordeste.pedidos.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenService {

    private static final int MINIMUM_SECRET_BYTES = 32;

    private final SecretKey signingKey;

    private final long expirationMs;

    public JwtTokenService(
        @Value("${api.security.jwt.secret}")
            String secret,
        @Value("${api.security.jwt.expiration-ms}")
            long expirationMs
    ) {
        validarConfiguracao(secret, expirationMs);

        this.signingKey = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
        this.expirationMs = expirationMs;
    }

    public String gerarToken(
        String username,
        Perfil perfil
    ) {
        Date agora = new Date();

        Date expiracao = new Date(
            agora.getTime() + expirationMs
        );

        return Jwts.builder()
            .subject(username)
            .claim("role", perfil.name())
            .issuedAt(agora)
            .expiration(expiracao)
            .signWith(signingKey)
            .compact();
    }

    public String getUsernameDoToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleDoToken(String token) {
        return getClaims(token).get(
            "role",
            String.class
        );
    }

    public boolean isTokenValido(String token) {
        try {
            Claims claims = getClaims(token);

            return !claims
                .getExpiration()
                .before(new Date());
        } catch (Exception exception) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private void validarConfiguracao(
        String secret,
        long expirationMs
    ) {
        if (
            secret == null
                || secret.isBlank()
        ) {
            throw new IllegalStateException(
                "JWT_SECRET não foi configurado"
            );
        }

        int tamanhoEmBytes = secret
            .getBytes(StandardCharsets.UTF_8)
            .length;

        if (tamanhoEmBytes < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                "JWT_SECRET deve possuir pelo menos "
                    + MINIMUM_SECRET_BYTES
                    + " bytes"
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException(
                "JWT_EXPIRATION_MS deve ser maior que zero"
            );
        }
    }
}