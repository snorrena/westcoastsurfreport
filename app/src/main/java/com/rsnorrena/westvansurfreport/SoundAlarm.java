package com.rsnorrena.westvansurfreport;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Admin on 2016-05-17.
 */
public class SoundAlarm implements TextToSpeech.OnInitListener{

    private static final String TAG = SoundAlarm.class.getSimpleName();
    private TinyDB tinydb;
    private MediaPlayer mp;
    private TextToSpeech tts;
    private Context context;


    public SoundAlarm(){
        this.context = MainActivity.context;
        tinydb = new TinyDB(context);
    }

    public void soundAlarmOn(){
        tinydb.putBoolean("alarmtriggered", true);
        tts = new TextToSpeech(context, this);
        //code for sounding of the audio alarm
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
//the sound file is type ogg including a meta tag for android looping because the set looping method does not seem to work
        mp = MediaPlayer.create(context, R.raw.splashsound);
        mp.setLooping(false);// set to true for loop
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
        });
    }

    public void alarmOff(){
        //stops and shuts down the texttospeech object if it exists
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (mp != null){
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;

        }
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.CANADA);

            int surfgrade = tinydb.getInt("surfgrade");
            String surfreport;

            surfreport = "The current report for surf potential in West Vancouver is  " + String.valueOf(surfgrade) + " percent.";
            Log.d(TAG, "TTS called");

            tts.speak(surfreport, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
