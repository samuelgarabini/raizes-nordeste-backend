package com.raizes.nordeste.pedidos.controller;

import com.raizes.nordeste.pedidos.repository.CampanhaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campanhas")
@Tag(name = "Campanhas Promocionais", description = "Endpoints de gerenciamento de campanhas")
public class CampanhaController {

    private final CampanhaRepository campanhaRepository;

    public CampanhaController(CampanhaRepository campanhaRepository) {
        this.campanhaRepository = campanhaRepository;
    }

    @GetMapping
    @Operation(summary = "Listar campanhas", description = "Retorna todas as campanhas cadastradas no banco")
    public ResponseEntity<?> listarCampanhas() {
        return ResponseEntity.ok(campanhaRepository.findAll());
    }
}