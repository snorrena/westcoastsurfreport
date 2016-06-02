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


    public static String getData(String uri) {

        StringBuilder sb;
        BufferedReader reader = null;
        HttpURLConnection con;
        String fileToReturn = "";

        try {
            URL url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                fileToReturn = sb.toString();

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

}

