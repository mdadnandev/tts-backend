package com.example.ttsbackend.service;

import com.example.ttsbackend.model.TtsRequest;
import com.example.ttsbackend.model.VoiceInfo;

import java.util.List;

public interface TtsService {

    byte[] synthesize(TtsRequest request);

    List<VoiceInfo> listVoices();

    List<VoiceInfo> listVoices(String languageCode);
}