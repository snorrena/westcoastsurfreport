package com.rsnorrena.westvansurfreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.rsnorrena.westvansurfreport.model.RssData;
import com.rsnorrena.westvansurfreport.parsers.RssXMLParser;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    RssData[] rssdatalist = new RssData[2];//data array object

    //class object and variable declarations
    TinyDB tinydb;
    String surfreport =  "";

    //field variables to hold the important data
    String date;
    String time, oldTime;
    String winddirection;
    String windspeed;
    String waveheight;
    String waveinterval;
    String winddirectiondegrees;

    //two xml data sources used in the app for wind forecast and Halibut Bank live data.
    String[] datasource = {"http://www.ndbc.noaa.gov/data/latest_obs/46146.rss","https://weather.gc.ca/rss/marine/14300_e.xml"};

    Context PassedContext;//context variable declaration to hold the contect passed into the onReceive method

    @Override
    public void onReceive(Context context, Intent intent) {

        PassedContext = context;//assign the passed in context the the declared context PassedContext
        Log.d(TAG, "On receive called");

        if (isOnline()) {//check for internet connectivity
            requestData(datasource);//passed the two xml data sources (html address) into the requestData method
        } else {
            Toast.makeText(PassedContext, "Network isn't available", Toast.LENGTH_LONG).show();
        }//msg to display if the internet isn't working

    }

    private void requestData(String[] uri) {//method receives the variable containing the two uri sources
        MyTask myTask = new MyTask();
        myTask.execute(uri);//executes the code in the member class myTask passing in the two uri sources in a string array
    }

    protected boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) PassedContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public class MyTask extends AsyncTask<String, String, String[]> {
//MyTask extends AsyncTask to execute http connect outside of the main thread

        @Override
        protected void onPreExecute() {//nothing happening here

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

            //only run code to update info when a new file is received from the Halibut Bank Buoy.
            if (!result[0].equals("")) {

                tinydb = new TinyDB (PassedContext);
                int recordssaved = tinydb.getInt("recordssaved");//record in app prefs that a new record has been added

                //call to the parseFeed method in the class RssXMLParser passing in the downloaded xml file array
                //result = content String array passed from the doinbackground method
                //the rssdatalist is an array list of two data objs containing in the info parsed from the downloaded xml files
                rssdatalist = RssXMLParser.parseFeed(result);

                //update the wind report if the parser returns something other than null.
                if (rssdatalist[1] == null) {
                    Log.d(TAG, "The wind warning report file is null");
                }
                else
                {

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
                }else {
                    int newH, oldH;
                    boolean nextHour = false;
                    RssData rssdata = rssdatalist[0];//data obj containing information from the Halibut bank xml file.
                    time = rssdata.getTime();

                    //The oldTime will not exist on first run.
                    try {
                        List<String> lastRecordTime = tinydb.getList("saveddatarecord" + String.valueOf(recordssaved));
                        oldTime = lastRecordTime.get(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        oldTime = "not set";
                        nextHour = true;
                    }

System.out.println("Old time = " + oldTime);
System.out.println("New time = " + time);

                    if (!nextHour) {
                        String[] oldHour = oldTime.split(":");
                        oldH = Integer.parseInt(oldHour[0]);
                        String[] newHour = time.split(":");
                        newH = Integer.parseInt(newHour[0]);

                        if (oldH == 23 && newH == 0){
                            nextHour = true;
                        }else if(newH > oldH){
                            nextHour = true;
                        }
                    }

                    //Only add the record if the time stamp is new.
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
                        System.out.println("currentdatafeed size = " + currentdatafeed.size());
                        int index = 0;
                        for (String item : itemstoadd) {
                            System.out.println(index + " " + item);
                            index++;
                        }

                        recordssaved = recordssaved + 1;
                        tinydb.putInt("recordssaved", recordssaved);
                        Log.d(TAG, "NewRecordadded");
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

                        //saves the currentdatafeed into app prefs using the key value assigned to saveddatarecord
                        tinydb.putList(saveddatarecord, currentdatafeed);

                        //execute code to check the saved surf condition data and assign a %surfscore value and save to the database
                        SurfConditionsCheck.SurfScore();

                        int surfgrade = tinydb.getInt("surfgrade");
                        int surfgradealarm = tinydb.getInt("surfgradealarm");
                        boolean sendtextmessage = tinydb.getBoolean("sendtextmessage");
                        boolean alarmtriggered = tinydb.getBoolean("alarmtriggered");

                        //Sound of alarm if conditions met
                        if (!alarmtriggered && surfgrade >= surfgradealarm) {
                            SoundAlarm soundAlarm = new SoundAlarm();
                            soundAlarm.soundAlarmOn();
                        }

                        //send e-mails/text messages if the option is selected in app settings and the surf grade has been reached.
                        if (sendtextmessage && surfgrade >= surfgradealarm) {

                            surfreport = "The surf potential is " + String.valueOf(surfgrade) + "%.";

                            SendAlarmMessage sndmsg = new SendAlarmMessage(tinydb, surfreport, date, time, winddirection, windspeed,
                                    waveheight, waveinterval, winddirectiondegrees);
                            sndmsg.sendEmailMessage();
                        }


                        currentdatafeed.clear();
                        //reset the boolean if a new data record is added
                        tinydb.putBoolean("newrecordadded", true);
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }
    }
}




