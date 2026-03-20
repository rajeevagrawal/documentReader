package com.documentreader.api.dto;

import java.util.List;

public record ExtractPdfResponse(
        String status, String extractedText, List<String> warnings, String message) {

    public static ExtractPdfResponse ready(String text, List<String> warnings) {
        return new ExtractPdfResponse("READY", text, warnings == null ? List.of() : warnings, null);
    }

    public static ExtractPdfResponse failed(String message) {
        return new ExtractPdfResponse("FAILED", null, List.of(), message);
    }
}
