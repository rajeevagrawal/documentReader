package com.documentreader.api;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.documentreader.api.dto.ExtractPdfRequest;
import com.documentreader.pdf.PdfFetchService;
import com.documentreader.pdf.PdfTextExtractionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PdfExtractController.class)
class PdfExtractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PdfFetchService pdfFetchService;

    @MockitoBean
    private PdfTextExtractionService pdfTextExtractionService;

    @Test
    void extractReturnsReady() throws Exception {
        Mockito.when(pdfFetchService.fetch(any())).thenReturn(new byte[] {'%', 'P', 'D', 'F', '-', '1', '.', '4', '\n'});
        Mockito.when(pdfTextExtractionService.extract(any()))
                .thenReturn(new PdfTextExtractionService.ExtractionResult("hello", List.of("w")));

        mockMvc.perform(
                        post("/api/v1/pdf/extract")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new ExtractPdfRequest("https://x.test/a.pdf"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.extractedText").value("hello"));
    }
}
