package com.example.groupproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupproject.Service.MediaService;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean play = false;
    ImageButton lastTrack_button;
    ImageButton play_pause_button;
    ImageButton nextTrack_button;
    ImageButton b_market;
    ObjectAnimator last_button_anime;
    ObjectAnimator next_button_anime;
    AnimatorSet animatorSet;
    TextView songTitle;
    private MediaService.MyBinder mMyBinder;
    Intent MediaServiceIntent;
    private static final String TAG = "MyActivity";
    private Handler mHandler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songTitle = (TextView) findViewById(R.id.song_title_id);
        MediaServiceIntent = new Intent(this, MediaService.class);


        button_rotate_anime();


        //startActivity(HttpServer.getIntent(this));

        //Get permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        } else {
            //Prepare to play
            bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            Log.i(TAG, "Service Binded");
        }
    }


    public void button_rotate_anime() {
        lastTrack_button = (ImageButton) findViewById(R.id.last_track_id);
        last_button_anime = ObjectAnimator.ofFloat(lastTrack_button, "rotation", 0, 360);
        lastTrack_button.setOnClickListener(this);

        nextTrack_button = (ImageButton) findViewById(R.id.next_track_id);
        next_button_anime = ObjectAnimator.ofFloat(nextTrack_button, "rotation", 0, 360);
        nextTrack_button.setOnClickListener(this);

        play_pause_button = (ImageButton) findViewById(R.id.pause_play);
        play_pause_button.setOnClickListener(this);

        next_button_anime.setRepeatCount(Animation.INFINITE);
        last_button_anime.setRepeatCount(Animation.INFINITE);


        animatorSet = new AnimatorSet();
        animatorSet.setDuration(2000);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(last_button_anime, next_button_anime);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause_play:
                if (!play) {
                    animatorSet.start();
                    mMyBinder.playMusic();
                    play = true;
                    TextView title = (TextView) findViewById(R.id.song_title_id);
                    title.setText(mMyBinder.getSongName());
                } else {
                    animatorSet.pause();
                    mMyBinder.pauseMusic();
                    play = false;
                    TextView title = (TextView) findViewById(R.id.song_title_id);
                    title.setText(mMyBinder.getSongName());
                }
                break;
            case R.id.next_track_id:
                animatorSet.cancel();
                animatorSet.setStartDelay(200);
                animatorSet.start();
                Log.i(TAG,"Switching next");
                mMyBinder.nextMusic();
                songTitle.setText(mMyBinder.getSongName());
                break;
            case R.id.last_track_id:
                animatorSet.cancel();
                animatorSet.setStartDelay(200);
                animatorSet.start();
                Log.i(TAG,"Switching last");
                mMyBinder.lastMusic();
                songTitle.setText(mMyBinder.getSongName());
                break;
        }
    }

    public void QRPage(View view) {
        startActivity(HttpServer.getIntent(this));

    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MediaService.MyBinder) service;

            mHandler.post(mRunnable);

            Log.d(TAG, "Service connected to activity");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //Get permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);

        mMyBinder.closeMedia();
        unbindService(mServiceConnection);
    }


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mMyBinder.getCompletion()){
                animatorSet.pause();
            }
            mHandler.postDelayed(mRunnable, 500);
        }
    };


}


