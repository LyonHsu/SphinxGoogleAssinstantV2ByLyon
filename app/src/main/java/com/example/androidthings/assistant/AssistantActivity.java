/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.assistant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;

import com.example.androidthings.assistant.DialogFlow.DialogFlowInit;
import com.example.androidthings.assistant.EmbeddedAssistant.ConversationCallback;
import com.example.androidthings.assistant.EmbeddedAssistant.RequestCallback;
import com.example.androidthings.assistant.NetWork.NetWork;
import com.example.androidthings.assistant.NetWork.tool.Alert;
import com.example.androidthings.assistant.NetWork.tool.Permission;
import com.example.androidthings.assistant.Sphinx.CapTechSphinxManager;
import com.example.androidthings.assistant.TextToSpeech.LyonTextToSpeech;
import com.example.androidthings.assistant.Tool.ToastUtile;
import com.example.androidthings.assistant.Youtube.YoutubePlayer;
import com.example.androidthings.assistant.Youtube.YoutubePoster;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.voicehat.Max98357A;
import com.google.android.things.contrib.driver.voicehat.VoiceHat;
import com.google.android.things.pio.Gpio;
import com.google.assistant.embedded.v1alpha2.SpeechRecognitionResult;
import com.google.auth.oauth2.UserCredentials;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.androidthings.assistant.Tool.Utils;

import static com.example.androidthings.assistant.Sphinx.CapTechSphinxManager.ACTIVATION_KEYPHRASE;


public class AssistantActivity extends Activity implements Button.OnButtonEventListener, CapTechSphinxManager.SphinxListener {
    private static final String TAG = AssistantActivity.class.getSimpleName();

    // Peripheral and drivers constants.
    private static final int BUTTON_DEBOUNCE_DELAY_MS = 20;
    // Default on using the Voice Hat on Raspberry Pi 3.
    private static final boolean USE_VOICEHAT_I2S_DAC = Build.DEVICE.equals(BoardDefaults.DEVICE_RPI3);

    // Audio constants.
    private static final String PREF_CURRENT_VOLUME = "current_volume";
    private static final int SAMPLE_RATE = 16000;
    private static final int DEFAULT_VOLUME = 100;

    // Assistant SDK constants.
    private static final String DEVICE_MODEL_ID = "PLACEHOLDER";
    private static final String DEVICE_INSTANCE_ID = "PLACEHOLDER";
    private static final String LANGUAGE_CODE = "en-US";

    // Hardware peripherals.
    private Button mButton;
    private android.widget.Button mButtonWidget;
    private Gpio mLed;
    private Max98357A mDac;

    private Handler mMainHandler;

    // List & adapter to store and display the history of Assistant Requests.
    private EmbeddedAssistant mEmbeddedAssistant;
    private ArrayList<String> mAssistantRequests = new ArrayList<>();
    private ArrayAdapter<String> mAssistantRequestsAdapter;
    private CheckBox mHtmlOutputCheckbox;
    private WebView mWebView;

    //Sphinx
    //pocket sphinx for hot key
    private CapTechSphinxManager captechSphinxManager;
    boolean LEDShining = false;

    //NetWork
    NetWork netWork;

    //Is Use Google AIY Device
    public static boolean isGoogleAIY = false;

    Context context;
    ProgressDialog progressDialog;
    TextToSpeech textToSpeech;
    DialogFlowInit dialogFlowInit;
    String AISay="Yes";
    String openComplete = "開機完畢 你可以使用 " + ACTIVATION_KEYPHRASE + " 來喚醒";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "starting assistant demo");
        context = this;
        Permission permission = new Permission();
        if(!permission.checAudioRecordPermission(context)){
            Alert.showAlert(this, getString(R.string.wifititle), getString(R.string.wifioffmassage), "ok");
        }else {
            setContentView(R.layout.activity_main);

            textToSpeech= new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.d(TAG, "getTextToSpeech TTS init status:" + status);
                    if (status != TextToSpeech.ERROR) {
//                        int result = textToSpeech.setLanguage(Locale.getDefault());//Locale.);
                        textToSpeech.setPitch(1.0f); // 音調
                        textToSpeech.setSpeechRate(1.0f); // 速度
                        int result = textToSpeech.setLanguage(Locale.US);
                        textToSpeech.speak(openComplete, TextToSpeech.QUEUE_FLUSH, null);
                        Log.d(TAG, "getTextToSpeech speak result init:" + result);


                    }else{
                        Log.e(TAG, "getTextToSpeech TTS init Error:" + status);
                        ToastUtile.showText(context,"getTextToSpeech TTS init Error:" + status);
                    }
                }
            });


            //設定音量
            int systemName = AudioManager.STREAM_SYSTEM;
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maVolume = audioManager.getStreamMaxVolume(systemName);
            audioManager.setStreamVolume(systemName,maVolume,AudioManager.FLAG_SHOW_UI );
            systemName = AudioManager.STREAM_MUSIC;//STREAM_RING
            maVolume = audioManager.getStreamMaxVolume(systemName);
            audioManager.setStreamVolume(systemName,maVolume,AudioManager.FLAG_VIBRATE );
            systemName = AudioManager.STREAM_RING;//STREAM_RING
            maVolume = audioManager.getStreamMaxVolume(systemName);
            audioManager.setStreamVolume(systemName,maVolume,AudioManager.FLAG_VIBRATE );
            ToastUtile.showText(context,"設定音量為："+maVolume);
            Log.e(TAG,"設定音量為："+maVolume);

            setTurnScreenOn(true);


            try {
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("init");
                progressDialog.setMessage("beging....");
                progressDialog.show();
            }catch (Exception e){
                Log.e(TAG,Utils.FormatStackTrace(e));
            }
            final ListView assistantRequestsListView = findViewById(R.id.assistantRequestsListView);
            mAssistantRequestsAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                            mAssistantRequests);
            assistantRequestsListView.setAdapter(mAssistantRequestsAdapter);
            mHtmlOutputCheckbox = findViewById(R.id.htmlOutput);
            mHtmlOutputCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean useHtml) {
                    if (mEmbeddedAssistant != null) {
                        mWebView.setVisibility(useHtml ? View.VISIBLE : View.GONE);
                        assistantRequestsListView.setVisibility(useHtml ? View.GONE : View.VISIBLE);
                        mEmbeddedAssistant.setResponseFormat(useHtml
                                ? EmbeddedAssistant.HTML : EmbeddedAssistant.TEXT);
                    }
                }
            });
            mWebView = findViewById(R.id.webview);
            mWebView.getSettings().setJavaScriptEnabled(true);

            netWork = (NetWork) findViewById(R.id.network);
            netWork.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    netWork.superOnClick();
                    LyonTextToSpeech.speak(context,textToSpeech,netWork.getLocalIpAddress(context));

                }
            });

            mMainHandler = new Handler(getMainLooper());
            mButtonWidget = findViewById(R.id.assistantQueryButton);
            mButtonWidget.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mEmbeddedAssistant != null) {
                        captechSphinxManager.SpeechRecognizerStop();
                        mEmbeddedAssistant.startConversation();
                    }
                }
            });


            // Audio routing configuration: use default routing.
            /**
             * AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;//for 3.5mm
             * AudioDeviceInfo.TYPE_BUILTIN_MIC;//for 3.5mm
             * AudioDeviceInfo.TYPE_USB_DEVICE;//for USB
             * AudioDeviceInfo.TYPE_BUS;//for I2S like AIY
             * can't see the ReadMe Audio Configuration
             */
            AudioDeviceInfo audioInputDevice = null;
            AudioDeviceInfo audioOutputDevice = null;
            if (USE_VOICEHAT_I2S_DAC) {
                if (isGoogleAIY) {
                    audioInputDevice = findAudioDevice(AudioManager.GET_DEVICES_INPUTS, AudioDeviceInfo.TYPE_BUS);//TYPE_USB_DEVICE ,TYPE_BUS
                    if (audioInputDevice == null) {
                        Log.e(TAG, "failed to find I2S audio input device, using default");
                    } else {
                        Log.d(TAG, " find USB audio input device, using I2S");
                    }
                    audioOutputDevice = findAudioDevice(AudioManager.GET_DEVICES_OUTPUTS, AudioDeviceInfo.TYPE_BUS);
                    if (audioOutputDevice == null) {
                        Log.e(TAG, "failed to found I2S audio output device, using Unknow");
                    } else {
                        Log.d(TAG, " find USB audio input device, using I2S");
                    }
                } else {
                    Log.e(TAG, " find USB audio input device, using default");
                    audioInputDevice = findAudioDevice(AudioManager.GET_DEVICES_INPUTS, AudioDeviceInfo.TYPE_USB_DEVICE);
                    if (audioInputDevice == null) {
                        Log.e(TAG, "failed to find I2S audio input device, using Unknow");
                    } else {
                        Log.d(TAG, " find USB audio input device, using USB");
                    }
                    audioOutputDevice = findAudioDevice(AudioManager.GET_DEVICES_OUTPUTS, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
                    if (audioOutputDevice == null) {
                        Log.e(TAG, "failed to found 3.5mm audio output device, using Unknow");
                    } else {
                        Log.d(TAG, " find 3.5mm audio input device, using 3.5mm");
                    }
                }

            }

            try {
                if (USE_VOICEHAT_I2S_DAC) {
                    Log.i(TAG, "initializing DAC trigger");
                    mDac = VoiceHat.openDac();
                    mDac.setSdMode(Max98357A.SD_MODE_SHUTDOWN);

                    mButton = VoiceHat.openButton();
                    mLed = VoiceHat.openLed();
                } else {
//                PeripheralManager pioManager = PeripheralManager.getInstance();
//                mButton = new Button(BoardDefaults.getGPIOForButton(),
//                    Button.LogicState.PRESSED_WHEN_LOW);
//                mLed = pioManager.openGpio(BoardDefaults.getGPIOForLED());
                }

                if (mButton != null) {
                    mButton.setDebounceDelay(BUTTON_DEBOUNCE_DELAY_MS);
                    mButton.setOnButtonEventListener(this);
                }
                if (mLed != null) {
                    mLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                    mLed.setActiveType(Gpio.ACTIVE_HIGH);
                }
            } catch (Exception e) {
                Log.e(TAG, "error configuring peripherals:", e);
                return;
            }

            // Set volume from preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            int initVolume = preferences.getInt(PREF_CURRENT_VOLUME, DEFAULT_VOLUME);
            Log.i(TAG, "setting audio track volume to: " + initVolume);

            UserCredentials userCredentials = null;
            try {
                userCredentials =
                        EmbeddedAssistant.generateCredentials(this, R.raw.credentials);
                progressDialog.setMessage("set Oauth credentials....");
            } catch (IOException | JSONException e) {
                Log.e(TAG, "error getting user credentials", e);
            }
            try {

                progressDialog.setMessage("Embedded Assistant....");
                mEmbeddedAssistant = new EmbeddedAssistant.Builder()
                        .setCredentials(userCredentials)
                        .setDeviceInstanceId(DEVICE_INSTANCE_ID)
                        .setDeviceModelId(DEVICE_MODEL_ID)
                        .setLanguageCode(LANGUAGE_CODE)
                        .setAudioInputDevice(audioInputDevice)
                        .setAudioOutputDevice(audioOutputDevice)
                        .setAudioSampleRate(SAMPLE_RATE)
                        .setAudioVolume(initVolume)
                        .setRequestCallback(new RequestCallback() {
                            @Override
                            public void onRequestStart() {
                                Log.i(TAG, "starting assistant request, enable microphones");
                                mButtonWidget.setText(R.string.button_listening);
                                mButtonWidget.setEnabled(false);
                            }

                            @Override
                            public void onSpeechRecognition(List<SpeechRecognitionResult> results) {
                                for (final SpeechRecognitionResult result : results) {
                                    Log.i(TAG, "assistant request text: " + result.getTranscript() +
                                            " stability: " + Float.toString(result.getStability()));
                                    mAssistantRequestsAdapter.add(result.getTranscript());
                                }
                            }

                            @Override
                            public void onRequestFinish() {
                                super.onRequestFinish();

                            }
                        })
                        .setConversationCallback(new ConversationCallback() {
                            @Override
                            public void onResponseStarted() {
                                super.onResponseStarted();
                                // When bus type is switched, the AudioManager needs to reset the stream volume
                                if (mDac != null) {
                                    try {
                                        mDac.setSdMode(Max98357A.SD_MODE_LEFT);
                                    } catch (IOException e) {
                                        Log.e(TAG, "error enabling DAC", e);
                                    }
                                }
                            }

                            @Override
                            public void onResponseFinished() {
                                super.onResponseFinished();
                                if (mDac != null) {
                                    try {
                                        mDac.setSdMode(Max98357A.SD_MODE_SHUTDOWN);
                                    } catch (IOException e) {
                                        Log.e(TAG, "error disabling DAC", e);
                                    }
                                }
                                if (mLed != null) {
                                    try {
                                        mLed.setValue(false);
                                        LEDShining = false;
                                    } catch (IOException e) {
                                        Log.e(TAG, "cannot turn off LED", e);
                                    }
                                }


                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Log.e(TAG, "assist error: " + throwable.getMessage(), throwable);
                            }

                            @Override
                            public void onVolumeChanged(int percentage) {
                                Log.i(TAG, "assistant volume changed: " + percentage);
                                // Update our shared preferences
                                Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(AssistantActivity.this)
                                        .edit();
                                editor.putInt(PREF_CURRENT_VOLUME, percentage);
                                editor.apply();
                            }

                            @Override
                            public void onConversationFinished() {
                                Log.d(TAG, "sphinx assistant conversation finished");
                                mButtonWidget.setText(R.string.button_new_request);
                                mButtonWidget.setEnabled(true);


                                //the user is done making their request. stop passing data and clean up
                                Log.d(TAG, "sphinx the assistant request finish.");
                                //okay we can activate via keyphrase again
                                captechSphinxManager.startListeningToActivationPhrase();

                            }

                            @Override
                            public void onAssistantResponse(final String response) {
                                if (!response.isEmpty()) {
                                    mMainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAssistantRequestsAdapter.add("Google Assistant: " + response);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onAssistantDisplayOut(final String html) {
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Need to convert to base64
                                        try {
                                            final byte[] data = html.getBytes("UTF-8");
                                            final String base64String =
                                                    Base64.encodeToString(data, Base64.DEFAULT);
                                            mWebView.loadData(base64String, "text/html; charset=utf-8",
                                                    "base64");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                            public void onDeviceAction(String intentName, JSONObject parameters) {

                            }
                        })
                        .build();
                mEmbeddedAssistant.connect();
                mEmbeddedAssistant.setOnPlayMusiceListener(new EmbeddedAssistant.OnPlayMusiceListener() {
                    @Override
                    public void playMusice(String request, float stability) {
                        ToastUtile.showText(AssistantActivity.this,request+" 播放音樂！！");
                        if(dialogFlowInit!=null){
                            dialogFlowInit.setAiRequest(request);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "mEmbeddedAssistant Exception :" + e.getMessage() + "\n" + Utils.FormatStackTrace(e));
                Toast.makeText(this, "Exception :" + e.getMessage(), Toast.LENGTH_LONG).show();

//                    LyonTextToSpeech.speak(context,textToSpeech,e.getMessage());
            }

            dialogFlowInit = new DialogFlowInit(this){
                @Override
                public void DialogFlowSpeech(String speech) {
                    super.DialogFlowSpeech(speech);
                    Log.e(TAG, "dialogFlowInit Conversation speech: " + speech );
                    mAssistantRequestsAdapter.add("DialogFlowInit AIResponse:"+speech);
                    if(!TextUtils.isEmpty(speech))
                        LyonTextToSpeech.speak(context,textToSpeech,speech);
                    if(speech.contains("play") || true){

                        Intent intent = new Intent(AssistantActivity.this, YoutubePlayer.class);
                        Bundle bundle = new Bundle();
                        String videoId="OsUr8N7t4zc";
//                        for(int i=0;i<youtubePosters.size();i++){
//                            videoId=videoId+","+youtubePosters.get(i).getYoutubeId();
//                        }
                        bundle.putString("videoId",videoId);
                        Log.d(TAG,"videoId:"+videoId);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }

                @Override
                public void DialogFlowAction(String action) {
                    super.DialogFlowAction(action);
                    Log.e(TAG, "dialogFlowInit Conversation action: " + action );
                    if(action.equals("recommend")){

                    }
                }
            };

            //instantiate PSphinx
            progressDialog.setMessage("Embedded Sphinx....");
            captechSphinxManager = new CapTechSphinxManager(this, this);
            LEDShining = true;

            // TODO打開一盞燈！
            LEDShining();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(netWork!=null)
            netWork.onResume();
    }

    private AudioDeviceInfo findAudioDevice(int deviceFlag, int deviceType) {
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] adis = manager.getDevices(deviceFlag);
        for (AudioDeviceInfo adi : adis) {
            if (adi.getType() == deviceType) {
                return adi;
            }
        }
        return null;
    }

    @Override
    public void onButtonEvent(Button button, boolean pressed) {
        try {
            if (mLed != null) {
                mLed.setValue(true);
                Log.e(TAG, "mLed == on");
            }
        } catch (Exception e) {
            Log.d(TAG, "error toggling LED:", e);
        }
        if (pressed) {
            LyonTextToSpeech.speak(context,textToSpeech,AISay);
            captechSphinxManager.SpeechRecognizerStop();
            mEmbeddedAssistant.startConversation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroying assistant demo");
        if (mLed != null) {
            try {
                mLed.close();
            } catch (IOException e) {
                Log.w(TAG, "error closing LED", e);
            }
            mLed = null;
        }
        if (mButton != null) {
            try {
                mButton.close();
            } catch (IOException e) {
                Log.w(TAG, "error closing button", e);
            }
            mButton = null;
        }
        if (mDac != null) {
            try {
                mDac.close();
            } catch (IOException e) {
                Log.w(TAG, "error closing voice hat trigger", e);
            } catch (NullPointerException e){
                Log.w(TAG, "error closing voice hat trigger", e);
            }
            mDac = null;
        }
        if(mEmbeddedAssistant!=null)
            mEmbeddedAssistant.destroy();

        //let's clean up.
        if(captechSphinxManager!=null)
            captechSphinxManager.destroy();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech=null;
        }
    }

    //===========Sphinx 喚醒詞=======================================================================
    @Override
    public void onInitializationComplete() {
        Log.d(TAG, "Speech Recognition Ready");
        //讓我們的Sphinx Manager知道我們想要聽短語
        captechSphinxManager.startListeningToActivationPhrase();
        //lets show a blue light to indicate we are ready.
//        playDing(this);
        LEDShining=false;
        if(textToSpeech!=null) {
            LyonTextToSpeech.speak(context, textToSpeech, openComplete);
            ToastUtile.showText(this, openComplete);
        }
        progressDialog.dismiss();
    }

    @Override
    public void onActivationPhraseDetected() {
        // TODO開始我們的助理請求
        Log.d(TAG, "Activation Phrase Detected");
        LyonTextToSpeech.speak(context,textToSpeech,AISay);

        ToastUtile.showText(this,"是的");


        mEmbeddedAssistant.startConversation();
        if (mLed != null) {
            try {
                mLed.setValue(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void LEDShining(){
        Log.e(TAG, "mLed LEDShining");

        if(mLed!=null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=0;
                    while (LEDShining){
                        try {
                            mLed.setValue(true);
                            Log.e(TAG, "mLed == on");
                            Thread.sleep(250);
                            mLed.setValue(false);
                            Log.e(TAG, "mLed == off");
                            Thread.sleep(200);
                        } catch (IOException e) {
                            Log.e(TAG,"Led IOException:"+e);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        i++;
                    }
                }
            }).start();
        }else{
            Log.e(TAG, "mLed == null");
        }
    }




}
