package com.documentreader.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.documentreader.ai.OpenAiChatService;
import java.net.URI;
import java.util.List;
import com.documentreader.api.dto.GenerateWordFromPdfRequest;
import com.documentreader.api.dto.GenerateWordRequest;
import com.documentreader.docx.DocxWriterService;
import com.documentreader.pdf.PdfFetchService;
import com.documentreader.pdf.PdfTextExtractionService;
import com.documentreader.storage.LocalDocumentStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = WordDocumentController.class)
class WordDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OpenAiChatService openAiChatService;

    @MockitoBean
    private DocxWriterService docxWriterService;

    @MockitoBean
    private LocalDocumentStorageService storage;

    @MockitoBean
    private PdfFetchService pdfFetchService;

    @MockitoBean
    private PdfTextExtractionService pdfTextExtractionService;

    @Test
    void generatesDocxWithDisposition() throws Exception {
        Mockito.when(openAiChatService.generateFromPrompt(eq("Write one paragraph."), eq("ctx"), eq(true)))
                .thenReturn("Hello world");
        Mockito.when(docxWriterService.writeDocument("Hello world")).thenReturn(new byte[] {1, 2, 3, 4});

        mockMvc.perform(
                        post("/api/v1/documents/word")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new GenerateWordRequest(
                                                        "Write one paragraph.", "ctx", true, "out.docx"))))
                .andExpect(status().isOk())
                .andExpect(
                        header()
                                .string(
                                        "Content-Disposition",
                                        org.hamcrest.Matchers.containsString("out.docx")));
    }

    @Test
    void generatesDocxFromPdfUrlWithDisposition() throws Exception {
        String pdfUrl = "https://x.test/a.pdf";
        Mockito.when(pdfFetchService.fetch(URI.create(pdfUrl))).thenReturn(new byte[] {'%', 'P', 'D', 'F', '-', '1', '.', '4', '\n'});
        Mockito.when(pdfTextExtractionService.extract(any()))
                .thenReturn(new PdfTextExtractionService.ExtractionResult("hello pdf", List.of()));
        Mockito.when(openAiChatService.generateFromPrompt(eq("Write one paragraph."), eq("hello pdf"), eq(true)))
                .thenReturn("Hello world");
        Mockito.when(docxWriterService.writeDocument("Hello world")).thenReturn(new byte[] {1, 2, 3, 4});

        mockMvc.perform(
                        post("/api/v1/documents/word-from-pdf")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new GenerateWordFromPdfRequest(
                                                        pdfUrl, "Write one paragraph.", true, "out2.docx"))))
                .andExpect(status().isOk())
                .andExpect(
                        header()
                                .string(
                                        "Content-Disposition",
                                        org.hamcrest.Matchers.containsString("out2.docx")));
    }
}
