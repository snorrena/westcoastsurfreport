package com.rsnorrena.westvansurfreport;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.rsnorrena.westvansurfreport.model.RssData;
import com.rsnorrena.westvansurfreport.parsers.JsoupWebScrape;
import com.rsnorrena.westvansurfreport.parsers.RssXMLParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    RssData[] rssdatalist = new RssData[2];//data array object

    //class object and variable declarations
    TinyDB tinydb;
    String surfreport = "";

    //field variables to hold the important data
    String date;
    String time, oldTime;
    String winddirection;
    String windspeed;
    String waveheight;
    String waveinterval;
    String winddirectiondegrees;

    //two xml data sources used in the app for wind forecast and Halibut Bank live data.
    String[] datasource = {"https://www.ndbc.noaa.gov/data/latest_obs/46146.rss", "https://weather.gc.ca/rss/marine/14300_e.xml"};

    Context context;//context variable declaration to hold the contect passed into the onReceive method

    //intent to be used for set and run of the alarm monitoring service
    private AlarmManager manager;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;
    private Calendar cal;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;//assign the passed in context the the declared context context
        Log.d(TAG, "On receive called");

        //for set of the next alarm
        alarmIntent = new Intent("xyz.abc.ALARMUP");//intent identifier is coded in the android manifest file.
        pendingIntent = PendingIntent.getBroadcast(this.context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        cal = Calendar.getInstance();

        if (isOnline()) {//check for internet connectivity
            requestData(datasource);//passed the two xml data sources (html address) into the requestData method
        } else {
            Log.d(TAG, "The network isn't available!");

            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.MINUTE, 10);//adda ten minutes to the current time.
            setNextAlarm(cal);//set next alarm when the network is down.
        }//msg to display if the internet isn't working
    }

    private void setNextAlarm(Calendar cal) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String alarmTime = sdf.format(cal.getTime());
        Log.d("Next alarm time: ", alarmTime);

        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);//initialize the alarm service

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        }
    }

    private void requestData(String[] uri) {//method receives the variable containing the two uri sources
        MyTask myTask = new MyTask();
        myTask.execute(uri);//executes the code in the member class myTask passing in the two uri sources in a string array
    }

    protected boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public class MyTask extends AsyncTask<String, String, String[]> {
//MyTask extends AsyncTask to execute http connect outside of the main thread

        TinyDB tdb = new TinyDB(context);

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "MyTask onPreExecute called");
            try {
                if (!tdb.getBoolean("webScrapeComplete")) {//check if web scrape is in process.
                    Log.d(TAG, "Paused in OnPreExecute - waiting for webScrape to complete");
                    while (!tdb.getBoolean("webScrapeComplete")) {//pause until the web scrape finishes download of new data.
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    sleep(3000);//pause the app on the splash screen for three seconds
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String[] doInBackground(String... params) {//method receives a string array containing the two uri sources
            //then returns a string array with two data items consisting of the two xml files downloaded from the internet
            String[] content = new String[2];//this array hold the two xml files

            for (int i = 0; i < content.length; i++) {//loop to pass in each of the two uri's under params
                //and save the returned xml data files into the content string array.
                content[i] = HttpManager.getData(params[i]);//call the the getData method in the HttpManager class
            }

            return content;//sent the content array to the post execute method
        }

        @Override
        protected void onPostExecute(String[] result) {

            tinydb = new TinyDB(context);

            int recordssaved = tinydb.getInt("recordssaved");

            //call to the parseFeed method in the class RssXMLParser passing in the downloaded xml file array
            //result = content String array passed from the doinbackground method
            RssData halibutBankData = RssXMLParser.parseFeed(result, 0);
            RssData windData = RssXMLParser.parseFeed(result, 1);

            //add some sort of data check in here to validate the files to be saved in memory.

            rssdatalist[0] = halibutBankData;
            rssdatalist[1] = windData;

            //update the wind report if the parser returns something other than null.
            if (rssdatalist[1] == null) {
                Log.d(TAG, "The wind warning report file is null");
            } else {

                RssData rssdata_windwarning = rssdatalist[1];//data obj containing wind warning info from envir Canada

                //code to extract and save the wind warning data
                String title1 = rssdata_windwarning.getTitle1();
                String title2 = rssdata_windwarning.getTitle2();
                String title3 = rssdata_windwarning.getTitle3();
                String summary1 = rssdata_windwarning.getSummary1();
                String summary2 = rssdata_windwarning.getSummary2();

                //the wind warning data is then saved into a string array
                ArrayList<String> windwarningdata = new ArrayList<>();
                windwarningdata.add(title1);
                windwarningdata.add(title2);
                windwarningdata.add(title3);
                windwarningdata.add(summary1);
                windwarningdata.add(summary2);

                tinydb.putList("windforecast", windwarningdata);//the wind warning info is saved in app prefs
            }

            //update the Halibut Bank data if the parser returns something other than null.
            if (rssdatalist[0] == null) {

                Log.d(TAG, "The Halibut Bank report file is null");

                //set new alarm here to check for new record in ten minutes
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.add(Calendar.MINUTE, 10);//adda ten minutes to the current time.

                setNextAlarm(cal);

            } else {

                int newH, oldH;
                RssData rssdata = rssdatalist[0];//data obj containing information from the Halibut bank xml file.
                time = rssdata.getTime();

                List<String> lastRecordTime = tinydb.getList("saveddatarecord" + String.valueOf(recordssaved));
                oldTime = lastRecordTime.get(1);

                Log.d(TAG, "Old time = " + oldTime);
                Log.d(TAG, "New time = " + time);

                String[] oldHour = oldTime.split(":");
                oldH = Integer.parseInt(oldHour[0]);
                String[] newHour = time.split(":");
                newH = Integer.parseInt(newHour[0]);

                boolean nextHour = false;

                if (newH != oldH) {//test for sequential hourly report.


                    if (23 == oldH && newH > 0) {

                        refreshHalibutBankData();
                        nextHour = true;

                    }

                    if (0 == oldH && newH > 1) {

                        refreshHalibutBankData();
                        nextHour = true;

                    }

                    if ((newH - oldH) > 1) {//hour difference greater than one.

                        refreshHalibutBankData();
                        nextHour = true;

                    }

                    if (1 == (newH - oldH)) {

                        nextHour = true;

                    }

                }

                //Only add the record if the time stamp is the next hour or if the data has been refreshed..
                if (nextHour) {

                    //code to extract and save the halibut bank data
                    date = rssdata.getDate();
                    winddirection = rssdata.getWind_direction();
                    windspeed = rssdata.getWind_speed();
                    waveheight = rssdata.getWave_height();
                    waveinterval = rssdata.getWave_interval();
                    winddirectiondegrees = (winddirection.replaceAll("[^0-9]", ""));

                    //the Halibut bank data items are added to a string array then then the contents of that array are added to yet another array
                    ArrayList<String> currentdatafeed = new ArrayList<String>();
                    ArrayList<String> itemstoadd = new ArrayList<String>();
                    itemstoadd.add(date);
                    itemstoadd.add(time);
                    itemstoadd.add(winddirection);
                    itemstoadd.add(windspeed);
                    itemstoadd.add(waveheight);
                    itemstoadd.add(waveinterval);
                    itemstoadd.add(winddirectiondegrees);
                    currentdatafeed.addAll(itemstoadd);

                    recordssaved = tinydb.getInt("recordssaved");
                    ++recordssaved;
                    tinydb.putInt("recordssaved", recordssaved);
                    Log.d(TAG, "NewRecordadded # " + String.valueOf(recordssaved));
                    String saveddatarecord = "";

                    ArrayList<String> retaineddatarecord = new ArrayList<String>();

                    if (recordssaved == 1) {//set the key values based on the number of records saved
                        saveddatarecord = "saveddatarecord1";
                    } else if (recordssaved == 2) {
                        saveddatarecord = "saveddatarecord2";
                    } else if (recordssaved == 3) {
                        saveddatarecord = "saveddatarecord3";
                    } else if (recordssaved == 4) {
                        saveddatarecord = "saveddatarecord4";
                    } else if (recordssaved == 5) {
                        saveddatarecord = "saveddatarecord5";
                    } else if (recordssaved == 6) {
                        saveddatarecord = "saveddatarecord6";

                    } else if (recordssaved > 6) {//code the execute if there are already six records in the database
                        saveddatarecord = "saveddatarecord6";

                        //code the shuffle the saved data down if there are already six files in the database
                        retaineddatarecord = tinydb.getList("saveddatarecord2");
                        tinydb.putList("saveddatarecord1", retaineddatarecord);
                        retaineddatarecord = tinydb.getList("saveddatarecord3");
                        tinydb.putList("saveddatarecord2", retaineddatarecord);
                        retaineddatarecord = tinydb.getList("saveddatarecord4");
                        tinydb.putList("saveddatarecord3", retaineddatarecord);
                        retaineddatarecord = tinydb.getList("saveddatarecord5");
                        tinydb.putList("saveddatarecord4", retaineddatarecord);
                        retaineddatarecord = tinydb.getList("saveddatarecord6");
                        tinydb.putList("saveddatarecord5", retaineddatarecord);

                        tinydb.putInt("recordssaved", 6);
                    }
                    retaineddatarecord.clear();

                    //saves the currentdatafeed into app prefs using the key value assigned to saveddatarecord
                    tinydb.putList(saveddatarecord, currentdatafeed);

                    currentdatafeed.clear();

                    //set the boolean for a new data record added.
                    tinydb.putBoolean("newrecordadded", true);

                    //execute code to check the saved surf condition data and assign a %surfscore value and save to the database
                    SurfConditionsCheck.SurfScore();

                    int surfgrade = tinydb.getInt("surfgrade");
                    int surfgradealarm = tinydb.getInt("surfgradealarm");
                    boolean sendtextmessage = tinydb.getBoolean("sendtextmessage");
                    boolean alarm = tinydb.getBoolean("alarm");

                    //Sound of alarm if conditions met
                    if (alarm && surfgrade >= surfgradealarm) {
                        final SoundAlarm soundAlarm = new SoundAlarm();
                        soundAlarm.soundAlarmOn();

                        //using a handler to pass the soundAlarm obj to the main UI to allow for shut down via the toggle button.
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable myRunnable = new Runnable() {

                            @Override
                            public void run() {
                                MainActivity.passSoundAlarmObject(soundAlarm);
                            }
                        };
                        mainHandler.post(myRunnable);
                    }

                    //send e-mails/text messages if the option is selected in app settings and the surf grade has been reached.
                    if (sendtextmessage && surfgrade >= surfgradealarm) {

                        surfreport = "The surf potential is " + String.valueOf(surfgrade) + "%.";

                        SendAlarmMessage sndmsg = new SendAlarmMessage(tinydb, surfreport, date, time, winddirection, windspeed,
                                waveheight, waveinterval, winddirectiondegrees);
                        sndmsg.sendEmailMessage();
                    }

                    //set new alarm here for next hour.
                    cal.setTimeInMillis(System.currentTimeMillis());
                    cal.roll(Calendar.HOUR_OF_DAY, true);//roll the hour of day forward
                    cal.set(Calendar.MINUTE, 00);//set the minutes to zero

                    setNextAlarm(cal);

                } else {
                    //set new alarm here to check for new record in ten minutes
                    cal.setTimeInMillis(System.currentTimeMillis());
                    cal.add(Calendar.MINUTE, 10);//adda ten minutes to the current time.

                    setNextAlarm(cal);
                }
            }
        }

        private void refreshHalibutBankData() {

            Log.d(TAG, "Data refreshed");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    tinydb.putBoolean("webScrapeComplete", false);
                    JsoupWebScrape webScrape = new JsoupWebScrape(context);
                    webScrape.scrapeHalibutBankData();
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }
    }
}




