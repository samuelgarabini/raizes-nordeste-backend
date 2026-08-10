package com.raizes.nordeste.pedidos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raizes.nordeste.pedidos.infrastructure.security.AesEncryptorConverter;
import com.raizes.nordeste.pedidos.infrastructure.security.ClienteSecurityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "clientes")
@EntityListeners(ClienteSecurityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    private UUID id;

    @Column(
        nullable = false,
        length = 150
    )
    private String nome;

    @Convert(converter = AesEncryptorConverter.class)
    @Column(
        nullable = false,
        length = 1024
    )
    private String cpf;

    @Convert(converter = AesEncryptorConverter.class)
    @Column(
        nullable = false,
        length = 1024
    )
    private String email;

    @JsonIgnore
    @Column(
        name = "cpf_fingerprint",
        length = 64,
        unique = true
    )
    private String cpfFingerprint;
}