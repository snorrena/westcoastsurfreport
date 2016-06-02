package com.rsnorrena.westvansurfreport;

import android.content.Context;

import java.util.List;

/**
 * Created by Admin on 4/13/2015.
 */
public class SurfConditionsCheck {


    public static void SurfScore() {

        int surfgrade = 0;

        List<String> retrieveddatarecord;
        int recordssaved = 0;

        float threehourgoodwindtrend = 0;
        float threehouronemeterwaveheight = 0;
        float threehourtwentyknotwinds = 0;

        TinyDB tinydb = new TinyDB(MainActivity.context);
        recordssaved = tinydb.getInt("recordssaved");


        int recordstart = recordssaved - 2;

        //grade the surf conditions based on the last three records saved in the database.

        if (recordstart > 0) {

            for (int i = recordstart; i <= recordssaved; i++) {
                String x = "saveddatarecord" + String.valueOf(i);
                retrieveddatarecord = tinydb.getList(x);

                String winddirection = retrieveddatarecord.get(2);
                String windspeed = retrieveddatarecord.get(3);
                String waveheight = retrieveddatarecord.get(4);

                int winddirectiondegrees = Integer.valueOf(winddirection.replaceAll("[^0-9]", ""));
                float numericwindspeed = Float.valueOf(windspeed.replaceAll("[^0-9.]", ""));
                float windsp = Float.valueOf(numericwindspeed);
                float numericwaveheight = Float.valueOf(waveheight.replaceAll("[^0-9.]", ""));

//only use data for westerly wind direction
                if (winddirectiondegrees >= 270 && winddirectiondegrees <= 315) {
                    threehourgoodwindtrend = threehourgoodwindtrend + 1;
                    if (numericwaveheight >= 2.5) {
                        threehouronemeterwaveheight = threehouronemeterwaveheight + 1;
                    }
                    if (windsp >= 15) {
                        threehourtwentyknotwinds = threehourtwentyknotwinds + 1;
                    }
                }


            }


            float windtrendgrade = ((threehourgoodwindtrend / 3) * 100);
            float waveheightgrade = ((threehouronemeterwaveheight / 3) * 100);
            float windspeedgrade = ((threehourtwentyknotwinds / 3) * 100);

            int windir = tinydb.getInt("windir");//calculate the surfgrade using the % weights input by the user
            int windspd = tinydb.getInt("windspd");
            int waveht = tinydb.getInt("waveht");

            surfgrade = Math.round((windtrendgrade * windir + waveheightgrade * waveht + windspeedgrade * windspd) / 100);


        }else{
            surfgrade = 0;
        }
        tinydb.putInt("surfgrade", surfgrade);
    }
}
