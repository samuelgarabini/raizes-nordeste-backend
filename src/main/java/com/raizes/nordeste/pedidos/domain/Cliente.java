package com.raizes.nordeste.pedidos.domain;

import com.raizes.nordeste.pedidos.infrastructure.security.AesEncryptorConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    // AQUI: Você coloca o @Convert logo acima do campo sensível
    @Convert(converter = AesEncryptorConverter.class)
    @Column(nullable = false, unique = true)
    private String cpf; 

    @Convert(converter = AesEncryptorConverter.class)
    @Column(nullable = false)
    private String email;
}