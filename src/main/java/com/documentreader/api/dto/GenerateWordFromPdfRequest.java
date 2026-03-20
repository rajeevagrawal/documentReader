package com.documentreader.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateWordFromPdfRequest(
        @NotBlank String url,
        @NotBlank String prompt,
        Boolean usePdfContext,
        String fileName) {

    public boolean usePdfContextEffective() {
        return usePdfContext == null || usePdfContext;
    }
}
