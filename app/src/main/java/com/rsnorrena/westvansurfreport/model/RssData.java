package com.rsnorrena.westvansurfreport.model;

public class RssData {

    private String date;
    private String time;
    private String wind_direction;
    private String wind_speed;
    private String wave_height;
    private String wave_interval;
    private String title1;
    private String title2;
    private String title3;
    private String summary1;
    private String summary2;

    public String getSummary1() {
        return summary1;
    }

    public void setSummary1(String summary1) {
        this.summary1 = summary1;
    }

    public String getTitle3() {
        return title3;
    }

    public void setTitle3(String title3) {
        this.title3 = title3;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public String getSummary2() {
        return summary2;
    }

    public void setSummary2(String summary2) {
        this.summary2 = summary2;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWind_direction() {
        return wind_direction;
    }

    public void setWind_direction(String wind_direction) {
        this.wind_direction = wind_direction;
    }

    public String getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(String wind_speed) {
        this.wind_speed = wind_speed;
    }

    public String getWave_height() {
        return wave_height;
    }

    public void setWave_height(String wave_height) {
        this.wave_height = wave_height;
    }

    public String getWave_interval() {
        return wave_interval;
    }

    public void setWave_interval(String wave_interval) {
        this.wave_interval = wave_interval;
    }
}
