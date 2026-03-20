package com.documentreader.storage;

import com.documentreader.config.DocumentReaderProperties;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class LocalDocumentStorageService {

    private final DocumentReaderProperties properties;

    public LocalDocumentStorageService(DocumentReaderProperties properties) {
        this.properties = properties;
    }

    public void savePdf(URI sourceUri, byte[] pdfBytes) {
        if (!properties.getStorage().isEnabled()) {
            return;
        }
        try {
            Path dir = Path.of(properties.getStorage().getPdfDir());
            Files.createDirectories(dir);
            String fileName = "pdf-" + sha256Hex(sourceUri.toString()) + ".pdf";
            writeBytes(dir.resolve(fileName), pdfBytes);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to persist downloaded PDF to " + properties.getStorage().getPdfDir(),
                    e);
        }
    }

    public void saveDocx(String fileName, byte[] docxBytes) {
        if (!properties.getStorage().isEnabled()) {
            return;
        }
        try {
            Path dir = Path.of(properties.getStorage().getWordDir());
            Files.createDirectories(dir);
            writeBytes(dir.resolve(fileName), docxBytes);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to persist generated .docx to " + properties.getStorage().getWordDir(),
                    e);
        }
    }

    private static void writeBytes(Path path, byte[] bytes) throws IOException {
        Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash content", e);
        }
    }
}

