package com.example.ttsbackend.model;

public class VoiceInfo {

    private String id;
    private String name;
    private String languageCode;
    private String gender;
    private String naturalSampleRateHertz;

    public VoiceInfo() {}

    public VoiceInfo(String id, String name, String languageCode,
                     String gender, String naturalSampleRateHertz) {
        this.id = id;
        this.name = name;
        this.languageCode = languageCode;
        this.gender = gender;
        this.naturalSampleRateHertz = naturalSampleRateHertz;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getNaturalSampleRateHertz() { return naturalSampleRateHertz; }
    public void setNaturalSampleRateHertz(String naturalSampleRateHertz) {
        this.naturalSampleRateHertz = naturalSampleRateHertz;
    }
}