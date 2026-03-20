package com.documentreader.ai;

/** Model returned empty or unusable content (treated as 422). */
public class AiRefusalException extends RuntimeException {

    public AiRefusalException(String message) {
        super(message);
    }
}
