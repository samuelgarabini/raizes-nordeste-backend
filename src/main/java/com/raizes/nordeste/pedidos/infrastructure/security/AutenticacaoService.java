package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.infrastructure.exception.CredenciaisInvalidasException;
import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginRequestDTO;
import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginResponseDTO;
import com.raizes.nordeste.pedidos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService {

    /*
     * Hash utilizado apenas para manter o custo do BCrypt
     * quando o usuário informado não existe.
     */
    private static final String DUMMY_PASSWORD_HASH =
        "$2b$12$bQvttdBYw2KsXCaqWEA10uUjll6TmI."
            + "lhldAVb33Vvp2VWnsLu8I6";

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;

    public AutenticacaoService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO autenticar(
        LoginRequestDTO request
    ) {
        String username = request.username().trim();

        Usuario usuario = usuarioRepository
            .findByUsernameIgnoreCase(username)
            .orElse(null);

        String hashParaComparacao =
            usuario == null
                ? DUMMY_PASSWORD_HASH
                : usuario.getSenhaHash();

        boolean senhaValida = passwordEncoder.matches(
            request.password(),
            hashParaComparacao
        );

        if (
            usuario == null
                || !usuario.isAtivo()
                || !senhaValida
        ) {
            throw new CredenciaisInvalidasException();
        }

        String token = jwtTokenService.gerarToken(
            usuario.getUsername(),
            usuario.getPerfil()
        );

        return new LoginResponseDTO(
            token,
            jwtTokenService.getExpirationMs()
        );
    }
}