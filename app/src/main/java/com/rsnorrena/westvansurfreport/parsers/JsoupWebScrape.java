package com.rsnorrena.westvansurfreport.parsers;

import android.content.Context;
import android.util.Log;

import com.rsnorrena.westvansurfreport.TinyDB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Admin on 2017-03-20.
 */

public class JsoupWebScrape {

    private static final String TAG = JsoupWebScrape.class.getSimpleName();
    Context context;
    TinyDB tb;

    public JsoupWebScrape(Context c) {
        this.context = c;
    }

    //tag to be used in logs
    public void scrapeHalibutBankData() {

        Log.d(TAG, "JsoupWebScrape called");

        tb = new TinyDB(context);

        //clean out database records for refresh
        tb.remove("windforecast");
        tb.putInt("surfgrade", 0);
        tb.putInt("recordssaved", 0);
        tb.remove("saveddatarecord1");
        tb.remove("saveddatarecord2");
        tb.remove("saveddatarecord3");
        tb.remove("saveddatarecord4");
        tb.remove("saveddatarecord5");
        tb.remove("saveddatarecord6");
        tb.remove("alarmtriggered");
        tb.remove("lastRecordSavedDateAndTime");


        try {
            Document doc = Jsoup.connect("http://www.ndbc.noaa.gov/station_page.php?station=46146").timeout(6000).get();

            int recordNumber = 7;//number of records to be scrapped and populated in the database. Skips the first two tr.
            String recordKey = "saveddatarecord";

            for (Element table : doc.select("table.dataTable")) {//main loop to go through the source web page

                String date = null;
                String time = null;
                String winddirection = null;
                String windspeed = null;
                String waveheight = null;
                String waveinterval = null;
                String winddirectiondegrees = null;

                for (Element row : table.select("tr")) {//start of inner for range loop to go through the table data
                    Elements tds = row.select("td");

                    if (recordNumber <= 5 && recordNumber >= 1) {//skips processing of data in the first two table rows.

                        String cleanedRow = RssXMLParser.html2text(tds.toString());
                        String demilms = "[ ]";
                        String[] tokens = cleanedRow.split(demilms);

                        int month = Integer.valueOf(tokens[0]);

                        String nameOfMonth = null;

                        switch (month) {
                            case 1:
                                nameOfMonth = "January";
                                break;
                            case 2:
                                nameOfMonth = "February";
                                break;
                            case 3:
                                nameOfMonth = "March";
                                break;
                            case 4:
                                nameOfMonth = "April";
                                break;
                            case 5:
                                nameOfMonth = "May";
                                break;
                            case 6:
                                nameOfMonth = "June";
                                break;
                            case 7:
                                nameOfMonth = "July";
                                break;
                            case 8:
                                nameOfMonth = "August";
                                break;
                            case 9:
                                nameOfMonth = "September";
                                break;
                            case 10:
                                nameOfMonth = "October";
                                break;
                            case 11:
                                nameOfMonth = "November";
                                break;
                            case 12:
                                nameOfMonth = "December";
                                break;
                            default:
                                Log.d(TAG, "Invalid value for month");
                                break;
                        }

                        String windDirection = tokens[3];
                        switch (windDirection) {
                            case "N":
                                winddirectiondegrees = "360";
                                break;
                            case "NNE":
                                winddirectiondegrees = "22";
                                break;
                            case "NE":
                                winddirectiondegrees = "45";
                                break;
                            case "ENE":
                                winddirectiondegrees = "67";
                                break;
                            case "E":
                                winddirectiondegrees = "90";
                                break;
                            case "ESE":
                                winddirectiondegrees = "112";
                                break;
                            case "SE":
                                winddirectiondegrees = "135";
                                break;
                            case "SSE":
                                winddirectiondegrees = "157";
                                break;
                            case "S":
                                winddirectiondegrees = "180";
                                break;
                            case "SSW":
                                winddirectiondegrees = "202";
                                break;
                            case "SW":
                                winddirectiondegrees = "225";
                                break;
                            case "WSW":
                                winddirectiondegrees = "247";
                                break;
                            case "W":
                                winddirectiondegrees = "270";
                                break;
                            case "WNW":
                                winddirectiondegrees = "292";
                                break;
                            case "NW":
                                winddirectiondegrees = "315";
                                break;
                            case "NNW":
                                winddirectiondegrees = "337";
                                break;
                            default:
                                Log.d(TAG, "Invalid wind direction");
                                break;
                        }

                        time = tokens[2];
                        String[] hour = time.split(":");
                        int h = Integer.parseInt(hour[0]);//get the hour of time from the token

                        if (time.contains("12:00 am")) {//change to 24 hour clock if time is 12 am.
                            time = "00:00 am";
                        }

                        if (time.contains("pm") && h < 12) {//change to 24 hour clock
                            h = h + 12;
                            time = h + ":00 pm";
                        }

                        Calendar cal;
                        cal = Calendar.getInstance();
                        cal.setTimeInMillis(System.currentTimeMillis());
                        int year = cal.get(Calendar.YEAR);

                        date = nameOfMonth + " " + tokens[2] + ", " + year;
                        winddirection = tokens[3];
                        windspeed = tokens[4];
                        waveheight = tokens[6];
                        waveinterval = tokens[7];

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

                        switch (recordNumber) {
                            case 1:
                                recordKey = "saveddatarecord1";
                                break;
                            case 2:
                                recordKey = "saveddatarecord2";
                                break;
                            case 3:
                                recordKey = "saveddatarecord3";
                                break;
                            case 4:
                                recordKey = "saveddatarecord4";
                                break;
                            case 5:
                                recordKey = "saveddatarecord5";
                                break;
                            case 6:
                                recordKey = "saveddatarecord6";
                                break;
                        }

                        tb.putList(recordKey, currentdatafeed);

                        currentdatafeed.clear();

                        ArrayList<String> data = tb.getList(recordKey);
                        for (String r : data) {
                            Log.d(TAG, String.valueOf(recordNumber));
                            Log.d(TAG, r + ", ");
                        }

                    }//end of if statement
                    --recordNumber;

                }//end of the inner for range loop going through the table data.
            }//end of for range loop through full doc

            tb.putInt("recordssaved", 5);//for 5 records scrapped from the web.

            ArrayList<String> lastRecord = tb.getList("saveddatarecord5");
            String hourString = lastRecord.get(1);
            String[] hour = hourString.split(":");
            int h = Integer.parseInt(hour[0]);//get the hour of time from the token

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Date d = cal.getTime();
            Long dateInLong = d.getTime();

            tb.putLong("lastRecordSavedDateAndTime", dateInLong);//saves the timestamp of the 5th record in the database.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
