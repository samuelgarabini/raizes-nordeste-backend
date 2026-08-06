package com.raizes.nordeste.pedidos.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SystemMockController {

    // 1. Autenticação: POST /api/v1/auth/login
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        return ResponseEntity.ok(Map.of(
            "token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mocked_jwt_token_uninter_raizes_nordeste",
            "tipo", "Bearer",
            "roles", List.of("ROLE_OPERADOR", "ROLE_ADMIN"),
            "mensagem", "Autenticação realizada com sucesso (Mock acadêmico)"
        ));
    }

    // 2. Cardápio: GET /api/v1/unidades/{id}/cardapio
    @GetMapping("/unidades/{id}/cardapio")
    public ResponseEntity<Map<String, Object>> getCardapio(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
            "unidadeId", id,
            "nomeUnidade", "Raízes do Nordeste - Matriz",
            "itens", List.of(
                Map.of("id", 1, "nome", "Carne de Sol com Mandioca", "preco", 49.90, "categoria", "Pratos Principais"),
                Map.of("id", 2, "nome", "Baião de Dois Completo", "preco", 39.90, "categoria", "Pratos Principais"),
                Map.of("id", 3, "nome", "Cartola Pernambucana", "preco", 18.00, "categoria", "Sobremesas"),
                Map.of("id", 4, "nome", "Cerveja Artesanal de Rapadura", "preco", 14.90, "categoria", "Bebidas")
            )
        ));
    }

    // 3. Pagamentos Mock: POST /api/v1/pagamentos/mock
    @PostMapping("/pagamentos/mock")
    public ResponseEntity<Map<String, Object>> processarPagamentoMock(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
            "transacaoId", UUID.randomUUID().toString(),
            "pedidoId", request.getOrDefault("pedidoId", 101),
            "status", "APROVADO",
            "gateway", "MockGatewayPayment",
            "mensagem", "Pagamento aprovado com sucesso."
        ));
    }

    // 4. LGPD: DELETE /api/v1/lgpd/anonimizar
    @DeleteMapping("/lgpd/anonimizar")
    public ResponseEntity<Map<String, String>> anonimizarDados(@RequestParam(required = false) String clienteId) {
        String idAlvo = clienteId != null ? clienteId : "desconhecido";
        return ResponseEntity.ok(Map.of(
            "status", "SUCESSO",
            "mensagem", "Os dados sensíveis (PII) do cliente " + idAlvo + " foram anonimizados com sucesso em conformidade com a LGPD."
        ));
    }
}