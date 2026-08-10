package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginRequestDTO;
import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        // Mock de autenticação de usuários para ambiente de desenvolvimento/teste
        Perfil perfil = Perfil.CLIENTE;
        if ("admin".equalsIgnoreCase(loginRequest.username())) {
            perfil = Perfil.ADMIN;
        } else if ("gerente".equalsIgnoreCase(loginRequest.username())) {
            perfil = Perfil.GERENTE;
        } else if ("atendente".equalsIgnoreCase(loginRequest.username())) {
            perfil = Perfil.ATENDENTE;
        }

        String token = jwtTokenService.gerarToken(loginRequest.username(), perfil);
        return ResponseEntity.ok(new LoginResponseDTO(token, jwtTokenService.getExpirationMs()));
    }
}