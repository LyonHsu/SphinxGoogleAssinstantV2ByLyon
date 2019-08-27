package com.example.androidthings.assistant.Youtube.Play;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.androidthings.assistant.R;
import com.example.androidthings.assistant.Youtube.YoutubeConstants;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by i_hfuhsu on 2017/7/11.
 */

public class YoutubeFragment extends Fragment {

    String TAG=YoutubeFragment.class.getName();
    private static String VIDEO_ID = "iCgiydq5F3U";
    private static List<String> VIDEO_ID2 ;
    String videoUrl;
    YouTubePlayer.OnInitializedListener onInitializedListener;
    public  boolean isLoop = false;
    YouTubePlayer yooutPlayer;
    private YoutubeFragment.setOnPrivousShowListener setOnPrivousShowListener = null;
    private YoutubeFragment.setOnNextShowListener setOnNextShowListener  =null;
    private YoutubeFragment.setPlayPauseShowListener setPlayPauseShowListener  =null;
    YouTubePlayerSupportFragment youTubePlayerFragment;
    private static final int REQUEST_DIALOG = 101;
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    @Override
    public void setArguments(Bundle bundle) {
        super.setArguments(bundle);
        if(bundle != null){
            VIDEO_ID = bundle.getString("videoId");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle != null){
            VIDEO_ID = bundle.getString("videoId");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            YoutubeInit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.youtube_layout, container, false);
        VIDEO_ID2 = new ArrayList<>();
        try{
            String videoId[]=VIDEO_ID.split(",");
            Log.d(TAG,"videoId :"+videoId.length);
            for(int i=0;i<videoId.length;i++){
                VIDEO_ID2.add(videoId[i]);
                Log.d(TAG,"videoId["+i+"]:"+videoId[i]);
            }
        }catch (Exception e){
            VIDEO_ID2.add(VIDEO_ID);
        }
        Log.d(TAG,"VIDEO_ID2 size:"+VIDEO_ID2.size());

        //get youtube fragment api
        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.youtube_layout, youTubePlayerFragment).commit();

        YoutubeInit();



        return view;
    }

    private void YoutubeInit(){
        try{
            youTubePlayerFragment.initialize(YoutubeConstants.API_KEY, new YouTubePlayer.OnInitializedListener() {

                // YouTubeプレーヤーの初期化成功
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player, boolean wasRestored) {
                    if (!wasRestored) {
                        yooutPlayer=player;
                        player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        player.loadVideos(VIDEO_ID2);
                        player.play();
                        player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                            @Override
                            public void onLoading() {
                                Log.d(TAG,"YouTubePlayer onLoading");
                            }

                            @Override
                            public void onLoaded(String s) {
                                Log.d(TAG,"YouTubePlayer onLoaded");
                            }

                            @Override
                            public void onAdStarted() {
                                Log.d(TAG,"YouTubePlayer onAdStarted");
                            }

                            @Override
                            public void onVideoStarted() {
                                Log.d(TAG,"YouTubePlayer onVideoStarted");

                                if(setOnPrivousShowListener!=null)
                                    setOnPrivousShowListener.isPreviounShow(player.hasPrevious());
                                if(setOnNextShowListener!=null)
                                    setOnNextShowListener.isNextShow(player.hasNext());
                            }

                            @Override
                            public void onVideoEnded() {
                                Log.d(TAG,"YouTubePlayer onVideoEnded player.hasNext():"+player.hasNext());
                                Log.d(TAG,"YouTubePlayer onVideoEnded isLoop:"+isLoop);
                                if (isLoop) {
                                    Log.d(TAG, " loop play");
                                    if(player.hasPrevious()) {
                                        Log.d(TAG, " loop play+" + player.hasPrevious());
                                        player.previous();
                                    }
                                    else{
                                        Log.d(TAG, " loop play+"+player.hasPrevious());
                                        player.play();
                                    }
                                }
                                //                            if(player.hasNext()) {
                                //                                player.next();
                                //                                Log.d(TAG,"play next id:"+VIDEO_ID2.get())
                                //                            }
                                //                            else{
                                //
                                //                            }

                            }

                            @Override
                            public void onError(YouTubePlayer.ErrorReason errorReason) {
                                Log.e(TAG,"YouTubePlayer errorReason:"+errorReason);
                            }
                        });

                        player.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                            @Override
                            public void onPlaying() {
                                if(setPlayPauseShowListener!=null)
                                    setPlayPauseShowListener.isPlayPause(true);
                            }

                            @Override
                            public void onPaused() {
                                if(setPlayPauseShowListener!=null)
                                    setPlayPauseShowListener.isPlayPause(false);
                            }

                            @Override
                            public void onStopped() {
                                if(setPlayPauseShowListener!=null)
                                    setPlayPauseShowListener.isPlayPause(false);
                            }

                            @Override
                            public void onBuffering(boolean b) {

                            }

                            @Override
                            public void onSeekTo(int i) {

                            }
                        });
                    }
                }

                // YouTubeプレーヤーの初期化失敗
                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                    // YouTube error
                    String errorMessage = error.toString();
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    if (error.isUserRecoverableError()) {
                        error.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
                    } else {
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                    Log.e("errorMessage:", errorMessage);

                }


            });
        }catch(Exception e ){
            e.printStackTrace();
        }
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public void setLoop(boolean b){
        this.isLoop=b;
    }

    public boolean getIsLoop(){
        return this.isLoop;
    }


    public void setPlay(){
        if(yooutPlayer!=null){
            yooutPlayer.play();
        }
    }

    public boolean getPlayerStats(){
        if(yooutPlayer!=null){
            return yooutPlayer.isPlaying();
        }
        return false;
    }

    public void setPause(){
        if(yooutPlayer!=null){
            yooutPlayer.pause();
        }
    }

    public void setNext(){
        if(yooutPlayer!=null){
            if(yooutPlayer.hasNext())
                yooutPlayer.next();
        }
    }

    public void setPrevious(){
        if(yooutPlayer!=null){
            if(yooutPlayer.hasPrevious())
                yooutPlayer.previous();
        }
    }

    public static interface setOnPrivousShowListener{
        boolean isPreviounShow(boolean show);
    }

    public static interface setOnNextShowListener{
        boolean isNextShow(boolean show);
    }

    public static interface setPlayPauseShowListener{
        boolean isPlayPause(boolean playing);
    }

    public void setOnNextBtShowListener(setOnNextShowListener listener){
        this.setOnNextShowListener = listener;
    }

    public void setOnPrivousBtnShowListener(setOnPrivousShowListener listener){
        this.setOnPrivousShowListener = listener;
    }

    public void setPlayPauseBtnStatsListener(setPlayPauseShowListener listener){
        this.setPlayPauseShowListener = listener;
    }
}
