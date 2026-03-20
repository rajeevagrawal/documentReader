package com.documentreader.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ExtractPdfRequest(@NotBlank String url) {}
