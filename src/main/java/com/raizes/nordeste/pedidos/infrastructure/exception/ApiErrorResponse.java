package com.raizes.nordeste.pedidos.presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String error;
    private String message;
    private List<FieldErrorDetail> details;
    private Instant timestamp;
    private String path;
    private String requestId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String issue;
    }
}