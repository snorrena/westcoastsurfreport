package com.rsnorrena.westvansurfreport;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SetUp extends LicenseCheckActivity {//extends the LicenseCheckActivity that is included as a proj library

    private static Context context;
    //declaration of the context variable for this class

    //declaration of the field elements (highlighted purple) to be included in the layout screen
    EditText gmailaddress, gmailpasswor, phonenumber1, phonenumber2, surfcalc_winddir, surfcalc_windspd, surfcalc_waveht, surfgrade_alarm;
    Spinner spinner_number1, spinner_number2;
    Button update, clear;
    CheckBox sendtext;

    String emailaddress, password, phone1, phone2, service_provider_phone1, service_provider_phone2, txtMsgAddress1, txtMsgAddress2;
    int windir, windspd, waveht, surfgradealarm;

    boolean appsettings, datacheck, sendtextmessage;

    TinyDB tdb_setup;
    //declaration for class variables, text fields, checkbox and app preference database


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup);
        //overide of the activity class onCreate method and set up of activity display as per the xml layout file setup.

        context = getApplication();
        //sets the context variable for the application

        checkLicense();
        //call to the check license method of the LicenseCheckActivity
        //This class activity extends LicenseCheckActivity which is included in the app as a library extention

        tdb_setup = new TinyDB(context);
        //init of the database object

        initialize();// call to the class method for initialization of the EditTexts, Spinners and Buttons declared for this activity.


        sendtextmessage = tdb_setup.getBoolean("sendtextmessage");
        //check in the app pref database for a boolean value and set to the sendtextmessage boolean

        if (!sendtextmessage) {
            sendtext.setChecked(!sendtext.isChecked());
        }//the check box for the send text msg is checked by default. The box is then deselected if the sendtextmessage boolean is set to false.

        //pre populate default settings
        if (!appsettings) {
            tdb_setup.putInt("windir", 30);
            tdb_setup.putInt("windspd", 30);
            tdb_setup.putInt("waveht", 40);
            tdb_setup.putInt("surfgradealarm", 100);
        }


        update.setOnClickListener(new View.OnClickListener() {//onclick listener set for the update button
            @Override
            public void onClick(View v) {
                //collect data from edittext fields and save to tdb
                tdb_setup.putBoolean("appsettings", true);

                getEditTextData();//custom method to retrieve all data input into the set up screen ui.

                if (datacheck) {
                    Intent ourIntent = new Intent(SetUp.this, MainActivity.class);
                    ourIntent.setFlags(ourIntent.FLAG_ACTIVITY_CLEAR_TOP);
                    SetUp.this.startActivity(ourIntent);
                    finish();
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                tdb_setup.putBoolean("appsettings", false);
                                Clear_EditText_Fields();

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

            }
        });

        surfcalc_winddir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (surfcalc_winddir.getText().toString().length() == 2) {
                    surfcalc_windspd.requestFocus();
                }

            }
        });

        surfcalc_windspd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (surfcalc_windspd.getText().toString().length() == 2) {
                    surfcalc_waveht.requestFocus();
                }

            }
        });

        surfcalc_waveht.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (surfcalc_waveht.getText().toString().length() == 2) {
                    surfgrade_alarm.requestFocus();
                }

            }
        });

    }

    private void setspinner() {//set of the service provider spinner based on save data in app prefs
        String txtMsgAddress1 = tdb_setup.getString("txtMsgAddress1");
        String txtMsgAddress2 = tdb_setup.getString("txtMsgAddress2");

        String serviceProviderPhone1 = txtMsgAddress1.replaceAll("[0-9]", "");
        String serviceProviderPhone2 = txtMsgAddress2.replaceAll("[0-9]", "");
        Log.d("Phone1", String.valueOf(serviceProviderPhone1));
        Log.d("Phone2", String.valueOf(serviceProviderPhone2));


        if (serviceProviderPhone1.equals("@msg.telus.com")) {
            spinner_number1.setSelection(0);
        } else if (serviceProviderPhone1.equals("@pcs.rogers.com")) {
            spinner_number1.setSelection(1);
        } else if (serviceProviderPhone1.equals("@txt.bell.ca")) {
            spinner_number1.setSelection(2);
        } else if (serviceProviderPhone1.equals(("@fido.ca"))) {
            spinner_number1.setSelection(3);

        }
        if (serviceProviderPhone2.equals("@msg.telus.com")) {
            spinner_number2.setSelection(0);
        } else if (serviceProviderPhone2.equals("@pcs.rogers.com")) {
            spinner_number2.setSelection(1);
        } else if (serviceProviderPhone2.equals("@txt.bell.ca")) {
            spinner_number2.setSelection(2);
        } else if (serviceProviderPhone2.equals(("@fido.ca"))) {
            spinner_number2.setSelection(3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayExistingData();
    }

    private void getEditTextData() {//method to retrieve all data input into the set up screen
        emailaddress = gmailaddress.getText().toString();
        password = gmailpasswor.getText().toString();
        phone1 = phonenumber1.getText().toString();
        phone2 = phonenumber2.getText().toString();
        service_provider_phone1 = spinner_number1.getSelectedItem().toString();
        service_provider_phone2 = spinner_number2.getSelectedItem().toString();
        sendtextmessage = sendtext.isChecked();
        windir = Integer.parseInt(surfcalc_winddir.getText().toString());
        windspd = Integer.parseInt(surfcalc_windspd.getText().toString());
        waveht = Integer.parseInt(surfcalc_waveht.getText().toString());
        surfgradealarm = Integer.parseInt(surfgrade_alarm.getText().toString());

        datacheck = true;//set boolean initially assuming all set up data input is correct
        if (phone1.length() < 10 && phone1.length() != 0) {//start of check on set up data input
            datacheck = false;

        }
        if (phone2.length() < 10 && phone2.length() != 0) {
            datacheck = false;
        }

        if (phone1.length() == 0 && phone2.length() == 0 && sendtext.isChecked()) {
            datacheck = false;
        }

        int x = windir + windspd + waveht;//the value of the entered surf grade variables should total 100.
        if (x != 100) {
            datacheck = false;
        }

        String emailcheck = emailaddress.replace("@gmail.com", "");

        if (emailcheck.length() == 0 || password.length() == 0) {
            datacheck = false;
        }

        Log.d("Phone str lngth ", String.valueOf(phone1.length()));
        Log.d("Value of x", String.valueOf(x));
        Log.d("Boolean x", String.valueOf(datacheck));

        if (datacheck) {//if the datacheck is good create the phone to e-mail address using the correct service provider address

            txtMsgAddress1 = "";
            txtMsgAddress2 = "";

            if (!phone1.isEmpty()) {
                if (service_provider_phone1.equals("Telus")) {
                    txtMsgAddress1 = phone1 + "@msg.telus.com";
                } else if (service_provider_phone1.equals("Rogers")) {
                    txtMsgAddress1 = phone1 + "@pcs.rogers.com";
                } else if (service_provider_phone1.equals("Bell")) {
                    txtMsgAddress1 = phone1 + "@txt.bell.ca";
                } else if (service_provider_phone1.equals("Fido")) {
                    txtMsgAddress1 = phone1 + "@fido.ca";
                }
            }

            if (!phone2.isEmpty()) {
                if (service_provider_phone2.equals("Telus")) {
                    txtMsgAddress2 = phone2 + "@msg.telus.com";
                } else if (service_provider_phone2.equals("Rogers")) {
                    txtMsgAddress2 = phone2 + "@pcs.rogers.com";
                } else if (service_provider_phone2.equals("Bell")) {
                    txtMsgAddress2 = phone2 + "@txt.bell.ca";
                } else if (service_provider_phone2.equals("Fido")) {
                    txtMsgAddress2 = phone2 + "@fido.ca";
                }
            }

            tdb_setup.putString("emailaddress", emailaddress);//saved all set up data into app preferences
            tdb_setup.putString("password", password);
            tdb_setup.putString("phone1", phone1);
            tdb_setup.putString("phone2", phone2);
            tdb_setup.putString("txtMsgAddress1", txtMsgAddress1);
            Log.d("Phone1", phone1);
            Log.d("Spinner1", service_provider_phone1);
            Log.d("textMsgAdd1", txtMsgAddress1);
            tdb_setup.putString("txtMsgAddress2", txtMsgAddress2);
            tdb_setup.putBoolean("sendtextmessage", sendtextmessage);
            tdb_setup.putInt("windir", windir);
            tdb_setup.putInt("windspd", windspd);
            tdb_setup.putInt("waveht", waveht);
            tdb_setup.putInt("surfgradealarm", surfgradealarm);
        } else {
            Toast toast = Toast.makeText(context, "Error!  Please check input data.", Toast.LENGTH_LONG);//error message displayed if set up data is incorrect.
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void DisplayExistingData() {//sets layout fields to data saved in app prefs

        setspinner();//custom method sets the service provider spinner based on data saved in app prefs
        //retrieve saved set data saved in app prefs
        emailaddress = tdb_setup.getString("emailaddress");
        password = tdb_setup.getString("password");
        phone1 = tdb_setup.getString("phone1");
        phone2 = tdb_setup.getString("phone2");
        windir = tdb_setup.getInt("windir");
        windspd = tdb_setup.getInt("windspd");
        waveht = tdb_setup.getInt("waveht");
        surfgradealarm = tdb_setup.getInt("surfgradealarm");
        //set layout text fields to display set up data pulled from app prefs
        gmailaddress.setText(emailaddress);
        gmailpasswor.setText(password);
        phonenumber1.setText(phone1);
        phonenumber2.setText(phone2);
        surfcalc_winddir.setText(Integer.toString(windir));
        surfcalc_windspd.setText(Integer.toString(windspd));
        surfcalc_waveht.setText(Integer.toString(waveht));
        surfgrade_alarm.setText(Integer.toString(surfgradealarm));
    }

    private void Clear_EditText_Fields() {//method to clear data from the app prefs database and the application set up layout screen

        tdb_setup.putString("emailaddress", "");
        tdb_setup.putString("password", "");
        tdb_setup.putString("phone1", "");
        tdb_setup.putString("phone2", "");
        tdb_setup.putInt("windir", 0);
        tdb_setup.putInt("windspd", 0);
        tdb_setup.putInt("waveht", 0);
        tdb_setup.putInt("surfgradealarm", 0);

        gmailaddress.setText("@gmail.com");
        gmailpasswor.setText("");
        phonenumber1.setText("");
        phonenumber2.setText("");
        surfcalc_winddir.setText("0");
        surfcalc_windspd.setText("0");
        surfcalc_waveht.setText("0");
        surfgrade_alarm.setText("80");

        sendtextmessage = tdb_setup.getBoolean("sendtextmessage");

        if (sendtextmessage) {//deselect the send message checkbox if it is currently selected
            sendtext.setChecked(false);
            tdb_setup.putBoolean("sendtextmessage", false);
        }

    }

    private void initialize() {//initialize text fields and buttons by reference to the ids set up in the xml layout file.
        gmailaddress = (EditText) findViewById(R.id.etprimary_email);
        gmailpasswor = (EditText) findViewById(R.id.etprimary_password);
        phonenumber1 = (EditText) findViewById(R.id.etphone_number1);
        phonenumber2 = (EditText) findViewById(R.id.etphone_number2);
        surfcalc_winddir = (EditText) findViewById(R.id.etsurfcalc_winddir);
        surfcalc_windspd = (EditText) findViewById(R.id.etsurfcal_windspd);
        surfcalc_waveht = (EditText) findViewById(R.id.etsurfcsl_waveht);
        surfgrade_alarm = (EditText) findViewById(R.id.etsurfgrade);

        update = (Button) findViewById(R.id.bsettings_update);
        update.getBackground().setAlpha(64);//set of the alpha of the button to show through to background image
        clear = (Button) findViewById(R.id.bsettings_cleardata);
        clear.getBackground().setAlpha(64);

        spinner_number1 = (Spinner) findViewById(R.id.spinner1);
        spinner_number2 = (Spinner) findViewById(R.id.spinner2);

        sendtext = (CheckBox) findViewById(R.id.cb_SetUp);

        appsettings = false;

        try {
            appsettings = tdb_setup.getBoolean("appsettings");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (appsettings) {//set all layout fields if set up data already stored in app prefs
            DisplayExistingData();
        }


    }
}
