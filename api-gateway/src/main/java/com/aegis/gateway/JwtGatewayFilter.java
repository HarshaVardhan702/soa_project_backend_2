package com.aegis.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private final SecretKey key;
    public JwtGatewayFilter(@Value("${security.jwt.secret}") String secret){
        key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path=exchange.getRequest().getURI().getPath();
        if(path.startsWith("/auth/") || path.equals("/actuator/health")) return chain.filter(exchange);
        String header=exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(header==null || !header.startsWith("Bearer ")) return unauthorized(exchange);
        try {
            Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            String role=String.valueOf(claims.get("role"));
            if(path.startsWith("/approvals/") && !role.equals("ADJUSTER")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            ServerHttpRequest request=exchange.getRequest().mutate()
                    .header("X-Authenticated-User",claims.getSubject())
                    .header("X-Authenticated-Role",String.valueOf(claims.get("role"))).build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch(Exception ex) {
            return unauthorized(exchange);
        }
    }
    private Mono<Void> unauthorized(ServerWebExchange exchange){
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    @Override public int getOrder(){return -100;}
}
