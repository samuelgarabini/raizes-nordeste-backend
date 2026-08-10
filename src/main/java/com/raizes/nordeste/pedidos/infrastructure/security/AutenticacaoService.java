package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.infrastructure.audit.AuditoriaSegurancaService;
import com.raizes.nordeste.pedidos.infrastructure.audit.RegistrarAuditoriaCommand;
import com.raizes.nordeste.pedidos.infrastructure.audit.ResultadoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.audit.TipoEventoAuditoria;
import com.raizes.nordeste.pedidos.infrastructure.exception.CredenciaisInvalidasException;
import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginRequestDTO;
import com.raizes.nordeste.pedidos.infrastructure.security.dto.LoginResponseDTO;
import com.raizes.nordeste.pedidos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService {

    private static final String DUMMY_PASSWORD_HASH =
        "$2b$12$bQvttdBYw2KsXCaqWEA10uUjll6TmI."
            + "lhldAVb33Vvp2VWnsLu8I6";

    private static final String AUTH_RESOURCE =
        "AUTENTICACAO";

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;

    private final AuditoriaSegurancaService
        auditoriaService;

    public AutenticacaoService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        AuditoriaSegurancaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO autenticar(
        LoginRequestDTO request,
        String enderecoIp
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
            registrarFalhaDeLogin(
                username,
                enderecoIp
            );

            throw new CredenciaisInvalidasException();
        }

        String token = jwtTokenService.gerarToken(
            usuario.getUsername(),
            usuario.getPerfil()
        );

        registrarSucessoDeLogin(
            usuario,
            enderecoIp
        );

        return new LoginResponseDTO(
            token,
            jwtTokenService.getExpirationMs()
        );
    }

    private void registrarSucessoDeLogin(
        Usuario usuario,
        String enderecoIp
    ) {
        auditoriaService.registrar(
            new RegistrarAuditoriaCommand(
                TipoEventoAuditoria.LOGIN,
                ResultadoAuditoria.SUCESSO,
                usuario.getUsername(),
                usuario.getPerfil(),
                AUTH_RESOURCE,
                null,
                enderecoIp,
                null
            )
        );
    }

    private void registrarFalhaDeLogin(
        String username,
        String enderecoIp
    ) {
        auditoriaService.registrar(
            new RegistrarAuditoriaCommand(
                TipoEventoAuditoria.LOGIN,
                ResultadoAuditoria.FALHA,
                username,
                null,
                AUTH_RESOURCE,
                null,
                enderecoIp,
                "CREDENCIAIS_INVALIDAS"
            )
        );
    }
}