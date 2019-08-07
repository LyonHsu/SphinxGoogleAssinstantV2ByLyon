package com.example.androidthings.assistant.Sphinx;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class LyonSpeechRecognizerSetup  {

    private final Config config;

    public static LyonSpeechRecognizerSetup defaultSetup() {
        return new LyonSpeechRecognizerSetup(Decoder.defaultConfig());
    }

    public static LyonSpeechRecognizerSetup setupFromFile(File configFile) {
        return new LyonSpeechRecognizerSetup(Decoder.fileConfig(configFile.getPath()));
    }

    private LyonSpeechRecognizerSetup(Config config) {
        this.config = config;
    }

    public LyonSpeechRecognizer getRecognizer(Context context) throws IOException {
        return new LyonSpeechRecognizer(context,this.config);
    }

    public LyonSpeechRecognizerSetup setAcousticModel(File model) {
        return this.setString("-hmm", model.getPath());
    }

    public LyonSpeechRecognizerSetup setDictionary(File dictionary) {
        return this.setString("-dict", dictionary.getPath());
    }

    public LyonSpeechRecognizerSetup setSampleRate(int rate) {
        return this.setFloat("-samprate", (double)rate);
    }

    public LyonSpeechRecognizerSetup setRawLogDir(File dir) {
        return this.setString("-rawlogdir", dir.getPath());
    }

    public LyonSpeechRecognizerSetup setKeywordThreshold(float threshold) {
        return this.setFloat("-kws_threshold", (double)threshold);
    }

    public LyonSpeechRecognizerSetup setBoolean(String key, boolean value) {
        this.config.setBoolean(key, value);
        return this;
    }

    public LyonSpeechRecognizerSetup setInteger(String key, int value) {
        this.config.setInt(key, value);
        return this;
    }

    public LyonSpeechRecognizerSetup setFloat(String key, double value) {
        this.config.setFloat(key, value);
        return this;
    }

    public LyonSpeechRecognizerSetup setString(String key, String value) {
        this.config.setString(key, value);
        return this;
    }

    static {
        System.loadLibrary("pocketsphinx_jni");
    }
}
