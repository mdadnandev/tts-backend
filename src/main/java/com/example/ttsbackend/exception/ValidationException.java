package com.example.ttsbackend.exception;

public class ValidationException extends TtsException {

    public ValidationException(String message) {
        super(message, 400);
    }
}