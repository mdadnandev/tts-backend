package com.example.ttsbackend.model;

public class TtsResponse {

    private boolean success;
    private String audioBase64;
    private String format;
    private long durationMs;

    public TtsResponse() {}

    public TtsResponse(boolean success, String audioBase64, String format, long durationMs) {
        this.success = success;
        this.audioBase64 = audioBase64;
        this.format = format;
        this.durationMs = durationMs;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getAudioBase64() { return audioBase64; }
    public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}