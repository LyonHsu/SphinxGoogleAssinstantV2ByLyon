package com.example.androidthings.assistant.Sphinx;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.androidthings.assistant.AssistantActivity;
import com.example.androidthings.assistant.BoardDefaults;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class LyonSpeechRecognizer extends SpeechRecognizer {

    protected static final String TAG = SpeechRecognizer.class.getSimpleName();
    private final Decoder decoder;
    private final int sampleRate;
    private static final float BUFFER_SIZE_SECONDS = 0.4F;
    private int bufferSize;
    private final AudioRecord recorder;
    private Thread recognizerThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Collection<RecognitionListener> listeners = new HashSet();
    private static final boolean USE_VOICEHAT_I2S_DAC = Build.DEVICE.equals(BoardDefaults.DEVICE_RPI3);
    Context context;
    protected LyonSpeechRecognizer(Context context, Config config) throws IOException {
        super(config);
        this.context=context;
        this.decoder = new Decoder(config);
        this.sampleRate = (int)this.decoder.getConfig().getFloat("-samprate");
        this.bufferSize = (int)Math.round((float)this.sampleRate * 0.4F);
        this.recorder = new AudioRecord(6, this.sampleRate, 16, 2, this.bufferSize * 2);
        if (this.recorder.getState() == 0) {
            this.recorder.release();
            throw new IOException("Failed to initialize recorder. Microphone might be already in use.");
        }

        AudioDeviceInfo audioInputDevice = null;
        AudioDeviceInfo audioOutputDevice = null;
        if (USE_VOICEHAT_I2S_DAC) {
            if(AssistantActivity.isGoogleAIY) {
                audioInputDevice = findAudioDevice(AudioManager.GET_DEVICES_INPUTS, AudioDeviceInfo.TYPE_BUS);
                this.recorder.setPreferredDevice(audioInputDevice);
                if (audioInputDevice == null) {
                    Log.e("Lyon"+TAG, "failed to find I2S audio input device, using default");
                }
            }else{
                audioInputDevice = findAudioDevice(AudioManager.GET_DEVICES_INPUTS, AudioDeviceInfo.TYPE_USB_DEVICE);
                this.recorder.setPreferredDevice(audioInputDevice);
                if (audioInputDevice == null) {
                    Log.e("Lyon"+TAG, "failed to find I2S audio input device, using Unknow");
                } else {
                    Log.d("Lyon"+TAG, " find USB audio input device, using USB");
                }
            }
        }

    }

    public void addListener(RecognitionListener listener) {
        synchronized(this.listeners) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(RecognitionListener listener) {
        synchronized(this.listeners) {
            this.listeners.remove(listener);
        }
    }

    public boolean startListening(String searchName) {
        if (null != this.recognizerThread) {
            return false;
        } else {
            Log.i(TAG, String.format("Start recognition \"%s\"", searchName));
            this.decoder.setSearch(searchName);
            this.recognizerThread = new LyonSpeechRecognizer.RecognizerThread();
            this.recognizerThread.start();
            return true;
        }
    }

    public boolean startListening(String searchName, int timeout) {
        if (null != this.recognizerThread) {
            return false;
        } else {
            Log.i(TAG, String.format("Start recognition \"%s\"", searchName));
            this.decoder.setSearch(searchName);
            this.recognizerThread = new LyonSpeechRecognizer.RecognizerThread(timeout);
            this.recognizerThread.start();
            return true;
        }
    }

    private boolean stopRecognizerThread() {
        if (null == this.recognizerThread) {
            return false;
        } else {
            try {
                this.recognizerThread.interrupt();
                this.recognizerThread.join();
            } catch (InterruptedException var2) {
                Thread.currentThread().interrupt();
            }

            this.recognizerThread = null;
            return true;
        }
    }

    public boolean stop() {
        boolean result = this.stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Stop recognition");
            Hypothesis hypothesis = this.decoder.hyp();
            this.mainHandler.post(new LyonSpeechRecognizer.ResultEvent(hypothesis, true));
        }

        return result;
    }

    public boolean cancel() {
        boolean result = this.stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Cancel recognition");
        }

        return result;
    }

    public Decoder getDecoder() {
        return this.decoder;
    }

    public void shutdown() {
        this.recorder.release();
    }

    public String getSearchName() {
        return this.decoder.getSearch();
    }

    public void addFsgSearch(String searchName, FsgModel fsgModel) {
        this.decoder.setFsg(searchName, fsgModel);
    }

    public void addGrammarSearch(String name, File file) {
        Log.i(TAG, String.format("Load JSGF %s", file));
        this.decoder.setJsgfFile(name, file.getPath());
    }

    public void addGrammarSearch(String name, String jsgfString) {
        this.decoder.setJsgfString(name, jsgfString);
    }

    public void addNgramSearch(String name, File file) {
        Log.i(TAG, String.format("Load N-gram model %s", file));
        this.decoder.setLmFile(name, file.getPath());
    }

    public void addKeyphraseSearch(String name, String phrase) {
        this.decoder.setKeyphrase(name, phrase);
    }

    public void addKeywordSearch(String name, File file) {
        this.decoder.setKws(name, file.getPath());
    }

    public void addAllphoneSearch(String name, File file) {
        this.decoder.setAllphoneFile(name, file.getPath());
    }

    private class TimeoutEvent extends LyonSpeechRecognizer.RecognitionEvent {
        private TimeoutEvent() {
            super();
        }

        protected void execute(RecognitionListener listener) {
            listener.onTimeout();
        }
    }

    private class OnErrorEvent extends LyonSpeechRecognizer.RecognitionEvent {
        private final Exception exception;

        OnErrorEvent(Exception exception) {
            super();
            this.exception = exception;
        }

        protected void execute(RecognitionListener listener) {
            listener.onError(this.exception);
        }
    }

    private class ResultEvent extends LyonSpeechRecognizer.RecognitionEvent {
        protected final Hypothesis hypothesis;
        private final boolean finalResult;

        ResultEvent(Hypothesis hypothesis, boolean finalResult) {
            super();
            this.hypothesis = hypothesis;
            this.finalResult = finalResult;
        }

        protected void execute(RecognitionListener listener) {
            if (this.finalResult) {
                listener.onResult(this.hypothesis);
            } else {
                listener.onPartialResult(this.hypothesis);
            }

        }
    }

    private class InSpeechChangeEvent extends LyonSpeechRecognizer.RecognitionEvent {
        private final boolean state;

        InSpeechChangeEvent(boolean state) {
            super();
            this.state = state;
        }

        protected void execute(RecognitionListener listener) {
            if (this.state) {
                listener.onBeginningOfSpeech();
            } else {
                listener.onEndOfSpeech();
            }

        }
    }

    private abstract class RecognitionEvent implements Runnable {
        private RecognitionEvent() {
        }

        public void run() {
            RecognitionListener[] emptyArray = new RecognitionListener[0];
            RecognitionListener[] var2 = (RecognitionListener[])LyonSpeechRecognizer.this.listeners.toArray(emptyArray);
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                RecognitionListener listener = var2[var4];
                this.execute(listener);
            }

        }

        protected abstract void execute(RecognitionListener var1);
    }

    private final class RecognizerThread extends Thread {
        private int remainingSamples;
        private int timeoutSamples;
        private static final int NO_TIMEOUT = -1;

        public RecognizerThread(int timeout) {
            if (timeout != -1) {
                this.timeoutSamples = timeout * LyonSpeechRecognizer.this.sampleRate / 1000;
            } else {
                this.timeoutSamples = -1;
            }

            this.remainingSamples = this.timeoutSamples;
        }

        public RecognizerThread() {
            this(-1);
        }

        public void run() {
            LyonSpeechRecognizer.this.recorder.startRecording();
            if (LyonSpeechRecognizer.this.recorder.getRecordingState() == 1) {
                LyonSpeechRecognizer.this.recorder.stop();
                IOException ioe = new IOException("Failed to start recording. Microphone might be already in use.");
                LyonSpeechRecognizer.this.mainHandler.post(LyonSpeechRecognizer.this.new OnErrorEvent(ioe));
            } else {
                Log.d(LyonSpeechRecognizer.TAG, "Starting decoding");
                LyonSpeechRecognizer.this.decoder.startUtt();
                short[] buffer = new short[LyonSpeechRecognizer.this.bufferSize];
                boolean inSpeech = LyonSpeechRecognizer.this.decoder.getInSpeech();
                LyonSpeechRecognizer.this.recorder.read(buffer, 0, buffer.length);

                while(!interrupted() && (this.timeoutSamples == -1 || this.remainingSamples > 0)) {
                    int nread = LyonSpeechRecognizer.this.recorder.read(buffer, 0, buffer.length);
                    if (-1 == nread) {
                        throw new RuntimeException("error reading audio buffer");
                    }

                    if (nread > 0) {
                        LyonSpeechRecognizer.this.decoder.processRaw(buffer, (long)nread, false, false);
                        if (LyonSpeechRecognizer.this.decoder.getInSpeech() != inSpeech) {
                            inSpeech = LyonSpeechRecognizer.this.decoder.getInSpeech();
                            LyonSpeechRecognizer.this.mainHandler.post(LyonSpeechRecognizer.this.new InSpeechChangeEvent(inSpeech));
                        }

                        if (inSpeech) {
                            this.remainingSamples = this.timeoutSamples;
                        }

                        Hypothesis hypothesis = LyonSpeechRecognizer.this.decoder.hyp();
                        LyonSpeechRecognizer.this.mainHandler.post(LyonSpeechRecognizer.this.new ResultEvent(hypothesis, false));
                    }

                    if (this.timeoutSamples != -1) {
                        this.remainingSamples -= nread;
                    }
                }

                LyonSpeechRecognizer.this.recorder.stop();
                LyonSpeechRecognizer.this.decoder.endUtt();
                LyonSpeechRecognizer.this.mainHandler.removeCallbacksAndMessages((Object)null);
                if (this.timeoutSamples != -1 && this.remainingSamples <= 0) {
                    LyonSpeechRecognizer.this.mainHandler.post(LyonSpeechRecognizer.this.new TimeoutEvent());
                }

            }
        }
    }

    public AudioDeviceInfo findAudioDevice(int deviceFlag, int deviceType) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] adis = manager.getDevices(deviceFlag);
        for (AudioDeviceInfo adi : adis) {
            if (adi.getType() == deviceType) {
                return adi;
            }
        }
        return null;
    }

}
