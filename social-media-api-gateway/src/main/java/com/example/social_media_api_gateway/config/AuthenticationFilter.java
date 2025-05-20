package com.example.social_media_api_gateway.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        chain: Là GatewayFilterChain – đại diện cho chuỗi các filter được áp dụng cho request đó.
//
//        exchange: Là ServerWebExchange – đại diện cho toàn bộ thông tin request và response hiện tại.
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        if (path.contains("ws")||path.contains("signup") || path.contains("/login") || path.contains("check-username")) {
            return chain.filter(exchange); // chuyển request đi qua các filter tiếp theo
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer"))
            return unauthorized(exchange);

        String token = authHeader.replace("Bearer ", "");

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", decodedToken.getUid())
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (FirebaseAuthException e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    @Override
    public int getOrder() {
        return -1; // ưu tiên cao
    }
}
