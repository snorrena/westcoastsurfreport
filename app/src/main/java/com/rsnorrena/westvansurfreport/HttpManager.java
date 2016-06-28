package com.rsnorrena.westvansurfreport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

}

