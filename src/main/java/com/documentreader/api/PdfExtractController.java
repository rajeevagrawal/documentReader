package com.documentreader.api;

import com.documentreader.api.dto.ExtractPdfRequest;
import com.documentreader.api.dto.ExtractPdfResponse;
import com.documentreader.pdf.PdfFetchService;
import com.documentreader.pdf.PdfTextExtractionService;
import com.documentreader.storage.LocalDocumentStorageService;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pdf")
public class PdfExtractController {

    private final PdfFetchService pdfFetchService;
    private final PdfTextExtractionService pdfTextExtractionService;
    private final LocalDocumentStorageService storage;

    public PdfExtractController(
            PdfFetchService pdfFetchService,
            PdfTextExtractionService pdfTextExtractionService,
            LocalDocumentStorageService storage) {
        this.pdfFetchService = pdfFetchService;
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.storage = storage;
    }

    @PostMapping("/extract")
    public ExtractPdfResponse extract(@Valid @RequestBody ExtractPdfRequest request) throws URISyntaxException {
        URI uri = new URI(request.url().trim());
        byte[] bytes = pdfFetchService.fetch(uri);
        storage.savePdf(uri, bytes);
        PdfTextExtractionService.ExtractionResult result = pdfTextExtractionService.extract(bytes);
        return ExtractPdfResponse.ready(result.text(), result.warnings());
    }
}
