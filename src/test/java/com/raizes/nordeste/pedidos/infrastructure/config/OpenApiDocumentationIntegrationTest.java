package com.raizes.nordeste.pedidos.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveDocumentarMetadadosESegurancaJwt()
        throws Exception {

        JsonNode document = obterDocumento();

        assertThat(
            document.path("info")
                .path("title")
                .asText()
        ).isEqualTo(
            "Rede Raízes do Nordeste - API"
        );

        assertThat(
            document.path("info")
                .path("version")
                .asText()
        ).isEqualTo("1.0.0");

        JsonNode bearerAuth = document
            .path("components")
            .path("securitySchemes")
            .path("bearerAuth");

        assertThat(
            bearerAuth.path("type").asText()
        ).isEqualTo("http");

        assertThat(
            bearerAuth.path("scheme").asText()
        ).isEqualTo("bearer");

        assertThat(
            bearerAuth.path("bearerFormat").asText()
        ).isEqualTo("JWT");

        JsonNode globalSecurity =
            document.get("security");

        assertThat(
            globalSecurity == null
                || globalSecurity.isEmpty()
        ).isTrue();
    }

    @Test
    void deveManterEndpointsPublicosSemJwt()
        throws Exception {

        JsonNode document = obterDocumento();

        assertPublicOperation(
            operation(
                document,
                "/api/v1/auth/login",
                "post"
            )
        );

        assertPublicOperation(
            operation(
                document,
                "/api/v1/unidades/{unidadeId}/cardapio",
                "get"
            )
        );
    }

    @Test
    void deveProtegerPedidosCampanhasELgpd()
        throws Exception {

        JsonNode document = obterDocumento();

        assertBearerProtected(
            operation(
                document,
                "/api/v1/pedidos",
                "post"
            )
        );

        assertBearerProtected(
            operation(
                document,
                "/api/v1/pedidos/{id}/checkout",
                "post"
            )
        );

        assertBearerProtected(
            operation(
                document,
                "/api/v1/pedidos/{id}/status",
                "patch"
            )
        );

        assertBearerProtected(
            operation(
                document,
                "/api/v1/pedidos/{id}/cancelamento",
                "patch"
            )
        );

        assertBearerProtected(
            operation(
                document,
                "/api/campanhas",
                "get"
            )
        );

        assertBearerProtected(
            operation(
                document,
                "/api/v1/lgpd/anonimizar",
                "delete"
            )
        );
    }

    @Test
    void deveDocumentarRespostasDoFluxoPrincipal()
        throws Exception {

        JsonNode document = obterDocumento();

        assertResponses(
            operation(
                document,
                "/api/v1/auth/login",
                "post"
            ),
            "200",
            "400",
            "401"
        );

        assertResponses(
            operation(
                document,
                "/api/v1/pedidos",
                "post"
            ),
            "201",
            "400",
            "401",
            "403",
            "404",
            "409"
        );

        assertResponses(
            operation(
                document,
                "/api/v1/pedidos/{id}/checkout",
                "post"
            ),
            "200",
            "400",
            "401",
            "403",
            "404",
            "409"
        );

        assertResponses(
            operation(
                document,
                "/api/v1/pedidos/{id}/status",
                "patch"
            ),
            "200",
            "400",
            "401",
            "403",
            "404",
            "409"
        );

        assertResponses(
            operation(
                document,
                "/api/v1/pedidos/{id}/cancelamento",
                "patch"
            ),
            "200",
            "400",
            "401",
            "403",
            "404",
            "409"
        );

        assertResponses(
            operation(
                document,
                "/api/v1/pedidos",
                "get"
            ),
            "200",
            "400",
            "401",
            "403"
        );

        assertResponses(
            operation(
                document,
                "/api/v1/pedidos/{id}",
                "get"
            ),
            "200",
            "400",
            "401",
            "403",
            "404"
        );

        assertResponses(
            operation(
                document,
                "/api/campanhas",
                "get"
            ),
            "200",
            "401",
            "403"
        );
    }

    @Test
    void deveDocumentarSchemasComExemplos()
        throws Exception {

        JsonNode schemas = obterDocumento()
            .path("components")
            .path("schemas");

        assertThat(
            schemas.path("LoginRequest")
                .path("properties")
                .path("username")
                .path("example")
                .asText()
        ).isEqualTo("cliente");

        assertThat(
            schemas.path("CriarPedidoRequest")
                .path("properties")
                .path("canalPedido")
                .path("example")
                .asText()
        ).isEqualTo("APP");

        assertThat(
            schemas.path("CheckoutResponse")
                .path("properties")
                .path("gateway")
                .path("example")
                .asText()
        ).isEqualTo("MOCK_GATEWAY");

        assertThat(
            schemas.path("ApiError")
                .path("properties")
                .path("error")
                .path("example")
                .asText()
        ).isEqualTo("ESTOQUE_INSUFICIENTE");
    }

    @Test
    void deveMarcarAnonimizacaoMockComoObsoleta()
        throws Exception {

        JsonNode operation = operation(
            obterDocumento(),
            "/api/v1/lgpd/anonimizar",
            "delete"
        );

        assertThat(
            operation.path("deprecated").asBoolean()
        ).isTrue();

        assertThat(
            operation.path("description").asText()
        )
            .contains(
                "não altera CPF, e-mail"
            )
            .contains(
                "Não deve ser utilizado como evidência"
            );
    }

    private JsonNode obterDocumento()
        throws Exception {

        MvcResult result = mockMvc.perform(
                get("/v3/api-docs")
            )
            .andExpect(status().isOk())
            .andExpect(
                content().contentTypeCompatibleWith(
                    "application/json"
                )
            )
            .andReturn();

        return objectMapper.readTree(
            result.getResponse()
                .getContentAsString()
        );
    }

    private JsonNode operation(
        JsonNode document,
        String path,
        String method
    ) {
        JsonNode operation = document
            .path("paths")
            .path(path)
            .path(method);

        assertThat(
            operation.isMissingNode()
        )
            .as(
                "Operação %s %s deve existir",
                method.toUpperCase(),
                path
            )
            .isFalse();

        return operation;
    }

    private void assertPublicOperation(
        JsonNode operation
    ) {
        JsonNode security =
            operation.get("security");

        assertThat(
            security == null
                || security.isEmpty()
        ).isTrue();
    }

    private void assertBearerProtected(
        JsonNode operation
    ) {
        JsonNode security =
            operation.path("security");

        assertThat(
            security.isArray()
        ).isTrue();

        boolean containsBearerAuth =
            StreamSupport.stream(
                security.spliterator(),
                false
            )
            .anyMatch(item ->
                item.has("bearerAuth")
            );

        assertThat(
            containsBearerAuth
        ).isTrue();
    }

    private void assertResponses(
        JsonNode operation,
        String... expectedCodes
    ) {
        JsonNode responses =
            operation.path("responses");

        for (String code : expectedCodes) {
            assertThat(
                responses.has(code)
            )
                .as(
                    "A resposta HTTP %s deve "
                        + "estar documentada",
                    code
                )
                .isTrue();
        }
    }
}
