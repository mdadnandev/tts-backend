package com.example.ttsbackend.controller;

import com.example.ttsbackend.config.SupportedLanguages;
import com.example.ttsbackend.exception.TtsException;
import com.example.ttsbackend.model.TtsRequest;
import com.example.ttsbackend.model.TtsResponse;
import com.example.ttsbackend.model.VoiceInfo;
import com.example.ttsbackend.service.TtsService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TtsController {

    private final TtsService ttsService;

    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping(value = "/tts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TtsResponse> synthesize(@Valid @RequestBody TtsRequest request) {

        // ---- Language validation ----
        if (!SupportedLanguages.isSupported(request.getLanguage())) {
            throw new TtsException(
                    "Language not supported: '" + request.getLanguage() + "'. " +
                            "Supported languages: " + SupportedLanguages.CODES,
                    400);
        }

        byte[] audioBytes = ttsService.synthesize(request);
        String base64 = Base64.getEncoder().encodeToString(audioBytes);

        String format = "mp3";
        if (audioBytes.length >= 4 && audioBytes[0] == 'R' && audioBytes[1] == 'I' && audioBytes[2] == 'F' && audioBytes[3] == 'F') {
            format = "wav";
        }

        TtsResponse response = new TtsResponse(
                true,
                base64,
                format,
                estimateDurationMs(request.getText())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/voices")
    public ResponseEntity<List<VoiceInfo>> listVoices(
            @RequestParam(required = false) String language) {

        // If a language is given, validate it
        if (language != null && !language.isBlank()
                && !SupportedLanguages.isSupported(language)) {
            throw new TtsException(
                    "Language not supported: '" + language + "'", 400);
        }

        List<VoiceInfo> voices = (language != null && !language.isBlank())
                ? ttsService.listVoices(language)
                : ttsService.listVoices();
        return ResponseEntity.ok(voices);
    }

    private long estimateDurationMs(String text) {
        if (text == null || text.isBlank()) return 0;
        int words = text.trim().split("\\s+").length;
        return (long) ((words / 150.0) * 60_000);
    }
}