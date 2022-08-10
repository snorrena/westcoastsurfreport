package com.rsnorrena.westvansurfreport.parsers;

import android.util.Log;

import com.rsnorrena.westvansurfreport.TinyDB;
import com.rsnorrena.westvansurfreport.model.RssData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RssXMLParser {

    private static final String TAG = RssXMLParser.class.getSimpleName();

    public static RssData parseFeed(String[] content, int index, RssData rssdata) throws IOException, SAXException, ParserConfigurationException {
        //method receives the string value of the xml file(s) in the String array "content"
        // and returns the data file "Rssata".

        //instance of the data object class for the current data object

        boolean inDataItemTag = false;
        //used to determine if we care about the current data item

        if (index == 2){
            rssdata = scrapeHalibutBankData(rssdata, content[2]);
        }

        if (index == 1){
            rssdata = scrapeWindWarningData(rssdata, content[1]);
        }

        return rssdata;
    }

    private static RssData scrapeWindWarningData(RssData rssdata, String content) {
        Log.d(TAG, "Downloading the wind warning data file");
        //code for parsing the windwarding data
        //data to be collected from xml - title fields 1-3 and summary fields 1 & 2.
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(content));

            int eventType = parser.getEventType();
            //the XmlPullParser generates events, start tag, end tag, text, attributes
            //in this example we only care about the start tags end tags and text events
            int titlefieldcount = 0;
            int summaryfiedcount = 0;
            String currentTagName = null;
            boolean inDataItemTag = false;

            rssdata = new RssData();// we only need one data object for the the wind warning info

            while (eventType != XmlPullParser.END_DOCUMENT) {//loop through the whole file until the end tag

                switch (eventType) {//switch based on the event type or tag name

                    case XmlPullParser.START_TAG:
                        currentTagName = parser.getName();
                        if (currentTagName.equals("title")) {

                            titlefieldcount++;

                            if (titlefieldcount < 4) {

                                inDataItemTag = true;

                            }

                        }

                        if (currentTagName.equals("summary")) {

                            summaryfiedcount++;

                            if (summaryfiedcount < 3) {

                                inDataItemTag = true;

                            }

                        }

                        break;

                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("title")) {
                            inDataItemTag = false;
                        }
                        if (parser.getName().equals("summary")) {
                            inDataItemTag = false;
                        }
                        currentTagName = "";
                        break;

                    case XmlPullParser.TEXT:
                        if (inDataItemTag && rssdata != null) {
                            switch (currentTagName) {

                                case "title":

                                    String title = parser.getText();
                                    //the raw text is parsed from inbetween the (2) set of description tags in the rss file

                                    if (titlefieldcount == 1) {//call the the set method in the data obj to save the title text content
                                        rssdata.setTitle1(title);
                                    } else if (titlefieldcount == 2) {
                                        rssdata.setTitle2(title);
                                    } else if (titlefieldcount == 3) {
                                        rssdata.setTitle3(title);
                                    }
                                    break;
                                case "summary":

                                    String summary = parser.getText();
                                    //the raw text is parsed from inbetween the (2) set of description tags in the rss file
                                    String cleandsummary = html2text(summary);
                                    //jsoup is then used to strip out the html code

                                    if (summaryfiedcount == 1) {
                                        rssdata.setSummary1(cleandsummary);//call the set method the save the text info in the summary fields
                                    } else if (summaryfiedcount == 2) {
                                        rssdata.setSummary2(cleandsummary);
                                    }

                                    break;
                            }
                        }
                        break;
                }

                eventType = parser.next();

            }
        } catch (Exception e) {
            e.printStackTrace();
            rssdata = null;
        }
        return rssdata;
    }

    //method call to jsoup to strip out html tags and return plain text
    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }

    public static RssData scrapeHalibutBankData(RssData rssData, String content) throws ParserConfigurationException, IOException, SAXException {

        Log.d(TAG, "scrapping web for the current Halibut Bank report");

        boolean webScrapeComplete = false;

            Document doc = Jsoup.parse(content);

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
                Log.d(TAG, "Current report: TitleDataHeader");
                Log.d(TAG, titleData.toString());
                String[] titleDataHeaderTokens = titleData.toString().split(" ");
                Log.d(TAG, "Title token data");
                Log.d(TAG, "----------------");

              String numericTimeToken = null;
              String amPm = null;
              String dateToParse = null;

              for (int i = 0; i < titleDataHeaderTokens.length; i++) {
                    Log.d(TAG, "Token " + i + ": " + titleDataHeaderTokens[i]);
                  if(titleDataHeaderTokens[i].contains("(")){
                      numericTimeToken = titleDataHeaderTokens[i];
                      amPm = titleDataHeaderTokens[i + 1];
                  }
                  if(titleDataHeaderTokens[i].contains("/")){
                      dateToParse = titleDataHeaderTokens[i];
                  }
                }

              String numericTime = numericTimeToken.substring(1, numericTimeToken.length());
              time = setReportTime(numericTime + " " + amPm);
              Log.d(TAG, "Record time: " + time);

              String[] dateElements = dateToParse.split("/");
              int month = Integer.valueOf(dateElements[0]);
              nameOfMonth = getNameOfMonth(month);
              Log.d(TAG, "Month int: " + month + ", Month name: " + nameOfMonth);

              date = nameOfMonth + " " + dateElements[0] + ", " + dateElements[2].substring(0, (dateElements[2].length() - 1));
              Log.d(TAG, "Date: " + date);

                Log.d(TAG, "Current report: Table data");
                Log.d(TAG, "--------------------------");
              for (int i = 0; i < currentReportRows.size(); i++) {
                    Element row = currentReportRows.get(i);
                    Elements cols = row.select("td");
                    String cleanedRow = RssXMLParser.html2text(cols.toString());//clean out the html tags
                    String demilms = "[ ]";
                    String[] tokens = cleanedRow.split(demilms);
                    //iterate through current data in table rows
                    Log.d(TAG, "Row index: " + i);
                    if (i == 1) {
                        winddirection = tokens[3];
                        winddirectiondegrees = tokens[5];
                    }
                    if (i == 2) {
                        windspeed = tokens[3];
                    }
                    if (i == 4) {
                        waveheight = tokens[3];
                    }
                    if (i == 5) {
                        waveinterval = tokens[4];
                    }
                    for (int j = 0; j < tokens.length; j++) {
                        String item = tokens[j];
                        Log.d(TAG, ":  Token " + j + ": " + item);
                    }

                }

               rssData.setDate(date);
               rssData.setTime(time);
               rssData.setWind_direction(winddirection + " " + winddirectiondegrees);
               rssData.setWind_speed(windspeed);
               rssData.setWave_height(waveheight);
               rssData.setWave_interval(waveinterval);

        }//end of scrapeHalib
        return rssData;
    }

    private static String setReportTime(String time) {
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

    private static String getNameOfMonth(int month){
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

}