package com.rsnorrena.westvansurfreport;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.rsnorrena.westvansurfreport.parsers.JsoupWebScrape;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    //initialization of class variables (highlighted purple)
    private ToggleButton tb;
    private Button wf, cam, stop;

    //tag to be used in logs
    private static final String TAG = MainActivity.class.getSimpleName();

    private String date;
    private String time;
    private String winddirection;
    private String windspeed;
    private String waveheight;
    private String waveinterval;
    private String winddirectiondegrees;

    private String winddirectionletters = "";
    private String numerictime = "";
    private String numericwindspeed = "";
    private Float numericwaveheight = 0.0f;
    private int recordssaved;

    //Declaration of textfields in the main screen layout
    private TextView tvd1, tvd2, tvd3, tvd5, tvd6, tvd7, tvd8, tvd9, tvd10, tvd11, tvd12, tvd13, tvd14, tvd16;
    private TextView tve1, tve2, tve3, tve5, tve6, tve7, tve8, tve9, tve10, tve11, tve12, tve13, tve14, tve16;
    private TextView tvf1, tvf2, tvf3, tvf5, tvf6, tvf7, tvf8, tvf9, tvf10, tvf11, tvf12, tvf13, tvf14, tvf16;
    private TextView tvg1, tvg2, tvg3, tvg5, tvg6, tvg7, tvg8, tvg9, tvg10, tvg11, tvg12, tvg13, tvg14, tvg16;
    private TextView tvh1, tvh2, tvh3, tvh5, tvh6, tvh7, tvh8, tvh9, tvh10, tvh11, tvh12, tvh13, tvh14, tvh16;
    private TextView tvi1, tvi2, tvi3, tvi5, tvi6, tvi7, tvi8, tvi9, tvi10, tvi11, tvi12, tvi13, tvi14, tvi16;
    private TextView alertwindwarning;

    private TinyDB tinydb;
    //    private boolean blink;
    private static boolean activityVisible, serviceStarted, blink;

    public static Context context;
    //the public static context should be avaiable to all classes in the application

    //intent to be used for set and run of the alarm monitoring service
    private AlarmManager manager;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;

    public static SoundAlarm soundAlarm;

    @Override//send user to the setup screen on press of the menu button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                Intent myIntent = new Intent(MainActivity.this, SetUp.class);
                MainActivity.this.startActivity(myIntent);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_table);
        //sets the display as per the layout data_table

        MainActivity.context = getApplicationContext();
        //sets the application context to the variable "context".

        //instantiates the buttons on the display
        tb = (ToggleButton) findViewById(R.id.toggleButton);
        wf = (Button) findViewById(R.id.bwindforecast);
        cam = (Button) findViewById(R.id.bcam);
        stop = (Button) findViewById(R.id.bcleardata);

        //sets the alpha on the buttons to show through to the background image
        wf.getBackground().setAlpha(64);
        cam.getBackground().setAlpha(64);
        stop.getBackground().setAlpha(64);
        tb.getBackground().setAlpha(64);

        tb.setOnClickListener(new View.OnClickListener() {//onclick listener to start/stop the monitoring service
            @Override
            public void onClick(View v) {
                if (tb.isChecked()) {
                    tinydb.putBoolean("alarm", true);
                } else {
                    if (soundAlarm != null) {
                        soundAlarm.alarmOff();
                    }
                    tinydb.putBoolean("alarm", false);
                }
                Log.d("toggle button boolean", String.valueOf(tinydb.getBoolean("alarm")));
            }
        });

        wf.setOnClickListener(new View.OnClickListener() {//onclick listener for the wind forecdast button
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, WindForecast.class);
                MainActivity.this.startActivity(myIntent);
            }
        });//call the start the windforecast activity on click of the button

        cam.setOnClickListener(new View.OnClickListener() {//onclick listener for the web cam button
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, WebCam.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {//onclick listener for the clear data button

            @Override
            public void onClick(View v) {
                //extra dialog to confirm choice to clear data
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE://remove records saved in app prefs and update display

                                tinydb.remove("windforecast");
                                tinydb.putInt("surfgrade", 0);
                                tinydb.putInt("recordssaved", 0);
                                tinydb.remove("saveddatarecord1");
                                tinydb.remove("saveddatarecord2");
                                tinydb.remove("saveddatarecord3");
                                tinydb.remove("saveddatarecord4");
                                tinydb.remove("saveddatarecord5");
                                tinydb.remove("saveddatarecord6");
                                tinydb.remove("alarmtriggered");
                                tinydb.remove("lastRecordSavedDateAndTime");

//                                tinydb.remove("batterySaverCheck");//only query on first run

                                stopTheAndroidAlarmMonitor();

                                finish();


                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.YourDialogStyle);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

            }
        });


//instantiation of the textviews on the main display
        tvd1 = (TextView) findViewById(R.id.tvd1);
        tvd2 = (TextView) findViewById(R.id.tvd2);
        tvd3 = (TextView) findViewById(R.id.tvd3);
        tvd5 = (TextView) findViewById(R.id.tvd5);
        tvd6 = (TextView) findViewById(R.id.tvd6);
        tvd7 = (TextView) findViewById(R.id.tvd7);
        tvd8 = (TextView) findViewById(R.id.tvd8);
        tvd9 = (TextView) findViewById(R.id.tvd9);
        tvd10 = (TextView) findViewById(R.id.tvd10);
        tvd11 = (TextView) findViewById(R.id.tvd11);
        tvd12 = (TextView) findViewById(R.id.tvd12);
        tvd13 = (TextView) findViewById(R.id.tvd13);
        tvd14 = (TextView) findViewById(R.id.tvd14);
        tvd16 = (TextView) findViewById(R.id.tvd16);

        tve1 = (TextView) findViewById(R.id.tve1);
        tve2 = (TextView) findViewById(R.id.tve2);
        tve3 = (TextView) findViewById(R.id.tve3);
        tve5 = (TextView) findViewById(R.id.tve5);
        tve6 = (TextView) findViewById(R.id.tve6);
        tve7 = (TextView) findViewById(R.id.tve7);
        tve8 = (TextView) findViewById(R.id.tve8);
        tve9 = (TextView) findViewById(R.id.tve9);
        tve10 = (TextView) findViewById(R.id.tve10);
        tve11 = (TextView) findViewById(R.id.tve11);
        tve12 = (TextView) findViewById(R.id.tve12);
        tve13 = (TextView) findViewById(R.id.tve13);
        tve14 = (TextView) findViewById(R.id.tve14);
        tve16 = (TextView) findViewById(R.id.tve16);

        tvf1 = (TextView) findViewById(R.id.tvf1);
        tvf2 = (TextView) findViewById(R.id.tvf2);
        tvf3 = (TextView) findViewById(R.id.tvf3);
        tvf5 = (TextView) findViewById(R.id.tvf5);
        tvf6 = (TextView) findViewById(R.id.tvf6);
        tvf7 = (TextView) findViewById(R.id.tvf7);
        tvf8 = (TextView) findViewById(R.id.tvf8);
        tvf9 = (TextView) findViewById(R.id.tvf9);
        tvf10 = (TextView) findViewById(R.id.tvf10);
        tvf11 = (TextView) findViewById(R.id.tvf11);
        tvf12 = (TextView) findViewById(R.id.tvf12);
        tvf13 = (TextView) findViewById(R.id.tvf13);
        tvf14 = (TextView) findViewById(R.id.tvf14);
        tvf16 = (TextView) findViewById(R.id.tvf16);

        tvg1 = (TextView) findViewById(R.id.tvg1);
        tvg2 = (TextView) findViewById(R.id.tvg2);
        tvg3 = (TextView) findViewById(R.id.tvg3);
        tvg5 = (TextView) findViewById(R.id.tvg5);
        tvg6 = (TextView) findViewById(R.id.tvg6);
        tvg7 = (TextView) findViewById(R.id.tvg7);
        tvg8 = (TextView) findViewById(R.id.tvg8);
        tvg9 = (TextView) findViewById(R.id.tvg9);
        tvg10 = (TextView) findViewById(R.id.tvg10);
        tvg11 = (TextView) findViewById(R.id.tvg11);
        tvg12 = (TextView) findViewById(R.id.tvg12);
        tvg13 = (TextView) findViewById(R.id.tvg13);
        tvg14 = (TextView) findViewById(R.id.tvg14);
        tvg16 = (TextView) findViewById(R.id.tvg16);

        tvh1 = (TextView) findViewById(R.id.tvh1);
        tvh2 = (TextView) findViewById(R.id.tvh2);
        tvh3 = (TextView) findViewById(R.id.tvh3);
        tvh5 = (TextView) findViewById(R.id.tvh5);
        tvh6 = (TextView) findViewById(R.id.tvh6);
        tvh7 = (TextView) findViewById(R.id.tvh7);
        tvh8 = (TextView) findViewById(R.id.tvh8);
        tvh9 = (TextView) findViewById(R.id.tvh9);
        tvh10 = (TextView) findViewById(R.id.tvh10);
        tvh11 = (TextView) findViewById(R.id.tvh11);
        tvh12 = (TextView) findViewById(R.id.tvh12);
        tvh13 = (TextView) findViewById(R.id.tvh13);
        tvh14 = (TextView) findViewById(R.id.tvh14);
        tvh16 = (TextView) findViewById(R.id.tvh16);

        tvi1 = (TextView) findViewById(R.id.tvi1);
        tvi2 = (TextView) findViewById(R.id.tvi2);
        tvi3 = (TextView) findViewById(R.id.tvi3);
        tvi5 = (TextView) findViewById(R.id.tvi5);
        tvi6 = (TextView) findViewById(R.id.tvi6);
        tvi7 = (TextView) findViewById(R.id.tvi7);
        tvi8 = (TextView) findViewById(R.id.tvi8);
        tvi9 = (TextView) findViewById(R.id.tvi9);
        tvi10 = (TextView) findViewById(R.id.tvi10);
        tvi11 = (TextView) findViewById(R.id.tvi11);
        tvi12 = (TextView) findViewById(R.id.tvi12);
        tvi13 = (TextView) findViewById(R.id.tvi13);
        tvi14 = (TextView) findViewById(R.id.tvi14);
        tvi16 = (TextView) findViewById(R.id.tvi16);

        alertwindwarning = (TextView) findViewById(R.id.tvwindwarning);


//call to update the display if there is at least one record saved in the preferences database
        tinydb = new TinyDB(context);
        try {
            recordssaved = tinydb.getInt("recordssaved");
        } catch (Exception e) {
            recordssaved = 0;
            e.printStackTrace();
        }
        Log.d(TAG, String.valueOf(recordssaved));
        if (recordssaved > 0) {
            updateDisplay();
        }
        toggleButtonReset();

        startMonitor();//set and alarm if there is one already scheduled.

    }//end of onCreate

    public static void passSoundAlarmObject(SoundAlarm alarm) {
        soundAlarm = alarm;
    }

    private void checkForBatterySaver() {
        tinydb.putBoolean("batterySaverCheck", true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(context.getPackageName())) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                startActivity(new Intent(Settings.ACTION_SETTINGS));

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.YourDialogStyle);
                builder.setMessage("*** Important *** \n\nBattery optimization of this application should be manually deactivated in system settings to allow for continuous background monitoring.\n\nSettings - Battery - Battery Optimization - All apps - West Van Surf Report - Don't optimize\n\nProceed to system settings?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
            }
        }
    }

    private void stopTheAndroidAlarmMonitor() {
        //cancels the Android alarm
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//initialize the alarm service
        alarmIntent = new Intent("xyz.abc.ALARMUP");//intent identifier is coded in the android manifest file.
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
        serviceStarted = false;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static void activityStopped() {
        activityVisible = false;
    }

    public static void activityDestroyed() {
        activityVisible = false;
    }

    //new thread to run code to check for a new record added. Code runs once per minute
    private void updateDisplayService() {
        if (!serviceStarted) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    serviceStarted = true;
                    Log.d(TAG, "The update display service is started.");
                    try {
                        while (isActivityVisible()) {//loop to update the display if a new data record is added by the monitoring service
                            boolean x = tinydb.getBoolean("newrecordadded");
                            if (x) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        tinydb.putBoolean("newrecordadded", false);
                                        updateDisplay();//runs the update display method on the main thread if a new data record has been added

                                    }
                                });
                            }

                            Thread.sleep(3000);
                        }
                        Log.d(TAG, "Display service loop terminated.");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            }).start();
        } else {
            Log.d(TAG, "The update display service is already running");
        }

    }

    private void WindWarningCheck() {
        ArrayList<String> windforecast = new ArrayList<String>();
        int size = 0;
        try {
            windforecast = tinydb.getList("windforecast");
            size = windforecast.size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (size > 0) {
            String title2 = windforecast.get(1);
            boolean xyz = title2.contains("WARNING");

            if (xyz) {
                String dtitle2 = title2.replaceAll(", Strait of Georgia - north of Nanaimo", "");
                alertwindwarning.setText(dtitle2);
                blink();


            } else {
                alertwindwarning.setText("There are no wind warnings in effect.");
            }
        }
    }

    private void SurfPotentialPercentage() {
        int sg = 0;
        try {
            sg = tinydb.getInt("surfgrade");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sg > 0) {
            String westvansurfpercent = "The surf potential is " + String.valueOf(sg) + " %";
            Toast toast = Toast.makeText(context, westvansurfpercent, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        MainActivity.activityDestroyed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        MainActivity.activityResumed();
        startMonitor();
        updateDisplayService();
        updateDisplay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
        MainActivity.activityStopped();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart called");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        MainActivity.activityPaused();
    }

    public void startMonitor() {

        boolean androidAlarmSet = (pendingIntent.getBroadcast(MainActivity.this, 0, new Intent("xyz.abc.ALARMUP"), PendingIntent.FLAG_NO_CREATE) != null);

        if (!androidAlarmSet) {
            startTheAlarmMonitor();
        } else {
        }
    }

    private void startTheAlarmMonitor() {
        alarmIntent = new Intent("xyz.abc.ALARMUP");//intent identifier is coded in the android manifest file.
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.SECOND, 10);
//        cal.roll(Calendar.MINUTE, 5);//use the check how the app works in doze mode.

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String alarmTime = sdf.format(cal.getTime());
        Log.d("Alarm set: ", alarmTime);

        //sets the android system alarm to run the onRecieve method in the AlarmReceiver class every ten minutes
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//initialize the alarm service

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        }
    }

    public void updateDisplay() {


        Log.d(TAG, "update display called");

        toggleButtonReset();

        if (!tinydb.getBoolean("batterySaverCheck")) {
            checkForBatterySaver();//function to check if doze mode is set from the app in android os >= Marshmallo
        }

        Date currentDateAndTime = new Date(System.currentTimeMillis());

        //retrieve the date time of the last saved record and compare to current time.
        Date lastRecordSavedDate = null;
        try {
            Long dateInLong = tinydb.getLong("lastRecordSavedDateAndTime");
            lastRecordSavedDate = new Date(dateInLong);
            Log.d(TAG, "Last record saved date: " + lastRecordSavedDate.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "No calendar records saved as of yet");
            lastRecordSavedDate = currentDateAndTime;
        }

        long diffInMillies = currentDateAndTime.getTime() - lastRecordSavedDate.getTime();
        long hourDiff = TimeUnit.MILLISECONDS.toHours(diffInMillies);
        Log.d(TAG, "Time span between records: " + hourDiff + " hours");
        //if the time span between the last saved record and the current time is greater than 1 hours reset all data.
        if (hourDiff > 1) {
            tinydb.remove("windforecast");
            tinydb.putInt("surfgrade", 0);
            tinydb.putInt("recordssaved", 0);
            tinydb.remove("saveddatarecord1");
            tinydb.remove("saveddatarecord2");
            tinydb.remove("saveddatarecord3");
            tinydb.remove("saveddatarecord4");
            tinydb.remove("saveddatarecord5");
            tinydb.remove("saveddatarecord6");
            tinydb.remove("alarmtriggered");
            tinydb.remove("lastRecordSavedDateAndTime");

            //refresh Halibut Bank data from the web
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    tinydb.putBoolean("webScrapeComplete", false);
                    JsoupWebScrape webScrape = new JsoupWebScrape(context);
                    webScrape.scrapeHalibutBankData();
                }
            });
            t.start();
        }

        while (!tinydb.getBoolean("webScrapeComplete")) {//pause until the webscrape is complete before updating the display.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SurfPotentialPercentage();

        if (!blink) {
            setWindWarningVisible();
        }

        WindWarningCheck();

//clear of all text fields. Not sure why typeface is reset to normal
        tvd1.setText("");
        tvd1.setTypeface(null, Typeface.NORMAL);
        tvd2.setText("");
        tvd2.setTypeface(null, Typeface.NORMAL);
        tvd5.setText("");
        tvd5.setTypeface(null, Typeface.NORMAL);
        tvd6.setText("");
        tvd6.setTypeface(null, Typeface.NORMAL);
        tvd7.setText("");
        tvd7.setTypeface(null, Typeface.NORMAL);
        tvd8.setText("");
        tvd8.setTypeface(null, Typeface.NORMAL);
        tvd9.setText("");
        tvd9.setTypeface(null, Typeface.NORMAL);
        tvd10.setText("");
        tvd10.setTypeface(null, Typeface.NORMAL);
        tvd11.setText("");
        tvd11.setTypeface(null, Typeface.NORMAL);
        tvd12.setText("");
        tvd12.setTypeface(null, Typeface.NORMAL);
        tvd13.setText("");
        tvd13.setTypeface(null, Typeface.NORMAL);
        tvd14.setText("");
        tvd14.setTypeface(null, Typeface.NORMAL);
        tvd16.setText("");
        tvd16.setTypeface(null, Typeface.NORMAL);

        tve1.setText("");
        tve1.setTypeface(null, Typeface.NORMAL);
        tve2.setText("");
        tve2.setTypeface(null, Typeface.NORMAL);
        tve5.setText("");
        tve5.setTypeface(null, Typeface.NORMAL);
        tve6.setText("");
        tve6.setTypeface(null, Typeface.NORMAL);
        tve7.setText("");
        tve7.setTypeface(null, Typeface.NORMAL);
        tve8.setText("");
        tve8.setTypeface(null, Typeface.NORMAL);
        tve9.setText("");
        tve9.setTypeface(null, Typeface.NORMAL);
        tve10.setText("");
        tve10.setTypeface(null, Typeface.NORMAL);
        tve11.setText("");
        tve11.setTypeface(null, Typeface.NORMAL);
        tve12.setText("");
        tve12.setTypeface(null, Typeface.NORMAL);
        tve13.setText("");
        tve13.setTypeface(null, Typeface.NORMAL);
        tve14.setText("");
        tve14.setTypeface(null, Typeface.NORMAL);
        tve16.setText("");
        tve16.setTypeface(null, Typeface.NORMAL);

        tvf1.setText("");
        tvf1.setTypeface(null, Typeface.NORMAL);
        tvf2.setText("");
        tvf2.setTypeface(null, Typeface.NORMAL);
        tvf5.setText("");
        tvf5.setTypeface(null, Typeface.NORMAL);
        tvf6.setText("");
        tvf6.setTypeface(null, Typeface.NORMAL);
        tvf7.setText("");
        tvf7.setTypeface(null, Typeface.NORMAL);
        tvf8.setText("");
        tvf8.setTypeface(null, Typeface.NORMAL);
        tvf9.setText("");
        tvf9.setTypeface(null, Typeface.NORMAL);
        tvf10.setText("");
        tvf10.setTypeface(null, Typeface.NORMAL);
        tvf11.setText("");
        tvf11.setTypeface(null, Typeface.NORMAL);
        tvf12.setText("");
        tvf12.setTypeface(null, Typeface.NORMAL);
        tvf13.setText("");
        tvf13.setTypeface(null, Typeface.NORMAL);
        tvf14.setText("");
        tvf14.setTypeface(null, Typeface.NORMAL);
        tvf16.setText("");
        tvf16.setTypeface(null, Typeface.NORMAL);

        tvg1.setText("");
        tvg1.setTypeface(null, Typeface.NORMAL);
        tvg2.setText("");
        tvg2.setTypeface(null, Typeface.NORMAL);
        tvg5.setText("");
        tvg5.setTypeface(null, Typeface.NORMAL);
        tvg6.setText("");
        tvg6.setTypeface(null, Typeface.NORMAL);
        tvg7.setText("");
        tvg7.setTypeface(null, Typeface.NORMAL);
        tvg8.setText("");
        tvg8.setTypeface(null, Typeface.NORMAL);
        tvg9.setText("");
        tvg9.setTypeface(null, Typeface.NORMAL);
        tvg10.setText("");
        tvg10.setTypeface(null, Typeface.NORMAL);
        tvg11.setText("");
        tvg11.setTypeface(null, Typeface.NORMAL);
        tvg12.setText("");
        tvg12.setTypeface(null, Typeface.NORMAL);
        tvg13.setText("");
        tvg13.setTypeface(null, Typeface.NORMAL);
        tvg14.setText("");
        tvg14.setTypeface(null, Typeface.NORMAL);
        tvg16.setText("");
        tvg16.setTypeface(null, Typeface.NORMAL);

        tvh1.setText("");
        tvh1.setTypeface(null, Typeface.NORMAL);
        tvh2.setText("");
        tvh2.setTypeface(null, Typeface.NORMAL);
        tvh5.setText("");
        tvh5.setTypeface(null, Typeface.NORMAL);
        tvh6.setText("");
        tvh6.setTypeface(null, Typeface.NORMAL);
        tvh7.setText("");
        tvh7.setTypeface(null, Typeface.NORMAL);
        tvh8.setText("");
        tvh8.setTypeface(null, Typeface.NORMAL);
        tvh9.setText("");
        tvh9.setTypeface(null, Typeface.NORMAL);
        tvh10.setText("");
        tvh10.setTypeface(null, Typeface.NORMAL);
        tvh11.setText("");
        tvh11.setTypeface(null, Typeface.NORMAL);
        tvh12.setText("");
        tvh12.setTypeface(null, Typeface.NORMAL);
        tvh13.setText("");
        tvh13.setTypeface(null, Typeface.NORMAL);
        tvh14.setText("");
        tvh14.setTypeface(null, Typeface.NORMAL);
        tvh16.setText("");
        tvh16.setTypeface(null, Typeface.NORMAL);

        tvi1.setText("");
        tvi1.setTypeface(null, Typeface.NORMAL);
        tvi2.setText("");
        tvi2.setTypeface(null, Typeface.NORMAL);
        tvi5.setText("");
        tvi5.setTypeface(null, Typeface.NORMAL);
        tvi6.setText("");
        tvi6.setTypeface(null, Typeface.NORMAL);
        tvi7.setText("");
        tvi7.setTypeface(null, Typeface.NORMAL);
        tvi8.setText("");
        tvi8.setTypeface(null, Typeface.NORMAL);
        tvi9.setText("");
        tvi9.setTypeface(null, Typeface.NORMAL);
        tvi10.setText("");
        tvi10.setTypeface(null, Typeface.NORMAL);
        tvi11.setText("");
        tvi11.setTypeface(null, Typeface.NORMAL);
        tvi12.setText("");
        tvi12.setTypeface(null, Typeface.NORMAL);
        tvi13.setText("");
        tvi13.setTypeface(null, Typeface.NORMAL);
        tvi14.setText("");
        tvi14.setTypeface(null, Typeface.NORMAL);
        tvi16.setText("");
        tvi16.setTypeface(null, Typeface.NORMAL);


        int recordssaved = tinydb.getInt("recordssaved");

//start of code to loop through records saved and output to the display screen
        int i = 0;
        boolean gooddata = true;

        while (gooddata) {

            if (i == 1) {
                String a = "saveddatarecord1";

                List<String> x = PullDataFile(a);

                winddirectionletters = x.get(0);
                numerictime = x.get(1);
                String y = x.get(3);
                numericwaveheight = tryParseFloat(y);
                numericwindspeed = x.get(2);
                winddirectiondegrees = x.get(4);


                tvd1.setText(winddirectionletters);
                tvd2.setText(winddirectiondegrees);
                tvd16.setText(numerictime + ", ");

                if (numericwaveheight < 1) {//<1
                    tvd14.setTypeface(null, Typeface.BOLD);
                    tvd14.setText(numericwindspeed);// <1

                } else if (numericwaveheight >= 1 && numericwaveheight < 1.5) {//1
                    tvd13.setTypeface(null, Typeface.BOLD);
                    tvd13.setText(numericwindspeed);
                    tvd14.setText("|");

                } else if (numericwaveheight >= 1.5 && numericwaveheight < 2) {//1.5
                    tvd12.setTypeface(null, Typeface.BOLD);
                    tvd12.setText(numericwindspeed);
                    tvd13.setText("|");
                    tvd14.setText("|");

                } else if (numericwaveheight >= 2 && numericwaveheight < 2.5) {//2
                    tvd11.setTypeface(null, Typeface.BOLD);
                    tvd11.setText(numericwindspeed);
                    tvd12.setText("|");
                    tvd13.setText("|");
                    tvd14.setText("|");

                } else if (numericwaveheight >= 2.5 && numericwaveheight < 3) {//2.5
                    tvd10.setTypeface(null, Typeface.BOLD);
                    tvd10.setText(numericwindspeed);
                    tvd11.setText("|");
                    tvd12.setText("|");
                    tvd13.setText("|");
                    tvd14.setText("|");

                } else if (numericwaveheight >= 3 && numericwaveheight < 3.5) {//3
                    tvd9.setTypeface(null, Typeface.BOLD);
                    tvd9.setText(numericwindspeed);
                    tvd10.setText("|");
                    tvd11.setText("|");
                    tvd12.setText("|");
                    tvd13.setText("|");
                    tvd14.setText("|");

                } else if (numericwaveheight >= 3.5 && numericwaveheight < 4) {//3.5
                    tvd8.setTypeface(null, Typeface.BOLD);
                    tvd8.setText(numericwindspeed);
                    tvd9.setText("|");
                    tvd10.setText("|");
                    tvd11.setText("|");
                    tvd12.setText("|");
                    tvd13.setText("|");
                    tvd14.setText("|");


                } else if (numericwaveheight >= 4 && numericwaveheight < 4.5) {//4
                    tvd7.setTypeface(null, Typeface.BOLD);
                    tvd7.setText(numericwindspeed);
                    tvd8.setText("|");
                    tvd9.setText("|");
                    tvd10.setText("|");
                    tvd11.setText("|");
                    tvd12.setText("|");
                    tvd13.setText("|");
                    tvd14.setText("|");

                } else if (numericwaveheight >= 4.5 && numericwaveheight < 5) {//4.5
                    tvd6.setTypeface(null, Typeface.BOLD);
                    tvd6.setText(numericwindspeed);
                    tvd7.setText("|");
                    tvd8.setText("|");
                    tvd9.setText("|");
                    tvd10.setText("|");
                    tvd11.setText("|");
                    tvd12.setText("|");
                    tvd13.setText("|");
                    tvd14.setText("|");

                } else {//5
                    tvd5.setTypeface(null, Typeface.BOLD);
                    tvd5.setText(numericwindspeed);
                    tvd6.setText("|");
                    tvd7.setText("|");
                    tvd8.setText("|");
                    tvd9.setText("|");
                    tvd10.setText("|");
                    tvd11.setText("|");
                    tvd12.setText("|");
                    tvd13.setText("|");
                    tvd14.setText("|");

                }


            }

            if (i == 2) {

                String a = "saveddatarecord2";

                List<String> x = PullDataFile(a);


                winddirectionletters = x.get(0);
                numerictime = x.get(1);
                String y = x.get(3);
                numericwaveheight = tryParseFloat(y);
                numericwindspeed = x.get(2);
                winddirectiondegrees = x.get(4);

                tve1.setText(winddirectionletters);
                tve2.setText(winddirectiondegrees);
                tve16.setText(numerictime + ", ");

                if (numericwaveheight < 1) {// <1
                    tve14.setTypeface(null, Typeface.BOLD);
                    tve14.setText(numericwindspeed);

                } else if (numericwaveheight >= 1 && numericwaveheight < 1.5) {//1
                    tve13.setTypeface(null, Typeface.BOLD);
                    tve13.setText(numericwindspeed);
                    tve14.setText("|");

                } else if (numericwaveheight >= 1.5 && numericwaveheight < 2) {//1.5
                    tve12.setTypeface(null, Typeface.BOLD);
                    tve12.setText(numericwindspeed);
                    tve13.setText("|");
                    tve14.setText("|");

                } else if (numericwaveheight >= 2 && numericwaveheight < 2.5) {//2
                    tve11.setTypeface(null, Typeface.BOLD);
                    tve11.setText(numericwindspeed);
                    tve12.setText("|");
                    tve13.setText("|");
                    tve14.setText("|");

                } else if (numericwaveheight >= 2.5 && numericwaveheight < 3) {//2.5
                    tve10.setTypeface(null, Typeface.BOLD);
                    tve10.setText(numericwindspeed);
                    tve11.setText("|");
                    tve12.setText("|");
                    tve13.setText("|");
                    tve14.setText("|");

                } else if (numericwaveheight >= 3 && numericwaveheight < 3.5) {//3
                    tve9.setTypeface(null, Typeface.BOLD);
                    tve9.setText(numericwindspeed);
                    tve10.setText("|");
                    tve11.setText("|");
                    tve12.setText("|");
                    tve13.setText("|");
                    tve14.setText("|");

                } else if (numericwaveheight >= 3.5 && numericwaveheight < 4) {//3.5
                    tve8.setTypeface(null, Typeface.BOLD);
                    tve8.setText(numericwindspeed);
                    tve9.setText("|");
                    tve10.setText("|");
                    tve11.setText("|");
                    tve12.setText("|");
                    tve13.setText("|");
                    tve14.setText("|");

                } else if (numericwaveheight >= 4 && numericwaveheight < 4.5) {//4
                    tve7.setTypeface(null, Typeface.BOLD);
                    tve7.setText(numericwindspeed);
                    tve8.setText("|");
                    tve9.setText("|");
                    tve10.setText("|");
                    tve11.setText("|");
                    tve12.setText("|");
                    tve13.setText("|");
                    tve14.setText("|");


                } else if (numericwaveheight >= 4.5 && numericwaveheight < 5) {//4.5
                    tve6.setTypeface(null, Typeface.BOLD);
                    tve6.setText(numericwindspeed);
                    tve7.setText("|");
                    tve8.setText("|");
                    tve9.setText("|");
                    tve10.setText("|");
                    tve11.setText("|");
                    tve12.setText("|");
                    tve13.setText("|");
                    tve14.setText("|");

                } else {//5
                    tve5.setTypeface(null, Typeface.BOLD);
                    tve5.setText(numericwindspeed);
                    tve6.setText("|");
                    tve7.setText("|");
                    tve8.setText("|");
                    tve9.setText("|");
                    tve10.setText("|");
                    tve11.setText("|");
                    tve12.setText("|");
                    tve13.setText("|");
                    tve14.setText("|");

                }

            }

            if (i == 3) {

                String a = "saveddatarecord3";

                List<String> x = PullDataFile(a);

                winddirectionletters = x.get(0);
                numerictime = x.get(1);
                String y = x.get(3);
                numericwaveheight = tryParseFloat(y);
                numericwindspeed = x.get(2);
                winddirectiondegrees = x.get(4);

                tvf1.setText(winddirectionletters);
                tvf2.setText(winddirectiondegrees);
                tvf16.setText(numerictime + ", ");

                if (numericwaveheight < 1) {
                    tvf14.setTypeface(null, Typeface.BOLD);
                    tvf14.setText(numericwindspeed);// <1

                } else if (numericwaveheight >= 1 && numericwaveheight < 1.5) {//1
                    tvf13.setTypeface(null, Typeface.BOLD);
                    tvf13.setText(numericwindspeed);
                    tvf14.setText("|");

                } else if (numericwaveheight >= 1.5 && numericwaveheight < 2) {//1.5
                    tvf12.setTypeface(null, Typeface.BOLD);
                    tvf12.setText(numericwindspeed);
                    tvf13.setText("|");
                    tvf14.setText("|");

                } else if (numericwaveheight >= 2 && numericwaveheight < 2.5) {//2
                    tvf11.setTypeface(null, Typeface.BOLD);
                    tvf11.setText(numericwindspeed);
                    tvf12.setText("|");
                    tvf13.setText("|");
                    tvf14.setText("|");

                } else if (numericwaveheight >= 2.5 && numericwaveheight < 3) {//2.5
                    tvf10.setTypeface(null, Typeface.BOLD);
                    tvf10.setText(numericwindspeed);
                    tvf11.setText("|");
                    tvf12.setText("|");
                    tvf13.setText("|");
                    tvf14.setText("|");

                } else if (numericwaveheight >= 3 && numericwaveheight < 3.5) {//3
                    tvf9.setTypeface(null, Typeface.BOLD);
                    tvf9.setText(numericwindspeed);
                    tvf10.setText("|");
                    tvf11.setText("|");
                    tvf12.setText("|");
                    tvf13.setText("|");
                    tvf14.setText("|");

                } else if (numericwaveheight >= 3.5 && numericwaveheight < 4) {//3.5
                    tvf8.setTypeface(null, Typeface.BOLD);
                    tvf8.setText(numericwindspeed);
                    tvf9.setText("|");
                    tvf10.setText("|");
                    tvf11.setText("|");
                    tvf12.setText("|");
                    tvf13.setText("|");
                    tvf14.setText("|");

                } else if (numericwaveheight >= 4 && numericwaveheight < 4.5) {//4
                    tvf7.setTypeface(null, Typeface.BOLD);
                    tvf7.setText(numericwindspeed);
                    tvf8.setText("|");
                    tvf9.setText("|");
                    tvf10.setText("|");
                    tvf11.setText("|");
                    tvf12.setText("|");
                    tvf13.setText("|");
                    tvf14.setText("|");

                } else if (numericwaveheight >= 4.5 && numericwaveheight < 5) {//4.5
                    tvf6.setTypeface(null, Typeface.BOLD);
                    tvf6.setText(numericwindspeed);
                    tvf7.setText("|");
                    tvf8.setText("|");
                    tvf9.setText("|");
                    tvf10.setText("|");
                    tvf11.setText("|");
                    tvf12.setText("|");
                    tvf13.setText("|");
                    tvf14.setText("|");

                } else {//5
                    tvf5.setTypeface(null, Typeface.BOLD);
                    tvf5.setText(numericwindspeed);
                    tvf6.setText("|");
                    tvf7.setText("|");
                    tvf8.setText("|");
                    tvf9.setText("|");
                    tvf10.setText("|");
                    tvf11.setText("|");
                    tvf12.setText("|");
                    tvf13.setText("|");
                    tvf14.setText("|");

                }

            }

            if (i == 4) {

                String a = "saveddatarecord4";

                List<String> x = PullDataFile(a);

                winddirectionletters = x.get(0);
                numerictime = x.get(1);
                String y = x.get(3);
                numericwaveheight = tryParseFloat(y);
                numericwindspeed = x.get(2);
                winddirectiondegrees = x.get(4);

                tvg1.setText(winddirectionletters);
                tvg2.setText(winddirectiondegrees);
                tvg16.setText(numerictime + ", ");

                if (numericwaveheight < 1) {
                    tvg14.setTypeface(null, Typeface.BOLD);
                    tvg14.setText(numericwindspeed);// <1

                } else if (numericwaveheight >= 1 && numericwaveheight < 1.5) {//1
                    tvg13.setTypeface(null, Typeface.BOLD);
                    tvg13.setText(numericwindspeed);
                    tvg14.setText("|");

                } else if (numericwaveheight >= 1.5 && numericwaveheight < 2) {//1.5
                    tvg12.setTypeface(null, Typeface.BOLD);
                    tvg12.setText(numericwindspeed);
                    tvg13.setText("|");
                    tvg14.setText("|");

                } else if (numericwaveheight >= 2 && numericwaveheight < 2.5) {//2
                    tvg11.setTypeface(null, Typeface.BOLD);
                    tvg11.setText(numericwindspeed);
                    tvg12.setText("|");
                    tvg13.setText("|");
                    tvg14.setText("|");

                } else if (numericwaveheight >= 2.5 && numericwaveheight < 3) {//2.5
                    tvg10.setTypeface(null, Typeface.BOLD);
                    tvg10.setText(numericwindspeed);
                    tvg11.setText("|");
                    tvg12.setText("|");
                    tvg13.setText("|");
                    tvg14.setText("|");

                } else if (numericwaveheight >= 3 && numericwaveheight < 3.5) {//3
                    tvg9.setTypeface(null, Typeface.BOLD);
                    tvg9.setText(numericwindspeed);
                    tvg10.setText("|");
                    tvg11.setText("|");
                    tvg12.setText("|");
                    tvg13.setText("|");
                    tvg14.setText("|");

                } else if (numericwaveheight >= 3.5 && numericwaveheight < 4) {//3.5
                    tvg8.setTypeface(null, Typeface.BOLD);
                    tvg8.setText(numericwindspeed);
                    tvg9.setText("|");
                    tvg10.setText("|");
                    tvg11.setText("|");
                    tvg12.setText("|");
                    tvg13.setText("|");
                    tvg14.setText("|");

                } else if (numericwaveheight >= 4 && numericwaveheight < 4.5) {//4
                    tvg7.setTypeface(null, Typeface.BOLD);
                    tvg7.setText(numericwindspeed);
                    tvg8.setText("|");
                    tvg9.setText("|");
                    tvg10.setText("|");
                    tvg11.setText("|");
                    tvg12.setText("|");
                    tvg13.setText("|");
                    tvg14.setText("|");

                } else if (numericwaveheight >= 4.5 && numericwaveheight < 5) {//4.5
                    tvg6.setTypeface(null, Typeface.BOLD);
                    tvg6.setText(numericwindspeed);
                    tvg7.setText("|");
                    tvg8.setText("|");
                    tvg9.setText("|");
                    tvg10.setText("|");
                    tvg11.setText("|");
                    tvg12.setText("|");
                    tvg13.setText("|");
                    tvg14.setText("|");

                } else {//5
                    tvg5.setTypeface(null, Typeface.BOLD);
                    tvg5.setText(numericwindspeed);
                    tvg6.setText("|");
                    tvg7.setText("|");
                    tvg8.setText("|");
                    tvg9.setText("|");
                    tvg10.setText("|");
                    tvg11.setText("|");
                    tvg12.setText("|");
                    tvg13.setText("|");
                    tvg14.setText("|");

                }

            }

            if (i == 5) {

                String a = "saveddatarecord5";

                List<String> x = PullDataFile(a);

                winddirectionletters = x.get(0);
                numerictime = x.get(1);
                String y = x.get(3);
                numericwaveheight = tryParseFloat(y);
                numericwindspeed = x.get(2);
                winddirectiondegrees = x.get(4);

                tvh1.setText(winddirectionletters);
                tvh2.setText(winddirectiondegrees);
                tvh16.setText(numerictime + ", ");

                if (numericwaveheight < 1) {//<1
                    tvh14.setTypeface(null, Typeface.BOLD);
                    tvh14.setText(numericwindspeed);

                } else if (numericwaveheight >= 1 && numericwaveheight < 1.5) {//1
                    tvh13.setTypeface(null, Typeface.BOLD);
                    tvh13.setText(numericwindspeed);
                    tvh14.setText("|");

                } else if (numericwaveheight >= 1.5 && numericwaveheight < 2) {//1.5
                    tvh12.setTypeface(null, Typeface.BOLD);
                    tvh12.setText(numericwindspeed);
                    tvh13.setText("|");
                    tvh14.setText("|");

                } else if (numericwaveheight >= 2 && numericwaveheight < 2.5) {//2
                    tvh11.setTypeface(null, Typeface.BOLD);
                    tvh11.setText(numericwindspeed);
                    tvh12.setText("|");
                    tvh13.setText("|");
                    tvh14.setText("|");

                } else if (numericwaveheight >= 2.5 && numericwaveheight < 3) {//2.5
                    tvh10.setTypeface(null, Typeface.BOLD);
                    tvh10.setText(numericwindspeed);
                    tvh11.setText("|");
                    tvh12.setText("|");
                    tvh13.setText("|");
                    tvh14.setText("|");

                } else if (numericwaveheight >= 3 && numericwaveheight < 3.5) {//3
                    tvh9.setTypeface(null, Typeface.BOLD);
                    tvh9.setText(numericwindspeed);
                    tvh10.setText("|");
                    tvh11.setText("|");
                    tvh12.setText("|");
                    tvh13.setText("|");
                    tvh14.setText("|");

                } else if (numericwaveheight >= 3.5 && numericwaveheight < 4) {//3.5
                    tvh8.setTypeface(null, Typeface.BOLD);
                    tvh8.setText(numericwindspeed);
                    tvh9.setText("|");
                    tvh10.setText("|");
                    tvh11.setText("|");
                    tvh12.setText("|");
                    tvh13.setText("|");
                    tvh14.setText("|");

                } else if (numericwaveheight >= 4 && numericwaveheight < 4.5) {//4
                    tvh7.setTypeface(null, Typeface.BOLD);
                    tvh7.setText(numericwindspeed);
                    tvh8.setText("|");
                    tvh9.setText("|");
                    tvh10.setText("|");
                    tvh11.setText("|");
                    tvh12.setText("|");
                    tvh13.setText("|");
                    tvh14.setText("|");

                } else if (numericwaveheight >= 4.5 && numericwaveheight < 5) {//4.5
                    tvh6.setTypeface(null, Typeface.BOLD);
                    tvh6.setText(numericwindspeed);
                    tvh7.setText("|");
                    tvh8.setText("|");
                    tvh9.setText("|");
                    tvh10.setText("|");
                    tvh11.setText("|");
                    tvh12.setText("|");
                    tvh13.setText("|");
                    tvh14.setText("|");

                } else {//5
                    tvh5.setTypeface(null, Typeface.BOLD);
                    tvh5.setText(numericwindspeed);
                    tvh6.setText("|");
                    tvh7.setText("|");
                    tvh8.setText("|");
                    tvh9.setText("|");
                    tvh10.setText("|");
                    tvh11.setText("|");
                    tvh12.setText("|");
                    tvh13.setText("|");
                    tvh14.setText("|");

                }

            }

            if (i == 6) {


                String a = "saveddatarecord6";

                List<String> x = PullDataFile(a);

                winddirectionletters = x.get(0);
                numerictime = x.get(1);
                String y = x.get(3);
                numericwaveheight = tryParseFloat(y);
                numericwindspeed = x.get(2);
                winddirectiondegrees = x.get(4);

                tvi1.setText(winddirectionletters);
                tvi2.setText(winddirectiondegrees);
                tvi16.setText(numerictime);

                if (numericwaveheight < 1) {//<1
                    tvi14.setTypeface(null, Typeface.BOLD);
                    tvi14.setText(numericwindspeed);

                } else if (numericwaveheight >= 1 && numericwaveheight < 1.5) {//1
                    tvi13.setTypeface(null, Typeface.BOLD);
                    tvi13.setText(numericwindspeed);
                    tvi14.setText("|");

                } else if (numericwaveheight >= 1.5 && numericwaveheight < 2) {//1.5
                    tvi12.setTypeface(null, Typeface.BOLD);
                    tvi12.setText(numericwindspeed);
                    tvi13.setText("|");
                    tvi14.setText("|");

                } else if (numericwaveheight >= 2 && numericwaveheight < 2.5) {//2
                    tvi11.setTypeface(null, Typeface.BOLD);
                    tvi11.setText(numericwindspeed);
                    tvi12.setText("|");
                    tvi13.setText("|");
                    tvi14.setText("|");

                } else if (numericwaveheight >= 2.5 && numericwaveheight < 3) {//2.5
                    tvi10.setTypeface(null, Typeface.BOLD);
                    tvi10.setText(numericwindspeed);
                    tvi11.setText("|");
                    tvi12.setText("|");
                    tvi13.setText("|");
                    tvi14.setText("|");

                } else if (numericwaveheight >= 3 && numericwaveheight < 3.5) {//3
                    tvi9.setTypeface(null, Typeface.BOLD);
                    tvi9.setText(numericwindspeed);
                    tvi10.setText("|");
                    tvi11.setText("|");
                    tvi12.setText("|");
                    tvi13.setText("|");
                    tvi14.setText("|");

                } else if (numericwaveheight >= 3.5 && numericwaveheight < 4) {//3.5
                    tvi8.setTypeface(null, Typeface.BOLD);
                    tvi8.setText(numericwindspeed);
                    tvi9.setText("|");
                    tvi10.setText("|");
                    tvi11.setText("|");
                    tvi12.setText("|");
                    tvi13.setText("|");
                    tvi14.setText("|");

                } else if (numericwaveheight >= 4 && numericwaveheight < 4.5) {//4
                    tvi7.setTypeface(null, Typeface.BOLD);
                    tvi7.setText(numericwindspeed);
                    tvi8.setText("|");
                    tvi9.setText("|");
                    tvi10.setText("|");
                    tvi11.setText("|");
                    tvi12.setText("|");
                    tvi13.setText("|");
                    tvi14.setText("|");

                } else if (numericwaveheight >= 4.5 && numericwaveheight < 5) {//4.5
                    tvi6.setTypeface(null, Typeface.BOLD);
                    tvi6.setText(numericwindspeed);
                    tvi7.setText("|");
                    tvi8.setText("|");
                    tvi9.setText("|");
                    tvi10.setText("|");
                    tvi11.setText("|");
                    tvi12.setText("|");
                    tvi13.setText("|");
                    tvi14.setText("|");

                } else {//5
                    tvi5.setTypeface(null, Typeface.BOLD);
                    tvi5.setText(numericwindspeed);
                    tvi6.setText("|");
                    tvi7.setText("|");
                    tvi8.setText("|");
                    tvi9.setText("|");
                    tvi10.setText("|");
                    tvi11.setText("|");
                    tvi12.setText("|");
                    tvi13.setText("|");
                    tvi14.setText("|");

                }

            }

            i = i + 1;
            if (i > recordssaved) {
                gooddata = false;
            }
        }

    }

    private float tryParseFloat(String y) {
        try {
            return Float.parseFloat(y.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }


    private void toggleButtonReset() {
        if (tinydb.getBoolean("alarm")) {
            tb.setChecked(true);
        } else {
            tb.setChecked(false);
        }
    }

    private void blink() {
        if (!blink) {
            blink = true;
            //new thread to cause the headline to blink if there is a strong wind warning in the forecast.
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        while (isActivityVisible()) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    if (alertwindwarning.getVisibility() == View.VISIBLE) {
                                        alertwindwarning.setVisibility(View.INVISIBLE);
                                    } else {
                                        alertwindwarning.setVisibility(View.VISIBLE);
                                    }

                                }
                            });

                            Thread.sleep(2000);
                        }
                        blink = false;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

    }

    private void setWindWarningVisible() {
        if (alertwindwarning.getVisibility() == View.INVISIBLE) {
            alertwindwarning.setVisibility(View.VISIBLE);
        }
    }

    //method call to pull the saved data from shared preferences and return as a string list
    private List<String> PullDataFile(String a) {

        List<String> rssdata = tinydb.getList(a);

        date = rssdata.get(0);
        time = rssdata.get(1);
        winddirection = rssdata.get(2);
        windspeed = rssdata.get(3);
        waveheight = rssdata.get(4);
        waveinterval = rssdata.get(5);
        winddirectiondegrees = rssdata.get(6);

        winddirectionletters = winddirection.replaceAll("[^a-z.A-Z]", "");
        numerictime = time.replaceAll("[^0-9.:]", "");
        numericwindspeed = windspeed.replaceAll("[^0-9.]", "");

        List<String> x = Arrays.asList(winddirectionletters, numerictime, numericwindspeed, waveheight, winddirectiondegrees);
        return x;
    }

}