package com.example.groupproject;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startActivity(HttpServer.getIntent(this));
    }

    public void lastClick(View view)
    {
        ImageButton lastTrack_button = (ImageButton)findViewById(R.id.last_track_id);
        ObjectAnimator last_button_anime =ObjectAnimator.ofFloat(lastTrack_button,"rotation",0, 360);
        last_button_anime.setRepeatCount(Animation.INFINITE);

        ImageButton next_track_button = (ImageButton)findViewById(R.id.next_track_id);
        ObjectAnimator next_button_anime = ObjectAnimator.ofFloat(next_track_button,"rotation",0, 360);

        next_button_anime.setRepeatCount(Animation.INFINITE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(2000);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(last_button_anime, next_button_anime);
        animatorSet.start();







    }



    public void pause_play(View view)
    {


    }

    public void QRPage(View view)
    {
        startActivity(HttpServer.getIntent(this));

    }

    //..
    //  ObjectAnimator.ofFloat(lastTrack_button,"rotation",0, 45).start();


}


