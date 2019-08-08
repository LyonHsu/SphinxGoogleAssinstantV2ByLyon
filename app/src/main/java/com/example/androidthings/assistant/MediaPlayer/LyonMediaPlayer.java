package com.example.androidthings.assistant.MediaPlayer;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import com.example.androidthings.assistant.AssistantActivity;
import com.example.androidthings.assistant.R;

public class LyonMediaPlayer  {

    //MediaPlayer物件
    private static MediaPlayer mediaPlayer;
    //音樂播放索引(播到哪一首)
    private int index = 0;
    //是否為暫停狀態
    private boolean isPause;


    public static void playDing(Context context){
//        try {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer = MediaPlayer.create(context, R.raw.ding);
//            AudioDeviceInfo audioInputDevice = findAudioDevice(context, AudioManager.GET_DEVICES_INPUTS, AssistantActivity.InputDeviceType);
//            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
//                mediaPlayer.setPreferredDevice(audioInputDevice);
//            mediaPlayer.start();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
    }

    public static void playDong(Context context){
//        try {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer = MediaPlayer.create(context, R.raw.dong);
//            AudioDeviceInfo audioInputDevice = findAudioDevice(context, AudioManager.GET_DEVICES_INPUTS, AssistantActivity.InputDeviceType);
//            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
//                mediaPlayer.setPreferredDevice(audioInputDevice);
//            mediaPlayer.start();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
    }

    public static AudioDeviceInfo  findAudioDevice(Context context, int deviceFlag, int deviceType) {
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
