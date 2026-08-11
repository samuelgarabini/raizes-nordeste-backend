package com.raizes.nordeste.pedidos.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME =
        "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title(
                        "Rede Raízes do Nordeste - API"
                    )
                    .version("1.0.0")
                    .description(
                        """
                        API REST do projeto acadêmico \
                        Rede Raízes do Nordeste.

                        A aplicação oferece autenticação \
                        JWT, cardápio por unidade, pedidos \
                        multicanal, checkout com pagamento \
                        simulado, campanhas promocionais, \
                        fidelidade e ciclo operacional de \
                        status.

                        As rotas protegidas utilizam o \
                        esquema bearerAuth. Após realizar \
                        o login, informe o token JWT no \
                        botão Authorize do Swagger UI.
                        """
                    )
                    .contact(
                        new Contact()
                            .name(
                                "Samuel Ferreira Garabini"
                            )
                            .url(
                                "https://github.com/"
                                    + "samuelgarabini/"
                                    + "raizes-nordeste-backend"
                            )
                    )
            )
            .externalDocs(
                new ExternalDocumentation()
                    .description(
                        "Repositório e instruções "
                            + "de execução"
                    )
                    .url(
                        "https://github.com/"
                            + "samuelgarabini/"
                            + "raizes-nordeste-backend"
                    )
            )
            .components(
                new Components()
                    .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                            .name("Authorization")
                            .description(
                                "Token JWT obtido em "
                                    + "POST /api/v1/auth/login"
                            )
                            .type(
                                SecurityScheme.Type.HTTP
                            )
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            );
    }
}