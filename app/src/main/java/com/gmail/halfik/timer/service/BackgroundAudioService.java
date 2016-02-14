package com.gmail.halfik.timer.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.gmail.halfik.timer.activity.TimerActivity;

public class BackgroundAudioService extends Service implements MediaPlayer.OnCompletionListener
{
    private static final String TAG = "BackgroundAudioService";
    public static final String EXTRA_FILE_PATH = "file_name";

    MediaPlayer mediaPlayer;
    String mFileToPlay;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);



        mediaPlayer.setVolume(1f, 1f);
        mediaPlayer.setLooping(false);
        Log.i(TAG, "Media onCreate: ");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mFileToPlay = intent.getStringExtra(EXTRA_FILE_PATH);
        try{
            AssetFileDescriptor descriptor = getAssets().openFd(TimerActivity.ALARMS_FOLDER + "/" + mFileToPlay);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.i(TAG, "Media onStart: " + TimerActivity.ALARMS_FOLDER + "/" + mFileToPlay);

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        return START_STICKY;
    }

    public void onDestroy() {
        Log.i(TAG, "Media onDestroy");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        Log.i(TAG, "Media onCompletion");
        stopSelf();
    }

}