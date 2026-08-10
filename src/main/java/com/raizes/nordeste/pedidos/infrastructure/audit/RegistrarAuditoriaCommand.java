package com.raizes.nordeste.pedidos.infrastructure.audit;

import com.raizes.nordeste.pedidos.infrastructure.security.Perfil;

public record RegistrarAuditoriaCommand(
    TipoEventoAuditoria evento,
    ResultadoAuditoria resultado,
    String ator,
    Perfil perfil,
    String recurso,
    String recursoId,
    String enderecoIp,
    String codigoErro
) {
}