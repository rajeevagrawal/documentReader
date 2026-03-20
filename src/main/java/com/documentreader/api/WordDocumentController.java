package com.documentreader.api;

import com.documentreader.ai.OpenAiChatService;
import com.documentreader.api.dto.GenerateWordFromPdfRequest;
import com.documentreader.api.dto.GenerateWordRequest;
import com.documentreader.api.util.SafeFileNames;
import com.documentreader.docx.DocxWriterService;
import com.documentreader.pdf.PdfFetchService;
import com.documentreader.pdf.PdfTextExtractionService;
import com.documentreader.storage.LocalDocumentStorageService;
import jakarta.validation.Valid;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class WordDocumentController {

    private static final MediaType DOCX =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final OpenAiChatService openAiChatService;
    private final DocxWriterService docxWriterService;
    private final LocalDocumentStorageService storage;
    private final PdfFetchService pdfFetchService;
    private final PdfTextExtractionService pdfTextExtractionService;

    public WordDocumentController(
            OpenAiChatService openAiChatService,
            DocxWriterService docxWriterService,
            LocalDocumentStorageService storage,
            PdfFetchService pdfFetchService,
            PdfTextExtractionService pdfTextExtractionService) {
        this.openAiChatService = openAiChatService;
        this.docxWriterService = docxWriterService;
        this.storage = storage;
        this.pdfFetchService = pdfFetchService;
        this.pdfTextExtractionService = pdfTextExtractionService;
    }

    @PostMapping("/word")
    public ResponseEntity<byte[]> generateWord(@Valid @RequestBody GenerateWordRequest request) {
        String fileName = SafeFileNames.docxFileName(request.fileName());
        String generated =
                openAiChatService.generateFromPrompt(
                        request.prompt(), request.pdfContextText(), request.usePdfContextEffective());
        byte[] docx = docxWriterService.writeDocument(generated);
        storage.saveDocx(fileName, docx);
        ContentDisposition disposition =
                ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(DOCX)
                .contentLength(docx.length)
                .body(docx);
    }

    @PostMapping("/word-from-pdf")
    public ResponseEntity<byte[]> generateWordFromPdf(
            @Valid @RequestBody GenerateWordFromPdfRequest request) {
        URI uri = URI.create(request.url().trim());
        byte[] pdfBytes = pdfFetchService.fetch(uri);
        storage.savePdf(uri, pdfBytes);

        String extractedText = pdfTextExtractionService.extract(pdfBytes).text();

        String generated =
                openAiChatService.generateFromPrompt(
                        request.prompt(), extractedText, request.usePdfContextEffective());

        String fileName =
                SafeFileNames.docxFileName(
                        request.fileName() == null || request.fileName().isBlank()
                                ? "output.docx"
                                : request.fileName());

        byte[] docx = docxWriterService.writeDocument(generated);
        storage.saveDocx(fileName, docx);

        ContentDisposition disposition =
                ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(DOCX)
                .contentLength(docx.length)
                .body(docx);
    }
}
