package com.example.ttsbackend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TtsRequest {

    @NotBlank(message = "Text must not be empty")
    @Size(max = 5000, message = "Text must not exceed 5000 characters")
    private String text;

    @NotBlank(message = "Language must be specified")
    @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$",
            message = "Language must be a valid BCP-47 code, e.g. en-US")
    private String language;

    @NotBlank(message = "Voice must be specified")
    private String voice;

    public TtsRequest() {}

    public TtsRequest(String text, String language, String voice) {
        this.text = text;
        this.language = language;
        this.voice = voice;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getVoice() { return voice; }
    public void setVoice(String voice) { this.voice = voice; }
}