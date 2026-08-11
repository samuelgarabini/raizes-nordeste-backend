package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.infrastructure.exception.ForbiddenOperationException;
import com.raizes.nordeste.pedidos.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AutorizacaoClienteService {

    private final UsuarioRepository usuarioRepository;

    public AutorizacaoClienteService(
        UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public void validarAcessoAoCliente(
        UUID clienteId
    ) {
        Authentication authentication =
            SecurityContextHolder
                .getContext()
                .getAuthentication();

        Usuario usuario = usuarioRepository
            .findByUsernameIgnoreCase(
                authentication.getName()
            )
            .orElseThrow(this::acessoNegado);

        if (
            usuario.getPerfil() == Perfil.CLIENTE
                && !clienteId.equals(
                    usuario.getClienteId()
                )
        ) {
            throw acessoNegado();
        }
    }

    private ForbiddenOperationException acessoNegado() {
        return new ForbiddenOperationException(
            "ACESSO_CLIENTE_NEGADO",
            "O usuário autenticado não pode acessar "
                + "os dados deste cliente"
        );
    }
}
