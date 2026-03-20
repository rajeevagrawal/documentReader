package com.documentreader.api.util;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SafeFileNames {

    private static final Pattern SAFE = Pattern.compile("^[a-zA-Z0-9._-]+\\.docx$");

    private SafeFileNames() {}

    public static String docxFileName(String requested) {
        if (requested == null || requested.isBlank()) {
            return "document.docx";
        }
        String name = Path.of(requested).getFileName().toString().trim();
        if (!name.toLowerCase(Locale.ROOT).endsWith(".docx")) {
            name = name + ".docx";
        }
        if (!SAFE.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "fileName must match [a-zA-Z0-9._-]+.docx (no paths or special characters)");
        }
        return name;
    }
}
