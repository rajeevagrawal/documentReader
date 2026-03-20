package com.documentreader.api;

import com.documentreader.ai.OpenAiChatService;
import com.documentreader.api.dto.GenerateWordRequest;
import com.documentreader.api.util.SafeFileNames;
import com.documentreader.docx.DocxWriterService;
import jakarta.validation.Valid;
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

    public WordDocumentController(OpenAiChatService openAiChatService, DocxWriterService docxWriterService) {
        this.openAiChatService = openAiChatService;
        this.docxWriterService = docxWriterService;
    }

    @PostMapping("/word")
    public ResponseEntity<byte[]> generateWord(@Valid @RequestBody GenerateWordRequest request) {
        String fileName = SafeFileNames.docxFileName(request.fileName());
        String generated =
                openAiChatService.generateFromPrompt(
                        request.prompt(), request.pdfContextText(), request.usePdfContextEffective());
        byte[] docx = docxWriterService.writeDocument(generated);
        ContentDisposition disposition =
                ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(DOCX)
                .contentLength(docx.length)
                .body(docx);
    }
}
