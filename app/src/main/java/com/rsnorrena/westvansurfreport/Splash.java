package com.rsnorrena.westvansurfreport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.rsnorrena.westvansurfreport.parsers.JsoupWebScrape;

public class Splash extends Activity {

    private static final String TAG = Splash.class.getSimpleName();

    public static Context context;
    //declaration of the context variable for this class

    TinyDB tdb_splash;
    boolean appsettings;
    //declaration of the class fields/variables for the preference database and the boolean for the app settings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        //override the onCreate method of this Activity and set the layout as defined in the splash xml layout file.

        context = getApplication();
        //sets the context variable by calling the getApplication method of the Activity class

        if (isOnline()) {//only start the application if there is internet connectivity.

            tdb_splash = new TinyDB(context);
            //initializes the preferences database for this app
            appsettings = tdb_splash.getBoolean("appsettings");
            //check of the preferences database to determine if the app settinhgs have been saved

            int recordCount;
            try {
                recordCount = tdb_splash.getInt("recordssaved");
                Log.d(TAG, "Record count: " + String.valueOf(recordCount));
            } catch (Exception e) {
                recordCount = 0;
                e.printStackTrace();
            }

            //collect Halibut Bank data from the web if record count is less than 6
            if (recordCount < 6) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        tdb_splash.putBoolean("webScrapeComplete", false);//set to false at call the jsoupWebScrape.
                        JsoupWebScrape webScrape = new JsoupWebScrape(context);
                        webScrape.scrapeHalibutBankData();//method to scrape the Halibut Bank wind and wave data from the web.
                    }
                });
                t.start();
            }

            Thread timer = new Thread() {
                //starts a new thread to pause the app for three seconds before moving on to a new activity based on contents of the app settings boolean

                public void run() {

                    try {

                        if (!tdb_splash.getBoolean("webScrapeComplete")) {//check if web scrape is in process.
                            Log.d(TAG, "Paused for web scrape");
                            while (!tdb_splash.getBoolean("webScrapeComplete")) {//pause until the web scrape finishes download of new data.
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

        } else {//dialogue displayed if there is no internet connectivity
            Log.d(TAG, "No internet connection");
            try {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this, R.style.YourDialogStyle);
                builder.setMessage("No internet!\n\nCheck your network settings and try again.").setPositiveButton("Yes", dialogClickListener).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }//end of onCreate

    //method to check internet connectivity
    protected boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}
