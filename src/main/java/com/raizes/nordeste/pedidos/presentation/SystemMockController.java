package com.raizes.nordeste.pedidos.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SystemMockController {

    /*
     * A autenticação é tratada pelo AuthController.
     * O cardápio é tratado pelo CardapioController.
     * O pagamento mockado agora faz parte do checkout.
     *
     * Este controller permanecerá temporariamente apenas
     * para o endpoint de demonstração da LGPD, que será
     * implementado de verdade em uma etapa posterior.
     */

    @DeleteMapping("/lgpd/anonimizar")
    public ResponseEntity<Map<String, String>>
        anonimizarDados(
            @RequestParam(required = false)
                String clienteId
        ) {

        String idAlvo =
            clienteId != null
                ? clienteId
                : "desconhecido";

        return ResponseEntity.ok(
            Map.of(
                "status",
                "SUCESSO",
                "mensagem",
                "Os dados sensíveis do cliente "
                    + idAlvo
                    + " foram anonimizados."
            )
        );
    }
}