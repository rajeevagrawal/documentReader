package com.documentreader.pdf;

import com.documentreader.config.DocumentReaderProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class PdfTextExtractionService {

    private final DocumentReaderProperties properties;

    public PdfTextExtractionService(DocumentReaderProperties properties) {
        this.properties = properties;
    }

    public ExtractionResult extract(byte[] pdfBytes) {
        int maxPages = properties.getPdf().getMaxPages();
        List<String> warnings = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(new RandomAccessReadBuffer(pdfBytes))) {
            int pages = doc.getNumberOfPages();
            if (pages > maxPages) {
                warnings.add("Only the first " + maxPages + " pages were processed");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(Math.min(pages, maxPages));
            String text = stripper.getText(doc);
            String normalized = text == null ? "" : text.trim();
            if (normalized.isEmpty()) {
                warnings.add("No extractable text (may be image-only or empty)");
            }
            return new ExtractionResult(normalized, warnings);
        } catch (IOException e) {
            throw new InvalidPdfException("Could not read PDF: " + e.getMessage());
        }
    }

    public record ExtractionResult(String text, List<String> warnings) {}
}
