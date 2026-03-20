package com.documentreader.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateWordRequest(
        @NotBlank String prompt,
        String pdfContextText,
        Boolean usePdfContext,
        String fileName) {

    public boolean usePdfContextEffective() {
        return usePdfContext == null || usePdfContext;
    }
}
