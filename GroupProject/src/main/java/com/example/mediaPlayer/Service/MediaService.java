package com.example.mediaPlayer.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;



public class MediaService extends Service {

    private static final String TAG = "MediaService";
    private MyBinder mBinder = new MyBinder();
    // No of the song
    private int i = 0;
    //Path
    private String[] musicPath = new String[]{
            Environment.getExternalStorageDirectory() + "/Sounds/a1.mp3",
            Environment.getExternalStorageDirectory() + "/Sounds/a2.mp3",
            Environment.getExternalStorageDirectory() + "/Sounds/a3.mp3",
            Environment.getExternalStorageDirectory() + "/Sounds/a4.mp3"
    };
    //inintialize
    public MediaPlayer mMediaPlayer = new MediaPlayer();


    public MediaService() {
        iniMediaPlayerFile(i);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {


        /**
         * Play music
         */
        public void playMusic() {
            if (!mMediaPlayer.isPlaying()) {
                //Start playing if not playing
                mMediaPlayer.start();
            }
        }

        /**
         * Pause
         */
        public void pauseMusic() {
            if (mMediaPlayer.isPlaying()) {
                //Pause if palying
                mMediaPlayer.pause();
            }
        }

        /**
         * reset
         */
        public void resetMusic() {
            if (!mMediaPlayer.isPlaying()) {
                //Reset if not playing
                mMediaPlayer.reset();
                iniMediaPlayerFile(i);
            }
        }

        /**
         * Close media
         */
        public void closeMedia() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }

        /**
         * Next song
         */
        public void nextMusic() {
            if (mMediaPlayer != null && i < 4 && i >= 0) {
                //reset before switch
                mMediaPlayer.reset();
                iniMediaPlayerFile(i + 1);
                //Keep no in bound
                if (i == 2) {

                } else {
                    i = i + 1;
                }
                playMusic();
            }
        }

        /**
         * Previous song
         */
        public void preciousMusic() {
            if (mMediaPlayer != null && i < 4 && i > 0) {
                mMediaPlayer.reset();
                iniMediaPlayerFile(i - 1);
                //Keep no in bound
                if (i == 1) {

                } else {

                    i = i - 1;
                }
                playMusic();
            }
        }

        /**
         * Get length
         **/
        public int getProgress() {

            return mMediaPlayer.getDuration();
        }

        /**
         * Get current position
         */
        public int getPlayPosition() {

            return mMediaPlayer.getCurrentPosition();
        }
        /**
         * Play desiered position
         */
        public void seekToPositon(int msec) {
            mMediaPlayer.seekTo(msec);
        }




    }


    /**
     * Add file
     */
    private void iniMediaPlayerFile(int dex) {
        //Get file path
        try {
            //Catch IO exception
            //Set file to mediaPlayer
            mMediaPlayer.setDataSource(musicPath[dex]);
            //Get mediaPlayer prepaer
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(TAG, "IO exception");
            e.printStackTrace();
        }
    }
}
