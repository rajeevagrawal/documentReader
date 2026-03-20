package com.documentreader.pdf;

/** Base type for PDF pipeline failures mapped to HTTP errors. */
public class PdfServiceException extends RuntimeException {

    public PdfServiceException(String message) {
        super(message);
    }

    public PdfServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
