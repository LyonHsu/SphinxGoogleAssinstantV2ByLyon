package com.example.androidthings.assistant.TextToSpeech;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import com.example.androidthings.assistant.AssistantActivity;
import com.example.androidthings.assistant.Tool.ToastUtile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Decoder;

public class LyonTextToSpeech {
    static String TAG = LyonTextToSpeech.class.getSimpleName();


    public static void speak(Context context, TextToSpeech textToSpeech, String sss){
        ToastUtile.showText(context,sss);
        String TAGG = "LyonTextToSpeech "+TAG;
        if(textToSpeech==null)
        {
            Log.e(TAGG,"textToSpeech==null");
            return;
        }
        Log.e(TAGG,"textToSpeech "+sss);
        ArrayList<HashMap<String, String>> arrayList = getEngorChingString(sss);
        for(int i =0;i<arrayList.size();i++){
            int result=-1;
            if(arrayList.get(i).get("isEng").equals("true")){
                result =textToSpeech.setLanguage(Locale.ENGLISH);
                result=2;
            }else{
                result =textToSpeech.setLanguage(Locale.TAIWAN);
                result=1;
            }
            int isSpeak=-2;
            /**
             * TextToSpeech.QUEUE_FLUSH 丢弃之前的播报任务，立即播报本次内容
             * TextToSpeech.QUEUE_ADD 播放完之前的语音任务后才播报本次内容
             * params 设置TTS参数，可以是null。 final HashMap<String, String> params
             * KEY_PARAM_STREAM：音频通道，可以是：STREAM_MUSIC、STREAM_NOTIFICATION、STREAM_RING等
             * KEY_PARAM_VOLUME：音量大小，0-1f
             * 返回值：int SUCCESS = 0，int ERROR = -1。
             */
            final HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_MUSIC));

            AudioManager audioManager =(AudioManager) context.getSystemService(context.AUDIO_SERVICE);

            if (i == 0) {
                isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_FLUSH, params);
            } else
                isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_ADD, params);



            if(isSpeak == TextToSpeech.ERROR){
                Log.e(TAGG,"\""+arrayList.get(i).get("word")+"\" speak isSpeak:ERROR");
            }else
                Log.d(TAGG, "\""+arrayList.get(i).get("word")+"\" speak result:" + result+" isSpeak="+isSpeak);
        }
    }

    private static ArrayList<HashMap<String, String>> getEngorChingString(String s){
        Log.d(TAG,"20190605 string:"+s);
        HashMap<String, String> hashMap = new HashMap<>();
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        char[] c = s.toCharArray();
        Log.d(TAG,"20190605 c size:"+c.length);
        String word="";
        boolean isEng=false;
        boolean isoldEng=false;
        for(int i=0;i<c.length;i++){
            String cc = c[i]+"";

            if( cc.matches("[a-zA-Z|\\.]*") )//a-zA-Z0-9
            {
                isEng=true;
            }
            else
            {
                isEng=false;
            }
            Log.d(TAG,"20190605 c:"+cc+" isEng:"+isEng+ " / "+isoldEng);

            if(isoldEng!=isEng){
                hashMap.put("word",word);
                hashMap.put("isEng",isoldEng+"");
                arrayList.add(hashMap);
                isoldEng=isEng;
                word="";
                hashMap = new HashMap<>();
            }
            word=word+cc;
            Log.d(TAG,"20190605 word:"+word);
        }
        hashMap.put("word",word);
        hashMap.put("isEng",isoldEng+"");
        arrayList.add(hashMap);

        for(int i=0;i<arrayList.size();i++){
            Log.d(TAG,"20190605 arrayList:"+arrayList.get(i).get("word")+" / "+arrayList.get(i).get("isEng"));
        }

        return arrayList;
    }

    public void init(int status){

    }
}
