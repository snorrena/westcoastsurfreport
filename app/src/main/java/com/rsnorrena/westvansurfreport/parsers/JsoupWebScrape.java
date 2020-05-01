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
        tb.putBoolean("isWindWarningUpdated", false);

        while (!tb.getBoolean("webScrapeComplete")) {//loop until the document is downloaded and parsed successfully.

            Document doc = null;

            try {
                //get conditions report for the Halibut Bank buoy.
                doc = Jsoup.connect("https://www.ndbc.noaa.gov/station_page.php?station=46146").timeout(6000).get();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (doc != null) {//only attempt to parse if a document is returned.

                String recordKey = null;
                String date = null;
                String time = null;
                String winddirection = null;
                String windspeed = null;
                String waveheight = null;
                String waveinterval = null;
                String winddirectiondegrees = null;
                String nameOfMonth = null;

                //get the current report
                Element currentReport = doc.getElementById("data");
                Element currentReportTable = currentReport.select("table").get(0);
                Elements currentReportRows = currentReportTable.select("tr");

                Element titleDataHeader = currentReport.getElementsByClass("titleDataHeader").get(0);
                String titleData = RssXMLParser.html2text(titleDataHeader.toString());//clean out the html tags
                Log.d(TAG, "TitleDataHeader");
                Log.d(TAG, titleData.toString());
                String[] titleDataHeaderTokens = titleData.toString().split(" ");
                Log.d(TAG, "Title token data");
                for(int i = 0; i < titleDataHeaderTokens.length; i++){
                    Log.d(TAG, "index: " + i + ": " + titleDataHeaderTokens[i]);
                }

                for(int i = 0; i < currentReportRows.size(); i++){
                    Element row = currentReportRows.get(i);
                    Elements cols = row.select("td");
                    String cleanedRow = RssXMLParser.html2text(cols.toString());//clean out the html tags
                    String demilms = "[ ]";
                    String[] tokens = cleanedRow.split(demilms);
                    //iterate through current data in table rows
                    Log.d(TAG, "Row index: " + i);
                    if(i == 1){
                        winddirection = tokens[3];
                        winddirectiondegrees = tokens[5];
                    }
                    if(i == 2){
                        windspeed = tokens[3];
                    }
                    if(i==4){
                        waveheight = tokens[3];
                    }
                    if(i==5){
                        waveinterval = tokens[4];
                    }
                    for(String item: tokens) {
                        Log.d(TAG, item);
                    }

                }

                recordKey = setRecordKey(6);//set the record key for the current report
                String numericTime = titleDataHeaderTokens[5].substring(1, titleDataHeaderTokens[5].length());
                time = setReportTime(numericTime + " " + titleDataHeaderTokens[6]);
                Log.d(TAG, "Record time: " + time);
                String dateToParse = titleDataHeaderTokens[9];
                String[] dateElements = dateToParse.split("/");
                int month = Integer.valueOf(dateElements[0]);
                nameOfMonth = getNameOfMonth(month);
                Log.d(TAG, "Month int: " + month + ", Month name: " + nameOfMonth);
                date = nameOfMonth + " " + dateElements[0] + ", " + dateElements[2].substring(0, (dateElements[2].length() - 1));
                Log.d(TAG, "Date: " + date);

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

                tb.putList(recordKey, currentdatafeed);

                currentdatafeed.clear();
                itemstoadd.clear();


                int recordNumber = 7;//number of records to be scrapped and populated in the database. Skips the first two tr.

                for (Element table : doc.select("table.dataTable")) {//main loop to go through the source web page

                    for (Element row : table.select("tr")) {//start of inner for range loop to go through the table data

                        Elements tds = row.select("td");

                        if (recordNumber <= 5 && recordNumber >= 1) {//on process data in table rows 1 - 5 descending.

                            String cleanedRow = RssXMLParser.html2text(tds.toString());//clean out the html tags
                            String demilms = "[ ]";
                            String[] tokens = cleanedRow.split(demilms);

                            month = Integer.valueOf(tokens[0]);// index 0 element in the token array is the month.

                             nameOfMonth = getNameOfMonth(month);

                            String windDirection = tokens[3];//index 3 in the token array represents the wind direction.
                            winddirectiondegrees = getWindDirectionInDegrees(windDirection);

                            time = setReportTime(tokens[2]);

                            Calendar cal;//used to get the year
                            cal = Calendar.getInstance();
                            cal.setTimeInMillis(System.currentTimeMillis());
                            int year = cal.get(Calendar.YEAR);

                            date = nameOfMonth + " " + tokens[2] + ", " + year;
                            winddirection = tokens[3];
                            windspeed = tokens[4];
                            waveheight = tokens[6];
                            waveinterval = tokens[7];

                            //check if the wave height information is accurate
                            String checkedWaveHeight = waveheight.replaceAll("[^0-9.]", "");
                            if (checkedWaveHeight.equals("")) {
                                waveheight = "0";
                                waveinterval = "0";
                            }

                            //the Halibut bank data items are added to a string array then then the contents of that array are added to yet another array
                            currentdatafeed.clear();
                            itemstoadd.clear();
                            itemstoadd.add(date);
                            itemstoadd.add(time);
                            itemstoadd.add(winddirection);
                            itemstoadd.add(windspeed);
                            itemstoadd.add(waveheight);
                            itemstoadd.add(waveinterval);
                            itemstoadd.add(winddirectiondegrees);
                            currentdatafeed.addAll(itemstoadd);

                            recordKey = setRecordKey(recordNumber);

                            tb.putList(recordKey, currentdatafeed);

                            currentdatafeed.clear();

                        }//end of if statement
                        --recordNumber;

                    }//end of the inner for range loop going through the table data.
                }//end of for range loop through full doc

                tb.putInt("recordssaved", 6);//for 5 records scrapped from the web.

                ArrayList<String> lastRecord = tb.getList("saveddatarecord6");
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
                tb.putBoolean("webScrapeComplete", true);//set boolean to end the web scrape loop.

            }//end of if statement.

            while (!tb.getBoolean("isWindWarningUpdated")) {//main loop to scrape the wind report

                doc = null;

                try {
                    //get the wind report for north of Nanaimo.
                    doc = Jsoup.connect("https://weather.gc.ca/rss/marine/14300_e.xml").timeout(6000).get();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (doc != null) {//parse the report if the doc is not null

                    int titleCount = 0;//variables to keep track of fields of interest.
                    int summaryCount = 0;

                    String title1 = null;//variables to hold needed title and summary field data.
                    String title2 = null;
                    String title3 = null;
                    String summary1 = null;
                    String summary2 = null;

                    for (Element e : doc.select("title")) {//loop through the doc querying the title fields
                        String title = RssXMLParser.html2text(e.text());//parse out the html tags with Jsoup.
                        ++titleCount;
                        switch (titleCount) {//switch to set title variables
                            case 1:
                                title1 = title;
                                break;
                            case 2:
                                title2 = title;
                                break;
                            case 3:
                                title3 = title;
                                break;
                            default:
                                break;
                        }
                    }

                    for (Element e : doc.select(("summary"))) {//loop through the doc querying the summary fields.
                        String summary = RssXMLParser.html2text(e.text());//parse out the html tags
                        ++summaryCount;
                        switch (summaryCount) {//switch to set the summary variables.
                            case 1:
                                summary1 = Jsoup.parse(summary).text();
                                break;
                            case 2:
                                summary2 = Jsoup.parse(summary).text();
                                break;
                            default:
                                break;
                        }
                    }

                    //the wind warning data is then saved into a string array list.
                    ArrayList<String> windwarningdata = new ArrayList<>();
                    windwarningdata.add(title1);
                    windwarningdata.add(title2);
                    windwarningdata.add(title3);
                    windwarningdata.add(summary1);
                    windwarningdata.add(summary2);

                    tb.putList("windforecast", windwarningdata);//the wind warning data is saved in app prefs
                    tb.putBoolean("isWindWarningUpdated", true);//set to end the wind warning loop.


                }//end of if statement.

            }//end of wind warning while loop


        }//end of webscrape while loop
    }//end of scrapeHalibutBankData method.

    private String setReportTime(String time) {
        String[] hour = time.split(":");
        int h = Integer.parseInt(hour[0]);//get the hour of time from the token

        if (time.contains("am") && (h == 12)) {//change to 24 hour clock if time is 12 am.
            time = "00:00 am";
        }

        if (time.contains("pm") && h < 12) {//change to 24 hour clock
            h = h + 12;
            time = h + ":00 pm";
        }
        return time;
    }

    private String setRecordKey(int recordNumber) {
        String recordKey = null;
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
        return recordKey;
    }

    private String getWindDirectionInDegrees(String windDirection) {
        String winddirectiondegrees = null;
        switch (windDirection) {//the wind direction is used to determine the wind direction in degrees.

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
        return winddirectiondegrees;
    }

    private String getNameOfMonth (int month){
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
        return nameOfMonth;
    }
}//end of JsoupWebScrape class
