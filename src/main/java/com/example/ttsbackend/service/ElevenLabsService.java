package com.example.ttsbackend.service;

import com.example.ttsbackend.exception.TtsException;
import com.example.ttsbackend.model.TtsRequest;
import com.example.ttsbackend.model.VoiceInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
@ConditionalOnProperty(name = "tts.provider", havingValue = "elevenlabs")
public class ElevenLabsService implements TtsService {

    private static final Logger log = LoggerFactory.getLogger(ElevenLabsService.class);

    @Value("${tts.elevenlabs.api-key}")
    private String apiKey;

    @Value("${tts.elevenlabs.base-url}")
    private String baseUrl;

    @Value("${tts.elevenlabs.model-id}")
    private String modelId;

    private final RestTemplate restTemplate = new RestTemplate();

    // Cached voice IDs — used for validation
    private final AtomicReference<Set<String>> cachedVoiceIds =
            new AtomicReference<>(new HashSet<>());

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("ELEVENLABS_API_KEY is not set!");
        } else {
            log.info("ElevenLabsService initialized. Key prefix: {}...",
                    apiKey.substring(0, Math.min(8, apiKey.length())));
            // Warm up the cache
            try {
                refreshVoiceCache();
            } catch (Exception e) {
                log.warn("Failed to warm up voice cache: {}", e.getMessage());
            }
        }
    }

    // -----------------------------------------------------------
    // Voice ID cache (validates before hitting ElevenLabs)
    // -----------------------------------------------------------
    private void refreshVoiceCache() {
        try {
            List<VoiceInfo> voices = listVoices();
            Set<String> ids = new HashSet<>();
            for (VoiceInfo v : voices) {
                if (v.getId() != null) ids.add(v.getId());
            }
            cachedVoiceIds.set(ids);
            log.info("Voice cache refreshed: {} voice IDs cached", ids.size());
        } catch (Exception e) {
            log.warn("Voice cache refresh failed: {}", e.getMessage());
        }
    }

    public boolean isValidVoice(String voiceId) {
        if (voiceId == null || voiceId.isBlank()) return false;
        Set<String> cached = cachedVoiceIds.get();
        // If cache is empty (startup issue), accept everything and let
        // ElevenLabs reject invalid ones — safer than blocking all voices.
        if (cached.isEmpty()) return true;
        return cached.contains(voiceId);
    }

    // -----------------------------------------------------------
    // Synthesize: text → MP3 bytes
    // -----------------------------------------------------------
    @Override
    public byte[] synthesize(TtsRequest request) {
        log.info("ElevenLabs synthesize: voice={}, textLen={}",
                request.getVoice(),
                request.getText() == null ? 0 : request.getText().length());

        if (apiKey == null || apiKey.isBlank()) {
            throw new TtsException("TTS service not configured properly", 503);
        }
        if (request.getText() == null || request.getText().isBlank()) {
            throw new TtsException("Text must not be empty", 400);
        }
        if (request.getVoice() == null || request.getVoice().isBlank()) {
            throw new TtsException("Voice must be specified", 400);
        }

        // Upfront voice validation — avoid hitting ElevenLabs with garbage
        if (!isValidVoice(request.getVoice())) {
            throw new TtsException(
                    "Voice not supported: '" + request.getVoice() + "'", 400);
        }

        try {
            String url = baseUrl + "/text-to-speech/" + request.getVoice();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", apiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));

            Map<String, Object> body = Map.of(
                    "text", request.getText(),
                    "model_id", modelId,
                    "voice_settings", Map.of(
                            "stability", 0.5,
                            "similarity_boost", 0.5
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, byte[].class);

            byte[] audio = response.getBody();
            if (audio == null || audio.length == 0) {
                throw new TtsException("Empty audio response", 503);
            }

            log.info("Received {} bytes of MP3 audio", audio.length);
            return audio;

        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("ElevenLabs API returned status {}: {}",
                    e.getStatusCode(), errorBody);

            int status = e.getStatusCode().value();
            if (status == 400 || status == 422) {
                throw new TtsException("Invalid request to TTS provider", 400);
            }
            if (status == 401) {
                throw new TtsException("TTS authentication failed", 503);
            }
            if (status == 402) {
                throw new TtsException(
                        "TTS quota exceeded or voice requires paid plan", 402);
            }
            if (status == 429) {
                throw new TtsException(
                        "TTS rate limit exceeded. Try again later.", 503);
            }
            throw new TtsException("TTS provider error", 503);
        }
    }

    // -----------------------------------------------------------
    // List voices
    // -----------------------------------------------------------
    @Override
    public List<VoiceInfo> listVoices() {
        try {
            String url = baseUrl + "/voices";

            HttpHeaders headers = new HttpHeaders();
            headers.set("xi-api-key", apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            if (body == null) return getFallbackVoices();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> voices =
                    (List<Map<String, Object>>) body.get("voices");
            if (voices == null) return getFallbackVoices();

            List<VoiceInfo> result = new ArrayList<>();
            for (Map<String, Object> v : voices) {
                String voiceId = (String) v.get("voice_id");
                if (voiceId == null) continue;

                String voiceName = (String) v.get("name");
                if (voiceName == null) voiceName = "Unnamed";

                String category = (String) v.get("category");
                if (category == null) category = "premade";

                String gender = "NEUTRAL";
                @SuppressWarnings("unchecked")
                Map<String, Object> labels = (Map<String, Object>) v.get("labels");
                if (labels != null && labels.get("gender") != null) {
                    String raw = labels.get("gender").toString().toUpperCase();
                    if (raw.equals("MALE") || raw.equals("FEMALE")) {
                        gender = raw;
                    }
                }

                result.add(new VoiceInfo(
                        voiceId,
                        voiceName,
                        category,
                        gender,
                        "44100"
                ));
            }

            log.info("Fetched {} voices from ElevenLabs", result.size());
            return result;

        } catch (Exception e) {
            log.error("Failed to fetch voices: {}", e.getMessage());
            return getFallbackVoices();
        }
    }

    @Override
    public List<VoiceInfo> listVoices(String languageCode) {
        return listVoices();
    }

    private List<VoiceInfo> getFallbackVoices() {
        return List.of(
                new VoiceInfo("EXAVITQu4vr4xnSDxMaL", "Sarah",  "premade", "FEMALE", "44100"),
                new VoiceInfo("ErXwobaYiN019PkySvjV", "Antoni", "premade", "MALE",   "44100"),
                new VoiceInfo("TxGEqnHWrfWFTfGW9XjX", "Josh",   "premade", "MALE",   "44100"),
                new VoiceInfo("pNInz6obpgDQGcFmaJgB", "Adam",   "premade", "MALE",   "44100")
        );
    }
}