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

    private final AutenticacaoService autenticacaoService;

    public AuthController(
        AutenticacaoService autenticacaoService
    ) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
        @RequestBody @Valid LoginRequestDTO request
    ) {
        LoginResponseDTO response =
            autenticacaoService.autenticar(request);

        return ResponseEntity.ok(response);
    }
}