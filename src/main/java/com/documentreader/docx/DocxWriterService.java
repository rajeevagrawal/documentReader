package com.documentreader.docx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

@Service
public class DocxWriterService {

    public byte[] writeDocument(String bodyText) {
        List<String> paragraphs =
                Arrays.stream(bodyText.split("\n\n+"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
        if (paragraphs.isEmpty()) {
            paragraphs = List.of(bodyText.trim().isEmpty() ? " " : bodyText.trim());
        }
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String para : paragraphs) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(para);
            }
            doc.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build Word document", e);
        }
    }
}
