package com.amg99.flighttime.yvr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;

public class FlightTime extends Activity {

    private static final String TAG = "flighttime.yvr";
    private static final String APPKEY = "2208556623";
    private static final String AIRPORT = "yvr";
    private static final String CHECKURL = "http://flight.amg99.com/%s/%s/%s/%s";
    private static final String PREF_FLIGHTNO = "flightno";
    private static TextView mFlightNoView, mResultView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        FlightTime.mFlightNoView = (TextView) this.findViewById(R.id.textflightNo);
        FlightTime.mResultView = (TextView) this.findViewById(R.id.tvResult);
        
        // filter off the enter key
        FlightTime.mFlightNoView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
                return false;
            }
        });
        
        // Restore last used flight no
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String flightno = settings.getString(PREF_FLIGHTNO, "");
        FlightTime.mFlightNoView.setText(flightno);
    }

    @Override
    protected void onStop(){
       super.onStop();

      String flightno = FlightTime.mFlightNoView.getText().toString();

      // save the flight no for future use
      SharedPreferences settings = getPreferences(MODE_PRIVATE);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString(PREF_FLIGHTNO, flightno);
      editor.commit();
    }

    public void buttonHandler(View view) throws Exception {
        // dismiss the soft keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(FlightTime.mFlightNoView.getWindowToken(), 0);
        
        // put up a in progress message
        FlightTime.mResultView.setText("Checking...");
        
        // start a task to check flight info
        String flightno = FlightTime.mFlightNoView.getText().toString();
        String flighttype = "";
        switch (view.getId()) {
        case R.id.btnArrival:
            flighttype = "a";
            break;
        case R.id.btnDeparture:
            flighttype = "d";
            break; 
        }
        new CheckFlightInfo().execute(flightno, flighttype);
    }

    private class CheckFlightInfo extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String flightno, flighttype;
            String rawline, parsedtext;
            StringBuffer buf;
            URL url;
            URLConnection urlcon;
            BufferedReader in;
            
            flightno = params[0];
            flighttype = params[1];

            buf = new StringBuffer();

            try {
                url = new URL(String.format(CHECKURL, APPKEY, AIRPORT, flighttype, flightno));
                urlcon = url.openConnection();
                in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));

                buf.append("");
                while ((rawline = in.readLine()) != null) {
                    parsedtext = parse_info(rawline);
                    buf.append(parsedtext);
                }
                in.close();
                
            } catch (MalformedURLException e) {
                buf.append(e.toString());
            } catch (IOException e) {
                buf.append(e.toString());                
            } 

            return buf.toString();
        }
        @Override
        protected void onPostExecute(String result) {
            FlightTime.mResultView.setText(result);
        }
    }
    
    public String parse_info(String json) {
        StringBuilder sb = new StringBuilder().append("");
        Gson gson = new Gson();
        String[][] info = gson.fromJson(json, String[][].class);
        for (int i=0; i<info.length; i++) {
            if (sb.length() > 0) {
                sb.append("---\n");    
            }
            for (int j=0; j<info[i].length; j++) {
                sb.append(info[i][j].concat("\n"));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Called only once first time menu is clicked on
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // Called every time user clicks on a menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuAbout:
                intent = new Intent(getApplicationContext(), About.class);
                startActivity(intent);
                break;
        }
        return true;
    }
}
