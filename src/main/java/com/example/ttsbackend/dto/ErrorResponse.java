package com.example.ttsbackend.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private boolean success = false;
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse() {}

    public ErrorResponse(boolean success, int status, String error, String message) {
        this.success = success;
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}