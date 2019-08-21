package com.example.androidthings.assistant.Youtube;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.androidthings.assistant.R;
import com.example.androidthings.assistant.Youtube.Play.YoutubeFragment;


public class YoutubePlayer extends FragmentActivity {
    public String VIDEO_ID = "OsUr8N7t4zc";
    Button previousBtn ,loopBtn ,NextBtn ,PlayPauseBtn;
    private YoutubeFragment.setOnPrivousShowListener setOnPrivousShowListener = null;
    private YoutubeFragment.setOnNextShowListener setOnNextShowListener  =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        RelativeLayout youtubePlayerFragment = (RelativeLayout) findViewById(R.id.youtubePlayerFragment);
        Bundle bundle = getIntent().getExtras();
        final YoutubeFragment fragment = new YoutubeFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.youtubePlayerFragment, fragment)
                .commit();

        fragment.setOnNextBtShowListener(new YoutubeFragment.setOnNextShowListener() {
            @Override
            public boolean isNextShow(boolean show) {
                if(show)
                    NextBtn.setEnabled(true);
                else
                    NextBtn.setEnabled(false);
                return false;
            }
        });

        fragment.setOnPrivousBtnShowListener(new YoutubeFragment.setOnPrivousShowListener() {
            @Override
            public boolean isPreviounShow(boolean show) {
                if(show)
                    previousBtn.setEnabled(true);
                else
                    previousBtn.setEnabled(false);
                return false;
            }
        });

        fragment.setPlayPauseBtnStatsListener(new YoutubeFragment.setPlayPauseShowListener() {
            @Override
            public boolean isPlayPause(boolean playing) {
                if(playing){
                    PlayPauseBtn.setText("playing");
                }else{
                    PlayPauseBtn.setText("pause");
                }

                return false;
            }
        });

        loopBtn= (Button) findViewById(R.id.loopBtn);
        loopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.setLoop(!fragment.getIsLoop());
                if(fragment.getIsLoop()){
                    loopBtn.setText("Loop");
                }else{
                    loopBtn.setText("NoLoop");
                }
            }
        });
        if(fragment.getIsLoop()){
            loopBtn.setText("Loop");
        }else{
            loopBtn.setText("NoLoop");
        }

        previousBtn = (Button)findViewById(R.id.PreviousBtn);
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.setPrevious();
            }
        });
        NextBtn = (Button)findViewById(R.id.NextBtn);
        NextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.setNext();
            }
        });
        PlayPauseBtn = (Button)findViewById(R.id.PlayPauseBtn);
        PlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragment.getPlayerStats())
                    fragment.setPause();
                else
                    fragment.setPlay();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
