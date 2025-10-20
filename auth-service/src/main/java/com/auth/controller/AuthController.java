package com.auth.controller;


import com.auth.dto.LoginRequest;
import com.auth.dto.LoginResponse;
import com.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authManager;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        var authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        var roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toList());

        var body = new LoginResponse(token, user.getUsername(), roles);
        return ResponseEntity.ok(body);
    }
}
