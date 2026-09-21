package com.example.ttsbackend.exception;

public class TtsException extends RuntimeException {

    private final int statusCode;

    public TtsException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}