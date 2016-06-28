package com.rsnorrena.westvansurfreport.parsers;

import android.util.Log;
import com.rsnorrena.westvansurfreport.model.RssData;
import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.StringReader;

public class RssXMLParser {

    private static final String TAG = RssXMLParser.class.getSimpleName();

    public static RssData parseFeed(String[] content, int index) {
        //method receives the string value of the xml file(s) in the String array "content"
        // and returns the data file "Rssata".

        RssData rssdata = null;
        //instance of the data object class for the current data object

        boolean inDataItemTag = false;
        //used to determine if we care about the current data item
        String currentTagName = "";
        //which tag we are currently in..

        if (index == 0) {//condition to check the first file for the Halibut Bank data
            Log.d(TAG, "Downloading the Halibut Bank data file");
            try {//the parsing code is surrounded in the try catch
                //creates the object the parse the xml files contained in the array
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(content[0]));//set the input to the first item in the content String array

                int eventType = parser.getEventType();
                //the XmlPullParser generates events, start tag, end tag, text, attributes
                //in this example we only care about the start tags end tags and text events
                int descriptionfieldcount = 0;
                while (eventType != XmlPullParser.END_DOCUMENT) {//loop until the end tag of the xml file

                    switch (eventType) {//switch based on check of the tag name

                        case XmlPullParser.START_TAG:
                            currentTagName = parser.getName();
                            if (currentTagName.equals("description")) {

                                descriptionfieldcount++;

                                if (descriptionfieldcount == 2) {//we are only interested in the data contained with the second set of description tags

                                    inDataItemTag = true;
                                    rssdata = new RssData();//creats the obj to hold the data
                                }


                            }
                            break;

                        case XmlPullParser.END_TAG://when the end tag is reached we reset.
                            if (parser.getName().equals("description")) {
                                inDataItemTag = false;
                            }
                            currentTagName = "";
                            break;

                        case XmlPullParser.TEXT://case to parse text from inbetween the description tag
                            if (inDataItemTag && rssdata != null) {//confirms that we are in a data item we want and that the data obj has been created
                                switch (currentTagName) {

                                    case "description":

                                        String rawdescription = parser.getText();
                                        //the raw text is parsed from inbetween the (2) set of description tags in the rss file
                                        String cleandescription = html2text(rawdescription);
                                        //jsoup is then used to strip out the html code
                                        //jsoup is a special jar library added to the proj for this purpose

                                        String demilms = "[ ]";
                                        String[] tokens = cleandescription.split(demilms);
                                        //the cleaned text is then split into the token array by the deliminator space
                                        //used a for loop to output the index number and token in order to identify the token items of interest

                                        String date = tokens[0] + " " + tokens[1] + " " + tokens[2];
                                        String time = tokens[3] + " " + tokens[4];
                                        String winddirection = tokens[11] + " " + tokens[12];
                                        String windspeed = tokens[15] + " " + tokens[16];
                                        String waveheight = tokens[24];
                                        String waveinterval = tokens[29];

                                        rssdata.setDate(date);//call the the set methods in the data object to save the string data
                                        String[] hour = time.split(":");
                                        int h = Integer.parseInt(hour[0]);
                                        if (time.contains("12:00 am")) {
                                            time = "00:00 am";
                                        }
                                        if (time.contains("pm") && h < 12) {
                                            h = h + 12;
                                            time = h + ":00 pm";
                                        }
                                        //the token items of interest are then saved into the data object.
                                        rssdata.setTime(time);
                                        rssdata.setWind_direction(winddirection);
                                        rssdata.setWind_speed(windspeed);
                                        rssdata.setWave_height(waveheight);
                                        rssdata.setWave_interval(waveinterval);

                                    default:
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
        }

        if (index == 1) {
            Log.d(TAG, "Downloading the wind warning data file");
            //code for parsing the windwarding data
            //data to be collected from xml - title fields 1-3 and summary fields 1 & 2.
            try {

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(content[1]));

                int eventType = parser.getEventType();
                //the XmlPullParser generates events, start tag, end tag, text, attributes
                //in this example we only care about the start tags end tags and text events
                int titlefieldcount = 0;
                int summaryfiedcount = 0;

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
        }
        return rssdata;
    }

    //method call to jsoup to strip out html tags and return plain text
    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }
}
