package com.example.groupproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    boolean play= false;
    ImageButton lastTrack_button;
    ImageButton play_pause;
    ImageButton nextTrack_button;
    ImageButton b_market;
    Intent MediaServiceIntent;
    ObjectAnimator last_button_anime ;
    ObjectAnimator next_button_anime;
    AnimatorSet animatorSet;
    TextView songTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songTitle = (TextView)findViewById(R.id.song_title_id);
        MediaServiceIntent = new Intent(this, com.example.mediaPlayer.Service.MediaService.class);
        //Get permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        } else {
            //Prepare to play
            bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
        button_rotate_anime();


        //startActivity(HttpServer.getIntent(this));
    }
    //Get permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[]permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                } else {
                    Toast.makeText(this, "Not enough permission, quit", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MediaService.MyBinder) service;
            mSeekBar.setMax(mMyBinder.getProgress());

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Only process if user change the seekBar
                    if(fromUser){
                        mMyBinder.seekToPositon(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mHandler.post(mRunnable);

            Log.d(TAG, "Service connected to activity");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play:
                    mMyBinder.playMusic();
                    break;
                case R.id.pause:
                    mMyBinder.pauseMusic();
                    break;
                case R.id.next_track_id:
                    mMyBinder.nextMusic();
                    next_click();
                    break;
                case R.id.last_track_id:
                    mMyBinder.preciousMusic();
                    lastClick();
                    break;
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mRunnable);

            mMyBinder.closeMedia();
            unbindService(mServiceConnection);
        }
    /**

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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    }**/

    public void QRPage(View view)
    {
        startActivity(HttpServer.getIntent(this));

    }

    //...
    //  ObjectAnimator.ofFloat(lastTrack_button,"rotation",0, 45).start();


}


