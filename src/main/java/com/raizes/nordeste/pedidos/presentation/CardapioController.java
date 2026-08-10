package com.raizes.nordeste.pedidos.presentation;

import com.raizes.nordeste.pedidos.application.CardapioService;
import com.raizes.nordeste.pedidos.presentation.dto.CardapioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/unidades")
@RequiredArgsConstructor
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping("/{unidadeId}/cardapio")
    public ResponseEntity<CardapioDTO> getCardapio(@PathVariable Long unidadeId) {
        return ResponseEntity.ok(cardapioService.buscarCardapioPorUnidade(unidadeId));
    }
}