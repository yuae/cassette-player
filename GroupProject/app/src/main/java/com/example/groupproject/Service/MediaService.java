package com.example.groupproject.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MediaService extends Service {

    private static final String TAG = "MediaService";
    private MyBinder mBinder = new MyBinder();
    private int index=0;
    private String[] musicPath ;
    //inintialize
    public MediaPlayer mMediaPlayer = new MediaPlayer();


    public MediaService() {
        getFilePath();
        iniMediaPlayerFile(index);
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
                Log.i(TAG,"Playing");
                mMediaPlayer.start();
            }
        }

        /**
         * Pause
         */
        public void pauseMusic() {
            if (mMediaPlayer.isPlaying()) {
                //Pause if playing
                Log.i(TAG,"Pausing");
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
                iniMediaPlayerFile(index);
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
            if(mMediaPlayer!=null){
                if(index<musicPath.length-1){
                    index++;
                }else{
                    index=0;
                    Log.i(TAG,"Last song. Going back to the first");
                }
                mMediaPlayer.reset();
                iniMediaPlayerFile(index);
                playMusic();
            }
        }

        /**
         * Previous song
         */
        public void lastMusic() {
            if(mMediaPlayer!=null){
                if(index>0){
                    index--;
                }else{
                    index= musicPath.length-1;
                    Log.i(TAG,"First song. Going back to the last");
                }
                mMediaPlayer.reset();
                iniMediaPlayerFile(index);
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
    private void iniMediaPlayerFile(int index) {
        //Get file path
        try {
            //Catch IO exception
            //Set file to mediaPlayer
            mMediaPlayer.setDataSource(musicPath[index]);
            //Get mediaPlayer prepare
            mMediaPlayer.prepare();
            Log.i(TAG,"Playing: "+musicPath[index]);
        } catch (IOException e) {
            Log.d(TAG, "IO exception");
            e.printStackTrace();
        }
    }

    private void getFilePath(){
        File musicFolder = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.example.groupproject/files");
//        File musicFolder = new File(Environment.getExternalStorageDirectory()+"/music");
//        File file =getApplicationContext().getExternalFilesDir(null);
//        File musicFolder = new File(file.getAbsolutePath());
        File[] songs= musicFolder.listFiles();
        List<String> fileList = new ArrayList<>();
        for(int i = 0;i<songs.length;i++){
            fileList.add(songs[i].getAbsolutePath());
            Log.i(TAG,fileList.get(i)+"");
        }
        String[] fileArray = new String[fileList.size()];
        fileArray= fileList.toArray(fileArray);
        musicPath=fileArray;
    }

}
