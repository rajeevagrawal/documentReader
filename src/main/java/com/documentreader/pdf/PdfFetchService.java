package com.documentreader.pdf;

import com.documentreader.config.DocumentReaderProperties;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class PdfFetchService {

    private static final byte[] PDF_MAGIC = new byte[] {'%', 'P', 'D', 'F'};

    private final WebClient pdfWebClient;
    private final DocumentReaderProperties properties;

    public PdfFetchService(WebClient pdfWebClient, DocumentReaderProperties properties) {
        this.pdfWebClient = pdfWebClient;
        this.properties = properties;
    }

    public byte[] fetch(URI uri) {
        validateScheme(uri);
        DocumentReaderProperties.Pdf pdf = properties.getPdf();
        Duration timeout = Duration.ofSeconds(pdf.getFetchTimeoutSeconds());

        try {
            byte[] body =
                    pdfWebClient
                            .get()
                            .uri(uri)
                            .exchangeToMono(
                                    response -> {
                                        if (!response.statusCode().is2xxSuccessful()) {
                                            return response.createException().flatMap(Mono::error);
                                        }
                                        List<String> lengths = response.headers().header("Content-Length");
                                        if (!lengths.isEmpty()) {
                                            try {
                                                long len = Long.parseLong(lengths.getFirst());
                                                if (len > pdf.getMaxBytes()) {
                                                    return Mono.error(
                                                            new PdfTooLargeException(
                                                                    "PDF Content-Length exceeds configured limit"));
                                                }
                                            } catch (NumberFormatException ignored) {
                                                // ignore malformed Content-Length
                                            }
                                        }
                                        return response.bodyToMono(byte[].class);
                                    })
                            .block(timeout);

            if (body == null || body.length == 0) {
                throw new PdfServiceException("Empty response body");
            }
            if (body.length > pdf.getMaxBytes()) {
                throw new PdfTooLargeException("Downloaded PDF exceeds configured size limit");
            }
            if (!startsWithPdfMagic(body)) {
                throw new InvalidPdfException("Response is not a PDF");
            }
            return body;
        } catch (WebClientResponseException e) {
            throw new PdfServiceException("Failed to fetch URL: HTTP " + e.getStatusCode().value(), e);
        } catch (PdfServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfServiceException("Failed to fetch PDF: " + e.getMessage(), e);
        }
    }

    private void validateScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new InvalidPdfException("URL must have a scheme");
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return;
        }
        if ("http".equalsIgnoreCase(scheme)) {
            if (!properties.getPdf().isAllowInsecureHttp()) {
                throw new InvalidPdfException(
                        "HTTP URLs are disabled; use HTTPS or enable document.reader.pdf.allow-insecure-http");
            }
            return;
        }
        throw new InvalidPdfException("Only http and https URLs are supported");
    }

    private static boolean startsWithPdfMagic(byte[] data) {
        if (data.length < PDF_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (data[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }
}
