package com.rsnorrena.westvansurfreport;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class WindForecast extends Activity {

    TextView title1, title2, title3, summary1, summary2;
    TinyDB tdb;

    ArrayList<String> windforecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.windforecast);


        title1 = (TextView) findViewById(R.id.tvtitle1);
        title2 = (TextView) findViewById(R.id.tvtitle2);
        title3 = (TextView) findViewById(R.id.tvtitle3);
        summary1 = (TextView) findViewById(R.id.tvsummary1);
        summary2 = (TextView) findViewById(R.id.tvsummary2);

        tdb = new TinyDB(this);

        windforecast = tdb.getList("windforecast");
        int recordssaved = tdb.getInt("recordssaved");

        if (recordssaved != 0) {
            title1.setText(windforecast.get(0));
            title2.setText(windforecast.get(1));
            title3.setText(windforecast.get(2));
            summary1.setText(windforecast.get(3));
            summary2.setText(windforecast.get(4));
        }

    }
}
