package com.raizes.nordeste.pedidos.application.dto;

import com.raizes.nordeste.pedidos.domain.Cliente;
import com.raizes.nordeste.pedidos.util.MaskingUtil;
import java.util.UUID;

public record ClienteResponseDTO(
    UUID id,
    String nome,
    String cpfMascarado,
    String emailMascarado
) {
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        return new ClienteResponseDTO(
            cliente.getId(),
            cliente.getNome(),
            MaskingUtil.maskCpf(cliente.getCpf()),
            MaskingUtil.maskEmail(cliente.getEmail())
        );
    }
}