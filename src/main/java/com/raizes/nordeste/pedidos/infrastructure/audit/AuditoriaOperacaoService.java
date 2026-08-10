package com.raizes.nordeste.pedidos.infrastructure.audit;

import com.raizes.nordeste.pedidos.infrastructure.exception.ApiException;
import com.raizes.nordeste.pedidos.infrastructure.security.Perfil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
public class AuditoriaOperacaoService {

    private final AuditoriaSegurancaService
        auditoriaService;

    public AuditoriaOperacaoService(
        AuditoriaSegurancaService auditoriaService
    ) {
        this.auditoriaService = auditoriaService;
    }

    public void registrarSucesso(
        TipoEventoAuditoria evento,
        String recurso,
        UUID recursoId
    ) {
        ContextoAuditoria contexto =
            obterContexto();

        auditoriaService.registrar(
            new RegistrarAuditoriaCommand(
                evento,
                ResultadoAuditoria.SUCESSO,
                contexto.ator(),
                contexto.perfil(),
                recurso,
                converterId(recursoId),
                contexto.enderecoIp(),
                null
            )
        );
    }

    public void registrarFalha(
        TipoEventoAuditoria evento,
        String recurso,
        UUID recursoId,
        RuntimeException exception
    ) {
        ContextoAuditoria contexto =
            obterContexto();

        auditoriaService.registrar(
            new RegistrarAuditoriaCommand(
                evento,
                ResultadoAuditoria.FALHA,
                contexto.ator(),
                contexto.perfil(),
                recurso,
                converterId(recursoId),
                contexto.enderecoIp(),
                obterCodigoErro(exception)
            )
        );
    }

    private ContextoAuditoria obterContexto() {
        Authentication authentication =
            SecurityContextHolder
                .getContext()
                .getAuthentication();

        String ator = null;
        Perfil perfil = null;

        if (
            authentication != null
                && authentication.isAuthenticated()
                && !(authentication
                    instanceof
                    AnonymousAuthenticationToken)
        ) {
            ator = authentication.getName();
            perfil = obterPerfil(authentication);
        }

        return new ContextoAuditoria(
            ator,
            perfil,
            obterEnderecoIp()
        );
    }

    private Perfil obterPerfil(
        Authentication authentication
    ) {
        return authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority ->
                authority.startsWith("ROLE_")
            )
            .map(authority ->
                authority.substring("ROLE_".length())
            )
            .map(this::converterPerfil)
            .filter(profile -> profile != null)
            .findFirst()
            .orElse(null);
    }

    private Perfil converterPerfil(
        String profileName
    ) {
        try {
            return Perfil.valueOf(profileName);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String obterEnderecoIp() {
        if (
            RequestContextHolder
                .getRequestAttributes()
                instanceof ServletRequestAttributes
                    attributes
        ) {
            HttpServletRequest request =
                attributes.getRequest();

            return request.getRemoteAddr();
        }

        return null;
    }

    private String obterCodigoErro(
        RuntimeException exception
    ) {
        if (exception instanceof ApiException apiException) {
            return apiException.getErrorCode();
        }

        return "ERRO_INTERNO";
    }

    private String converterId(UUID recursoId) {
        return recursoId == null
            ? null
            : recursoId.toString();
    }

    private record ContextoAuditoria(
        String ator,
        Perfil perfil,
        String enderecoIp
    ) {
    }
}