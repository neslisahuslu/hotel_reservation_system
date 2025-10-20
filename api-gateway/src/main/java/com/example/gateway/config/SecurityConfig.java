package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String SECRET = "your256bitsecret_your256bitsecret_your256bitsecret";

    @Bean
    SecurityWebFilterChain chain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(reg -> reg
                        .pathMatchers("/api/auth/**", "/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST,   "/api/hotels/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/hotels/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/hotels/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET,    "/api/hotels/**").hasAnyRole("ADMIN","USER")
                        .pathMatchers(HttpMethod.POST,   "/api/rooms/**").hasAnyRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE,   "/api/rooms/**").hasAnyRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT,   "/api/rooms/**").hasAnyRole("ADMIN")
                        .pathMatchers(HttpMethod.GET,    "/api/rooms/**").hasAnyRole("ADMIN","USER")
                        .pathMatchers(HttpMethod.GET,    "/api/reservations/**").hasAnyRole("ADMIN","USER")
                        .pathMatchers(HttpMethod.POST,    "/api/reservations/**").hasAnyRole("ADMIN","USER")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthConverter())))
                .build();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        var key = new javax.crypto.spec.SecretKeySpec(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        return org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                .build();
    }

    private org.springframework.core.convert.converter.Converter<
            org.springframework.security.oauth2.jwt.Jwt,
            reactor.core.publisher.Mono<? extends org.springframework.security.authentication.AbstractAuthenticationToken>
            > jwtAuthConverter() {
        return jwt -> {
            var roles = jwt.getClaimAsStringList("roles");
            var authorities = roles == null ? java.util.List.<org.springframework.security.core.GrantedAuthority>of()
                    : roles.stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .collect(java.util.stream.Collectors.toList());
            return reactor.core.publisher.Mono.just(
                    new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt, authorities)
            );
        };
    }
}
