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
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.androidthings.assistant.BlueTooth.BluetoothTool;
import com.example.androidthings.assistant.DialogFlow.DialogFlowInit;
import com.example.androidthings.assistant.EmbeddedAssistant.ConversationCallback;
import com.example.androidthings.assistant.EmbeddedAssistant.RequestCallback;
import com.example.androidthings.assistant.NetWork.NetWork;
import com.example.androidthings.assistant.NetWork.tool.Alert;
import com.example.androidthings.assistant.NetWork.tool.Permission;
import com.example.androidthings.assistant.Sphinx.CapTechSphinxManager;
import com.example.androidthings.assistant.TextToSpeech.LyonTextToSpeech;
import com.example.androidthings.assistant.Tool.Log;
import com.example.androidthings.assistant.Tool.ToastUtile;
import com.example.androidthings.assistant.Youtube.Item;
import com.example.androidthings.assistant.Youtube.Play.YoutubeFragment;
import com.example.androidthings.assistant.Youtube.Search.SearchYoutube;
import com.example.androidthings.assistant.Youtube.YoutubeAdapter;
import com.example.androidthings.assistant.Youtube.YoutubePlayer;
import com.example.androidthings.assistant.Youtube.YoutubePoster;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.voicehat.Max98357A;
import com.google.android.things.contrib.driver.voicehat.VoiceHat;
import com.google.android.things.pio.Gpio;
import com.google.assistant.embedded.v1alpha2.AssistConfig;
import com.google.assistant.embedded.v1alpha2.AssistResponse;
import com.google.assistant.embedded.v1alpha2.AudioOutConfig;
import com.google.assistant.embedded.v1alpha2.DeviceConfig;
import com.google.assistant.embedded.v1alpha2.SpeechRecognitionResult;
import com.google.auth.oauth2.UserCredentials;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.androidthings.assistant.Tool.Utils;

import static com.example.androidthings.assistant.Sphinx.CapTechSphinxManager.ACTIVATION_KEYPHRASE;


public class AssistantActivity extends AppCompatActivity implements Button.OnButtonEventListener, CapTechSphinxManager.SphinxListener {
    private static final String TAG = AssistantActivity.class.getSimpleName();

    //Is Use Google AIY Device
    public static boolean isGoogleAIY = false;


    // Peripheral and drivers constants.
    private static final int BUTTON_DEBOUNCE_DELAY_MS = 20;
    // Default on using the Voice Hat on Raspberry Pi 3.
    public static final boolean USE_VOICEHAT_I2S_DAC = Build.DEVICE.equals(BoardDefaults.DEVICE_RPI3);

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


    Handler mMainHandler;
    Context context;
    ProgressDialog progressDialog;
    TextToSpeech textToSpeech;
    DialogFlowInit dialogFlowInit;
    String AISay="Yes";
    String openComplete = "開機完畢 你可以使用 " + ACTIVATION_KEYPHRASE + " 來喚醒";


    public final int NOTIFYCHANGE=2;
    List<YoutubePoster> youtubePosters;
    String nexttoken;
    LinearLayoutManager mLayoutManager;
    GridLayoutManager gridLayoutManager;
    YoutubeAdapter mAdapter;
    RecyclerView mRecyclerView;
    YoutubeFragment youtubeFragment;

    boolean isSpecialRequest = false;

    BluetoothTool bluetoothTool;
    final int OPENBLUETOOTH = 0;
    final int REQUEST_ENABLE_BT = 100;
    private static final int REQUEST_CODE = 2; // 请求码
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private android.widget.Button blueToothBtn;

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



                        int result = textToSpeech.setLanguage(Locale.getDefault());
                        HashMap myHash = new HashMap<String, String>();
                        myHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                                String.valueOf(AudioManager.MODE_NORMAL));
//                        textToSpeech.speak(openComplete, TextToSpeech.QUEUE_FLUSH, myHash);
                        Log.d(TAG, "getTextToSpeech speak result init:" + result);


                    }else{
                        Log.e(TAG, "getTextToSpeech TTS init Error:" + status);
                        ToastUtile.showText(context,"getTextToSpeech TTS init Error:" + status);
                    }
                }
            });


            //設定音量
            try {
                int systemName = AudioManager.STREAM_SYSTEM;
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

                /**
                 *MODE_NORMAL : 普通模式，既不是鈴聲模式也不是通話模式
                 * MODE_RINGTONE : 鈴聲模式
                 * MODE_IN_CALL : 通話模式
                 * MODE_IN_COMMUNICATION : 通訊模式，包括音/視訊,VoIP通話.(3.0加入的，與通話模式類似)
                 */
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.setSpeakerphoneOn(true);


                int maVolume = audioManager.getStreamMaxVolume(systemName) / 2;
                audioManager.setStreamVolume(systemName, maVolume, AudioManager.FLAG_SHOW_UI);
                systemName = AudioManager.STREAM_MUSIC;//STREAM_RING
                maVolume = audioManager.getStreamMaxVolume(systemName);
                audioManager.setStreamVolume(systemName, maVolume, AudioManager.FLAG_VIBRATE);
                systemName = AudioManager.STREAM_RING;//STREAM_RING
                maVolume = audioManager.getStreamMaxVolume(systemName);
                audioManager.setStreamVolume(systemName, maVolume, AudioManager.FLAG_VIBRATE);
                ToastUtile.showText(context, "設定音量為：" + maVolume);
                Log.e(TAG, "設定音量為：" + maVolume);
            }catch (Exception e){
                Log.e(TAG,Utils.FormatStackTrace(e));
            }


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

            netWork.setOnWifiStatusListener(new NetWork.OnWifiStatusListener() {
                @Override
                public void wifiStatue(NetworkInfo.DetailedState status) {
                    Log.i(TAG, " onReceive: intent action wifiStatue:" + status);
                    if(status.equals(NetworkInfo.DetailedState.CONNECTED)) {
                        String ipp =  "no connect wifi!";
                        String ip =ipp;
                        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInf = wifiMan.getConnectionInfo();
                        int ipAddress = wifiInf.getIpAddress();
                        ip= String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
                        String IP=""+ip;
                        String ssID = wifiInf.getSSID();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("IP", IP);
                            jsonObject.put("SSID", ssID);
                            blueToothWrite(jsonObject);
                        }catch (JSONException e){
                            Log.e(TAG,"");
                        }
                    }else if(status.equals(NetworkInfo.DetailedState.SCANNING) ||
                            status.equals(NetworkInfo.DetailedState.DISCONNECTING) ||
                            status.equals(NetworkInfo.DetailedState.FAILED) ||
                            status.equals(NetworkInfo.DetailedState.BLOCKED) ||
                            status.equals(NetworkInfo.DetailedState.DISCONNECTED)
                    ){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("IP", "DISCONNECTED");
                            blueToothWrite(jsonObject);
                        }catch (JSONException e){
                            Log.e(TAG,"");
                        }
                    }else if(status.equals(NetworkInfo.DetailedState.CONNECTING) ||
                            status.equals(NetworkInfo.DetailedState.AUTHENTICATING) ||
                            status.equals(NetworkInfo.DetailedState.OBTAINING_IPADDR)
                    ){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("IP", "CONNECTING");
                            blueToothWrite(jsonObject);
                        }catch (JSONException e){
                            Log.e(TAG,"");
                        }
                    }else if(status.equals(NetworkInfo.DetailedState.SUSPENDED)){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("IP", "SUSPENDED");
                            blueToothWrite(jsonObject);
                        }catch (JSONException e){
                            Log.e(TAG,"");
                        }
                    }
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
                Log.e(TAG, "error configuring peripherals:"+ e);
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
                Log.e(TAG, "error getting user credentials"+ e);
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
                                        Log.e(TAG, "error enabling DAC:"+ e);
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
                                        Log.e(TAG, "error disabling DAC:"+ e);
                                    }
                                }
                                if (mLed != null) {
                                    try {
                                        mLed.setValue(false);
                                        LEDShining = false;
                                    } catch (IOException e) {
                                        Log.e(TAG, "cannot turn off LED:"+ e);
                                    }
                                }


                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Log.e(TAG, "assist error: " + throwable.getMessage()+" "+ throwable);
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
                    public void playMusice(AssistResponse assistResponse, String request, float stability) {
//                        ToastUtile.showText(AssistantActivity.this,request+" 播放音樂！！");
                        if(dialogFlowInit!=null){
                            dialogFlowInit.setAiRequest(assistResponse, request);
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

                }

                @Override
                public void DialogFlowAction(AssistResponse assistResponse, JSONObject jsonObject) {
                    super.DialogFlowAction(assistResponse, jsonObject);

                    if(jsonObject!=null) {
                        String action = jsonObject.optString("action");
                        if (!TextUtils.isEmpty(action)) {
                            if (action.equals("play_music")) {
                                isSpecialRequest = true;
                                String artist = jsonObject.optString("artist");
                                String song = jsonObject.optString("any");
                                Log.e(TAG, "DialogFlowAction: 播放:" + artist + " 歌曲:" + song);
                                Toast.makeText(AssistantActivity.this, "播放:" + artist + " 歌曲:" + song, Toast.LENGTH_LONG).show();
                                searchYoutube(artist + " " + song);
                            }else if(action.equals("stop_music")){
                                isSpecialRequest = true;
                                youtubeFragment.setPause();
                                Toast.makeText(AssistantActivity.this, "播放暫停" , Toast.LENGTH_LONG).show();
                            }else if(action.equals("next_music")){
                                isSpecialRequest = true;
                                youtubeFragment.setNext();
                                Toast.makeText(AssistantActivity.this, "播放下一首" , Toast.LENGTH_LONG).show();
                            }else if(action.equals("previous_music")){
                                isSpecialRequest = true;
                                youtubeFragment.setPrevious();
                                Toast.makeText(AssistantActivity.this, "播放上一首" , Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    if(isSpecialRequest){
                        Log.e(TAG,"====== stop Assistant Request ======");
                        mEmbeddedAssistant.stopConversation(isSpecialRequest);
                    }else {
                        Log.e(TAG,"====== callAssistantResponse ======");
                        mEmbeddedAssistant.callAssistantResponse(assistResponse, isSpecialRequest);
                    }
                }
            };

            //youbute
            youtubePosters = new ArrayList<>();

            mAdapter = new YoutubeAdapter(youtubePosters);
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclesView);
            mRecyclerView.setAdapter(mAdapter);
            mLayoutManager = new LinearLayoutManager(this);
            mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            gridLayoutManager = new GridLayoutManager(this,6);

            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setHasFixedSize(true);

            //instantiate PSphinx
            progressDialog.setMessage("Embedded Sphinx....");
            captechSphinxManager = new CapTechSphinxManager(this, this);
            LEDShining = true;

            // TODO打開一盞燈！
            LEDShining();
        }

        BlueToothInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(netWork!=null)
            netWork.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d(TAG, "BluetoothTool Enable requestCode:" + requestCode);
        }
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                Toast.makeText(this, "Permission Denieddd by user.Please Check it in Settings", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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
            Log.d(TAG, "error toggling LED:"+ e);
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
                Log.w(TAG, "error closing LED:"+ e);
            }
            mLed = null;
        }
        if (mButton != null) {
            try {
                mButton.close();
            } catch (IOException e) {
                Log.w(TAG, "error closing button:"+ e);
            }
            mButton = null;
        }
        if (mDac != null) {
            try {
                mDac.close();
            } catch (IOException e) {
                Log.w(TAG, "error closing voice hat trigger:"+ e);
            } catch (NullPointerException e){
                Log.w(TAG, "error closing voice hat trigger:"+ e);
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

        bluetoothTool.onDestroy();
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
//            LyonTextToSpeech.speak(context, textToSpeech, openComplete);
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

        if(mEmbeddedAssistant!=null)
            mEmbeddedAssistant.startConversation();
        if (mLed != null) {
            try {
                mLed.setValue(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOTIFYCHANGE:
                    Log.e(TAG,"Youtube NOTIFYCHANGE");
                    mAdapter.setNotifyDataSetChanged(youtubePosters);
                    playYoutube(0);
                    break;
            }
        }
    };

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


    private Item toItem(int Type , String sss){
        Item itme = new Item();
        itme.Type=Type;
        itme.sss=sss;
        return itme;
    }


    private void searchYoutube(String keyWord){
        new SearchYoutube(keyWord){
            public void YoutubePosters(List<YoutubePoster> posters){
                Log.d(TAG,"YoutubeAdapter searchBtn onClick: YoutubePosters size:"+posters.size());
                youtubePosters = posters;

                Message msg = mHandler.obtainMessage();
                msg.what=NOTIFYCHANGE;
                msg.obj=youtubePosters;
                mHandler.sendMessage(msg);
            }

            @Override
            public void getNextPageToken(String NextPageToken) {
                nexttoken=NextPageToken;
            }

            @Override
            public void getPrevPageToken(String PextPageToken) {

            }
        }.execute();
    }

    private void playYoutube(int position){
        Log.e(TAG,"Youtube playYoutube() ");
        YoutubePoster youtubePoster = youtubePosters.get(position);
        Log.d(TAG,"Youtube playYoutube YoutubePoster:"+youtubePoster.getTitle());
        String videoId=youtubePoster.getYoutubeId();
        for(int i=0;i<youtubePosters.size();i++){
            videoId=videoId+","+youtubePosters.get(i).getYoutubeId();
        }

        RelativeLayout youtubePlayerFragment = (RelativeLayout) findViewById(R.id.youtubePlayerFragment);
        youtubePlayerFragment.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putString("videoId",videoId);
        Log.d(TAG,"videoId:"+videoId);
        youtubeFragment = new YoutubeFragment();
        youtubeFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.youtubePlayerFragment, youtubeFragment)
                .commit();

        youtubeFragment.setPlayPauseBtnStatsListener(new YoutubeFragment.setPlayPauseShowListener() {
            @Override
            public boolean isPlayPause(boolean playing) {
//                if(playing){
//                    PlayPauseBtn.setText("playing");
//                }else{
//                    PlayPauseBtn.setText("pause");
//                }

                return false;
            }
        });
    }

    public void BlueToothInit(){
        bluetoothTool = new BluetoothTool(this) {
            @Override
            public void getBluetoothDeviceName(HashMap<String, String> bluetoothDeviceName, BluetoothDevice device) {

                int i = 0;
                for (Map.Entry<String, String> entry : bluetoothDeviceName.entrySet()) {
                    Log.d(TAG,""+"[" + i + "]:" + entry.getValue() + ", " + entry.getKey());
                    i++;
                }
            }
            @Override
            public void openBluetoothTime(int time) {
                Log.e(TAG, "openBluetoothTime:" + time);
                Message message = new Message();
                message.obj = time;
                message.what = OPENBLUETOOTH;
                handler.sendMessage(message);

            }

            @Override
            public void startBT(Intent intent) {
                Log.d(TAG, "startBT intent:" + intent.getAction());
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }

            @Override
            public void reSearchOldBluetoothdevice() {
                super.reSearchOldBluetoothdevice();
            }
        };
        String bluetoothType = bluetoothTool.getBlueToothType(bluetoothTool.getBluetoothClass());
        String blueDate = "bluetooth Name:" + bluetoothTool.getBluetoothName("Lyon Smart Box Pi3_" + Build.MODEL) + ",   Mac:" + bluetoothTool.getBluetoothMac();
        bluetoothTool.findBuletoothDevice();
        bluetoothTool.openBlueTooth();

        blueToothBtn = findViewById(R.id.blueToothBtn);
        blueToothBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothTool!=null) {
                    bluetoothTool.openBlueTooth();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case OPENBLUETOOTH:
                    Log.d(TAG, "handler openBluetoothTime:" + message.obj + "s");
                    break;
            }
        }
    };

    public void blueToothWrite(JSONObject jsonObject){
        if(bluetoothTool!=null){
            bluetoothTool.bluetoothWrite(jsonObject);
        }
    }

}
