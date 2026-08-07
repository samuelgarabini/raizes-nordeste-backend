package com.raizes.nordeste.pedidos.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "tb_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean processed;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        this.processed = false;
    }
}