package com.raizes.nordeste.pedidos.infrastructure.persistence;

import com.raizes.nordeste.pedidos.domain.CanalPedido;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CanalPedidoConverter implements AttributeConverter<CanalPedido, String> {

    @Override
    public String convertToDatabaseColumn(CanalPedido canal) {
        if (canal == null) {
            return null;
        }
        // Converte para minúsculas para satisfazer a restrição (check constraint) do PostgreSQL
        return canal.name().toLowerCase();
    }

    @Override
    public CanalPedido convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Converte de volta para maiúsculas para o Enum do Java
        return CanalPedido.valueOf(dbData.toUpperCase());
    }
}