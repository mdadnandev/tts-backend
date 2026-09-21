package com.example.ttsbackend.service;

import com.example.ttsbackend.exception.TtsException;
import com.example.ttsbackend.model.TtsRequest;
import com.example.ttsbackend.model.VoiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@ConditionalOnProperty(name = "tts.provider", havingValue = "mock", matchIfMissing = true)
public class MockTtsService implements TtsService {

    private static final Logger log = LoggerFactory.getLogger(MockTtsService.class);

    @Override
    public byte[] synthesize(TtsRequest request) {
        log.info("[MOCK] Synthesize: lang={}, voice={}, textLen={}",
                request.getLanguage(), request.getVoice(),
                request.getText() == null ? 0 : request.getText().length());

        if (request.getText() == null || request.getText().isBlank()) {
            throw new TtsException("Text must not be empty", 400);
        }

        return ("MOCK_AUDIO:" + request.getText()).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<VoiceInfo> listVoices() {
        return getFallbackVoices();
    }

    @Override
    public List<VoiceInfo> listVoices(String languageCode) {
        return getFallbackVoices().stream()
                .filter(v -> v.getLanguageCode().equals(languageCode))
                .toList();
    }

    private List<VoiceInfo> getFallbackVoices() {
        return List.of(
                new VoiceInfo("mock-male-1",    "Mock Male 1",    "en-US", "MALE",   "24000"),
                new VoiceInfo("mock-female-1",  "Mock Female 1",  "en-US", "FEMALE", "24000"),
                new VoiceInfo("mock-hindi-1",   "Mock Hindi 1",   "hi-IN", "FEMALE", "24000"),
                new VoiceInfo("mock-spanish-1", "Mock Spanish 1", "es-ES", "MALE",   "24000")
        );
    }
}