package com.example.ttsbackend.config;

import java.util.Set;

/**
 * Languages supported by this application.
 * Must match the frontend LanguageSelector list.
 */
public final class SupportedLanguages {

    private SupportedLanguages() {}

    public static final Set<String> CODES = Set.of(
            "en-US",   // English (US)
            "en-GB",   // English (UK)
            "en-IN",   // English (India)
            "hi-IN",   // Hindi
            "gu-IN",   // Gujarati
            "mr-IN",   // Marathi
            "es-ES",   // Spanish
            "fr-FR",   // French
            "de-DE"    // German
    );

    public static boolean isSupported(String code) {
        return code != null && CODES.contains(code);
    }
}