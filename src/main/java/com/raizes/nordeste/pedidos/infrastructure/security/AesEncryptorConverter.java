package com.raizes.nordeste.pedidos.infrastructure.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter
public class AesEncryptorConverter
    implements AttributeConverter<String, String> {

    private final AesGcmEncryptionService
        encryptionService;

    public AesEncryptorConverter(
        AesGcmEncryptionService encryptionService
    ) {
        this.encryptionService = encryptionService;
    }

    @Override
    public String convertToDatabaseColumn(
        String attribute
    ) {
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(
        String databaseValue
    ) {
        if (databaseValue == null) {
            return null;
        }

        /*
         * Compatibilidade temporária com registros
         * criados pelas migrations antigas em texto.
         */
        if (
            !encryptionService.isEncrypted(
                databaseValue
            )
        ) {
            return databaseValue;
        }

        return encryptionService.decrypt(
            databaseValue
        );
    }
}