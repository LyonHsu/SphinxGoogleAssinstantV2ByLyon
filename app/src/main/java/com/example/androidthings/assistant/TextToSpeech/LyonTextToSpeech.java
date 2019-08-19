package com.example.androidthings.assistant.TextToSpeech;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class LyonTextToSpeech  {
    String TAG = LyonTextToSpeech.class.getSimpleName();
    private static TextToSpeech textToSpeech;
    Context context;

    public LyonTextToSpeech(Context context){
        this.context=context;
        textToSpeech=getTextToSpeech();
    }

    public void speak(String sss){
         textToSpeech =  getTextToSpeech();
        String TAGG = "LyonTextToSpeech "+TAG;
        if(textToSpeech==null)
        {
            Log.e(TAGG,"textToSpeech==null");
            return;
        }
        Log.e(TAGG,"textToSpeech "+sss);
        ArrayList<HashMap<String,String>> arrayList = getEngorChingString(sss);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (i == 0) {
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_FLUSH, null, null);
                } else
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_ADD, null, null);
            }else{
                if (i == 0) {
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_FLUSH, null);
                } else
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_ADD, null);

            }
            if(isSpeak == TextToSpeech.ERROR){
                Log.e(TAGG,"\""+arrayList.get(i).get("word")+"\" speak isSpeak:ERROR");
            }else
                Log.d(TAGG, "\""+arrayList.get(i).get("word")+"\" speak result:" + result+" isSpeak="+isSpeak);
        }
    }

    public TextToSpeech getTextToSpeech(){
        //set text to speech
        if(textToSpeech==null) {
            textToSpeech= new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.d(TAG, "getTextToSpeech TTS init status:" + status);
                    if (status != TextToSpeech.ERROR) {
//                        int result = textToSpeech.setLanguage(Locale.getDefault());//Locale.);
                        int result = textToSpeech.setLanguage(Locale.TAIWAN);

                        Log.d(TAG, "getTextToSpeech speak result init:" + result);
                        init(status);
                    }
                }
            });
        }
        return textToSpeech;
    }

    public ArrayList<HashMap<String,String>> getEngorChingString(String s){
        Log.d(TAG,"20190605 string:"+s);
        HashMap<String,String> hashMap = new HashMap<>();
        ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
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

    public static void pause() {
        textToSpeech.stop();
        textToSpeech.shutdown();
    }
}
