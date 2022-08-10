package com.rsnorrena.westvansurfreport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HttpManager {

    public static String getData(String uri) {

        StringBuilder sb;
        BufferedReader reader = null;
        HttpURLConnection con;
        String fileToReturn = null;

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
        public static String getDocument(String url){
            Document doc = null;

            boolean dataIsDownloaded = false;

            while(!dataIsDownloaded){
                try {
                    //get conditions report for the Halibut Bank buoy.
                    doc = Jsoup.connect("https://www.ndbc.noaa.gov/station_page.php?station=46146").timeout(6000).get();
                    dataIsDownloaded = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return doc.html();
        }
}

