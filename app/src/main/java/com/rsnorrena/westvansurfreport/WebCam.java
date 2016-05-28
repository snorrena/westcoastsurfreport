package com.rsnorrena.westvansurfreport;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by Admin on 4/27/2015.
 */
public class WebCam extends Activity {
    String appWebViewTempUrl;
    WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webcam);
        wv = (WebView) findViewById(R.id.webView);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.loadUrl("http://www.kiteboardbc.com/webcam/westvan/");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "called");
            wv.loadUrl("about:blank");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "called");

            wv.loadUrl("http://www.kiteboardbc.com/webcam/westvan/");

    }
}
