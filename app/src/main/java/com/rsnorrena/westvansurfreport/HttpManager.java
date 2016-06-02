package com.rsnorrena.westvansurfreport;

/**
 * Created by Admin on 3/30/2015.
 */

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;


public class HttpManager {

    StringBuilder sb;
    BufferedReader reader = null;
    HttpURLConnection con;
    String fileToReturn = "";
    TinyDB tinyDB;
    Date oldFileDate, newFileDate;

    public String getData(String uri, int i) {

        try {
            URL url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                //only update the report time stamp when a new Halibut Bank report is downloaded.
                if (i == 0) {
                    //create the database object
                    tinyDB = new TinyDB(MainActivity.context);

                    //get the int value of the number of records saved in the database
                    int recordssaved = tinyDB.getInt("recordssaved");

                    long newFileTimeStamp;

                    //initialization of the old and new date objects one minute apart.
                    //The old date is then converted to a long then to a string and saved in shared preferences.
                    Long curTime = con.getLastModified();
                    oldFileDate = new Date(curTime - 60000);
                    newFileDate = new Date(curTime);

                    tinyDB.putString("fileTimeStamp", newFileDate.toString());

                    if (recordssaved > 0) {
                        //retrieval of a long value for the header date from the url connection.
                        newFileTimeStamp = con.getLastModified();
                        //convert the con long value to a date.
                        newFileDate = new Date(newFileTimeStamp);
                        //save of the string into shared preferences
                        tinyDB.putString("fileTimeStamp", newFileDate.toString());

                        //return the last data file saved in shared preferences
                        List<String> x = tinyDB.getList("saveddatarecord" + String.valueOf(recordssaved));

                        //get the Date object from the last saved file
                        String lastSavedRecordTimeStamp = x.get(7);
                        oldFileDate = new Date(lastSavedRecordTimeStamp);

                    }
                    System.out.println("The old date is: " + oldFileDate.toString());
                    System.out.println("The new date is: " + newFileDate.toString());

                    if (newFileDate.after(oldFileDate)) {

                        System.out.println("The date test passed");

                        downloadNewFile();
                    }
                }else{
                    downloadNewFile();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileToReturn;
    }

    private void downloadNewFile() throws IOException {
        sb = new StringBuilder();
        reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        fileToReturn = sb.toString();
    }

}

