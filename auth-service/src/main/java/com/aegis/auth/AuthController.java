package com.aegis.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SecretKey key;
    private final long expiration;

    public AuthController(@Value("${security.jwt.secret}") String secret,
                          @Value("${security.jwt.expiration-ms}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        // Demo credentials only. Replace with a user table + BCrypt in production.
        if (!("policyholder".equals(request.username()) && "Pass123!".equals(request.password()))
                && !("adjuster".equals(request.username()) && "Pass123!".equals(request.password()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        String role = request.username().equals("adjuster") ? "ADJUSTER" : "POLICYHOLDER";
        Date now = new Date();
        String token = Jwts.builder().subject(request.username()).claim("role", role)
                .issuedAt(now).expiration(new Date(now.getTime() + expiration)).signWith(key).compact();
        return Map.of("accessToken", token, "tokenType", "Bearer", "expiresInSeconds", expiration / 1000, "role", role);
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
}
