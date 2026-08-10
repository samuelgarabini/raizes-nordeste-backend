package com.raizes.nordeste.pedidos.infrastructure.security;

import com.raizes.nordeste.pedidos.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
    extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
        JwtTokenService jwtTokenService,
        UsuarioRepository usuarioRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = recuperarToken(request);

        if (
            token != null
                && jwtTokenService.isTokenValido(token)
        ) {
            autenticarUsuario(token);
        }

        filterChain.doFilter(request, response);
    }

    private void autenticarUsuario(String token) {
        String username =
            jwtTokenService.getUsernameDoToken(token);

        usuarioRepository
            .findByUsernameIgnoreCase(username)
            .filter(Usuario::isAtivo)
            .ifPresent(usuario -> {
                SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                        "ROLE_"
                            + usuario.getPerfil().name()
                    );

                UsernamePasswordAuthenticationToken
                    authentication =
                        new UsernamePasswordAuthenticationToken(
                            usuario.getUsername(),
                            null,
                            List.of(authority)
                        );

                SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            });
    }

    private String recuperarToken(
        HttpServletRequest request
    ) {
        String authorization =
            request.getHeader("Authorization");

        if (
            authorization == null
                || !authorization.startsWith("Bearer ")
        ) {
            return null;
        }

        String token = authorization.substring(7).trim();

        return token.isBlank() ? null : token;
    }
}