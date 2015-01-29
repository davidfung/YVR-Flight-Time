package com.amg99.flighttime.yvr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class About extends Activity {
    //private static final String website = "http://amg99.com/flighttimeyvr.html";
    private static final String website = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=5MA2UFJN6GE5C";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
    
    public void aboutButtonHandler(View view) throws Exception {
        switch (view.getId()) {
        case R.id.aboutButton:
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
            startActivity(intent);
            break;
        }
    }
}
