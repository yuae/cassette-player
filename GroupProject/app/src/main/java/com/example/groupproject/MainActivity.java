package com.example.groupproject;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    boolean play= false;
    ImageButton lastTrack_button;
    ImageButton play_pause;
    ImageButton nextTrack_button;
    ImageButton b_market;
    ObjectAnimator last_button_anime ;
    ObjectAnimator next_button_anime;
    AnimatorSet animatorSet;
    TextView songTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songTitle = (TextView)findViewById(R.id.song_title_id);

        button_rotate_anime();


        //startActivity(HttpServer.getIntent(this));
    }

    public void lastClick(View view)
    {
        //animatorSet.pause();
        //animatorSet.setDuration(2000);
        animatorSet.cancel();
        animatorSet.setStartDelay(200);
        animatorSet.start();
        String last_title ="";
        songTitle.setText("last");
        //Log.d("clicked,")



    }


    public void button_rotate_anime()
    {
        lastTrack_button = (ImageButton)findViewById(R.id.last_track_id);
        last_button_anime =ObjectAnimator.ofFloat(lastTrack_button,"rotation",0, 360);

        nextTrack_button = (ImageButton)findViewById(R.id.next_track_id);
        next_button_anime = ObjectAnimator.ofFloat(nextTrack_button,"rotation",0, 360);

        next_button_anime.setRepeatCount(Animation.INFINITE);
        last_button_anime.setRepeatCount(Animation.INFINITE);


        animatorSet = new AnimatorSet();
        animatorSet.setDuration(2000);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(last_button_anime, next_button_anime);

    }
    public void pause_play(View view)
    {
        if(!play)
        {
            animatorSet.start();

        }else
        {

            animatorSet.pause();

        }

    }

    public void next_click(View view)
    {
        animatorSet.cancel();
        animatorSet.setStartDelay(200);
        animatorSet.start();

    }

    public void QRPage(View view)
    {
        startActivity(HttpServer.getIntent(this));

    }

    //...
    //  ObjectAnimator.ofFloat(lastTrack_button,"rotation",0, 45).start();


}


