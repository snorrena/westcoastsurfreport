package com.rsnorrena.westvansurfreport;

import android.util.Log;

public class SendAlarmMessage {

    private TinyDB tinydb;
    private String surfreport;
    private String date;
    private String time;
    private String winddirection;
    private String windspeed;
    private String waveheight;
    private String waveinterval;
    private String winddirectiondegrees;

    public SendAlarmMessage(TinyDB tinydb, String surfreport, String date, String time, String winddirection, String windspeed,
                            String waveheight, String waveinterval, String winddirectiondegrees) {
        this.tinydb = tinydb;
        this.surfreport = surfreport;
        this.date = date;
        this.time = time;
        this.winddirection = winddirection;
        this.windspeed = windspeed;
        this.waveheight = waveheight;
        this.waveinterval = waveinterval;
        this.winddirectiondegrees = winddirectiondegrees;
    }

    public void sendEmailMessage() {
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {

                    String emailaddress = tinydb.getString("emailaddress");
                    String password = tinydb.getString("password");

                    String phone1 = tinydb.getString("txtMsgAddress1");
                    String phone2 = tinydb.getString("txtMsgAddress2");

                    int totalphonenumbers = 0;//sets the array of phone numbers to be passed to the mail code
                    if (phone1.length() != 0) {//the array can only be one or two if the sendtextmessage boolean is true
                        totalphonenumbers = totalphonenumbers + 1;
                    }
                    if (phone2.length() != 0) {
                        totalphonenumbers = totalphonenumbers + 1;
                    }
                    String[] phonenumbers = new String[totalphonenumbers];//string array created based on number of phone #s

                    if (totalphonenumbers == 1) {//sets the first item of the phone # array to the single saved phone #
                        if (phone1.length() != 0) {
                            phonenumbers[0] = phone1;
                        } else {
                            phonenumbers[0] = phone2;
                        }
                    } else {
                        phonenumbers[0] = phone1;//sets two separate phone #s to idex items 0 and 1 in the phone numbers array
                        phonenumbers[1] = phone2;
                    }


                    Mail m = new Mail(emailaddress, password);//creates and instance of the mail obj passing in the email adr and psw

                    String[] toArr = phonenumbers;//assigns the phone # array to the string array toArr
//                    String[] toArr = {"snorrena@gmail.com", "snorrena@hotmail.com"};
                    m.set_to(toArr);//call to the set_to method of the Mail obj passed in the toArr array of phone numbers
                    m.set_from(emailaddress);//call to the set_from method in the mail obj passed in the String array list of email addresses
                    m.set_subject("West Vancouver Surf Report: " + date + ", " + time);//call to the set_subject method
                    String wd = winddirection.replaceAll("[^a-zA-Z0-9()]", "");
                    String emailbody = surfreport + " Current conditions: " + "Wind: " + wd + ", " + windspeed + ", Waves: " + waveheight + " ft. @ " + waveinterval + " second intervals.";
                    m.setBody(emailbody);//call to the set body method passing the e-mail msg

                    try {
//                    m.addAttachment("/sdcard/filelocation");

                        if (m.send()) {//call the the mail obj send method.
//                                    Toast.makeText(PassedContext, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                            Log.d("Email thread", "Msg sent");
                        } else {
//                                    Toast.makeText(PassedContext, "Email was not sent.", Toast.LENGTH_LONG).show();
                            Log.d("Email thread", "Msg not sent");
                        }
                    } catch (Exception e) {
                        //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                        Log.e("MailApp", "Could not send email", e);
                    }

                } finally {

                }
            }
        };
        thread.start();


    }//end of send mail code


}
