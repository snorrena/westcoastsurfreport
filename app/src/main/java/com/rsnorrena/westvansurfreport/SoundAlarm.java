package com.rsnorrena.westvansurfreport;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SoundAlarm {

    private static final String TAG = SoundAlarm.class.getSimpleName();
    private TinyDB tinydb;
    private MediaPlayer mp;
    private TextToSpeech tts;
    private Context context;

    public SoundAlarm() {
        this.context = MainActivity.context;
        tinydb = new TinyDB(context);
    }

    public void soundAlarmOn() {

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.CANADA);

                    int surfGrade = tinydb.getInt("surfgrade");

                    String surfreport = "The current report for surf potential in West Vancouver is  " + String.valueOf(surfGrade) + " percent.";
                    Log.d(TAG, "TTS called");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(surfreport, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        tts.speak(surfreport, TextToSpeech.QUEUE_FLUSH, null);
                    }

                }
            }
        });
        //code for sounding of the audio alarm
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
//the sound file is type ogg including a meta tag for android looping because the set looping method does not seem to work
        mp = MediaPlayer.create(context, R.raw.splashsound);
        mp.setLooping(true);// set to true for loop
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.reset();
                mp.release();
            }
        });
    }

    public void alarmOff() {
        //stops and shuts down the texttospeech object if it exists
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;

        }
    }
}
