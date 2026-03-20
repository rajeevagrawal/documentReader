package com.documentreader.api;

import com.documentreader.ai.AiRefusalException;
import com.documentreader.ai.AiUnavailableException;
import com.documentreader.pdf.InvalidPdfException;
import com.documentreader.pdf.PdfServiceException;
import com.documentreader.pdf.PdfTooLargeException;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI TYPE_BASE = URI.create("https://documentreader.local/problems/");

    @ExceptionHandler(URISyntaxException.class)
    public ResponseEntity<ProblemDetail> badUri(URISyntaxException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-url", "Invalid URL", ex.getMessage());
    }

    @ExceptionHandler(InvalidPdfException.class)
    public ResponseEntity<ProblemDetail> invalidPdf(InvalidPdfException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-pdf", "Invalid PDF", ex.getMessage());
    }

    @ExceptionHandler(PdfTooLargeException.class)
    public ResponseEntity<ProblemDetail> tooLarge(PdfTooLargeException ex) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "pdf-too-large", "PDF too large", ex.getMessage());
    }

    @ExceptionHandler(PdfServiceException.class)
    public ResponseEntity<ProblemDetail> pdfFetchFailed(PdfServiceException ex) {
        return problem(HttpStatus.BAD_GATEWAY, "pdf-fetch-failed", "Upstream fetch failed", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> badArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "bad-request", "Bad request", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
        String detail =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .findFirst()
                        .orElse("Validation failed");
        return problem(HttpStatus.BAD_REQUEST, "validation", "Validation failed", detail);
    }

    @ExceptionHandler(AiUnavailableException.class)
    public ResponseEntity<ProblemDetail> aiDown(AiUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "ai-unavailable", "AI unavailable", ex.getMessage());
    }

    @ExceptionHandler(AiRefusalException.class)
    public ResponseEntity<ProblemDetail> aiRefusal(AiRefusalException ex) {
        return problem(
                HttpStatus.UNPROCESSABLE_ENTITY, "ai-refusal", "Generation refused", ex.getMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(
            HttpStatus status, String typeSuffix, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(TYPE_BASE.resolve(typeSuffix));
        return ResponseEntity.status(status).body(pd);
    }
}
