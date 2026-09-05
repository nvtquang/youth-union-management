package com.hcmcyu.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GatewayJwtService {

    private final SecretKey secretKey;

    public GatewayJwtService(@Value("${auth.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public GatewayPrincipal parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new GatewayPrincipal(
                    claims.get("userId", String.class),
                    claims.get("memberId", String.class),
                    claims.get("username", String.class),
                    claims.get("email", String.class),
                    claims.get("role", String.class),
                    claims.get("organizationId", String.class),
                    claims.get("tdpId", String.class)
            );
        } catch (ExpiredJwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
        } catch (IllegalArgumentException | JwtException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
    }
}
