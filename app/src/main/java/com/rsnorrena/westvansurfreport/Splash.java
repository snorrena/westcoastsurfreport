package com.rsnorrena.westvansurfreport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rsnorrena.westvansurfreport.parsers.JsoupWebScrape;

public class Splash extends Activity {

    public static Context context;
    //declaration of the context variable for this class

    TinyDB tdb_splash;
    boolean appsettings;
    //declaration of the class fields/variables for the preference database and the boolean for the app settings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        //overide the onCreate method of this Activity and set the layout as defined in the splash xml layout file.

        context = getApplication();
        //sets the context variable by calling the getApplication method of the Activity class

        appsettings = false;
        //initializes the app settings boolean

        tdb_splash = new TinyDB(context);
        //initializes the preferences database for this app
        appsettings = tdb_splash.getBoolean("appsettings");
        //check of the preferences database to determine if the app settinhgs have been saved

        int recordCount;
        try {
            recordCount = tdb_splash.getInt("recordssaved");
        } catch (Exception e) {
            recordCount = 0;
            e.printStackTrace();
        }

        //collect Halibut Bank data from the web if record count is less than 6
        if (recordCount < 6) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
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


        Thread timer = new Thread() {
            //starts a new thread to pause the app for three seconds before moving on to a new activity based on contents of the app settings boolean

            public void run() {

                try {
                    sleep(3000);//pause the app for three seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    if (appsettings) {//start the main activity if the app settings boolean is set to true. Otherwise go to the set up screen.
                        Intent ourIntent = new Intent(Splash.this, MainActivity.class);
                        ourIntent.setFlags(ourIntent.FLAG_ACTIVITY_CLEAR_TOP);
                        Splash.this.startActivity(ourIntent);
                        finish();
                    } else {
                        Intent ourIntent = new Intent(Splash.this, SetUp.class);
                        Splash.this.startActivity(ourIntent);
                        finish();
                    }

                }

            }

        };

        timer.start();

    }
}
