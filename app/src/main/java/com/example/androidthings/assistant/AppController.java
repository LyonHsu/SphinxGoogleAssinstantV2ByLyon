package com.example.androidthings.assistant;

import android.app.Application;
import android.app.UiModeManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;

import com.example.androidthings.assistant.BlueTooth.BluetoothTool;
import com.example.androidthings.assistant.TextToSpeech.LyonTextToSpeech;
import com.example.androidthings.assistant.Tool.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppController extends Application {

    static String TAG = AppController.class.getName();
    private static AppController appController;
    private TextToSpeech mTtsEngine;
    private static final String UTTERANCE_ID =
            "com.example.androidthings.bluetooth.audio.UTTERANCE_ID";



    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        appController = this;
        initTts();
    }

    public static synchronized AppController getInstance() {
        return appController;
    }

    private void initTts() {
        mTtsEngine = new TextToSpeech(AppController.this,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            mTtsEngine.setLanguage(Locale.US);
                        } else {
                            Log.w(TAG, "Could not open TTS Engine (onInit status=" + status
                                    + "). Ignoring text to speech");
                            mTtsEngine = null;
                        }
                    }
                });
    }
    public void speak(Context context,String utterance) {
        Log.i(TAG, utterance);
        if (mTtsEngine != null) {
            mTtsEngine.speak(utterance, TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID);
            LyonTextToSpeech.speak(context,mTtsEngine,utterance);
        }
    }

    public static boolean checkPiDevice(Context mContext){
        boolean isPiDevice = true;
        UiModeManager uiModeManager = (UiModeManager) mContext.getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.d(TAG, "checkTVDevice Running on a TV Device");
            isPiDevice = true;
        } else {
            Log.d(TAG, "checkTVDevice Running on a non-TV Device");
            isPiDevice = false;
        }
        return isPiDevice;
    }


}
