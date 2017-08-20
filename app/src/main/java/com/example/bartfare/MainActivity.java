package com.example.bartfare;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.Button;

import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private static final String[] stat_abbr = {"12th", "16th", "19th", "24th", "ashb",
                                        "balb", "bayf", "cast", "civc", "cols",
                                        "colm", "conc", "daly", "dbrk", "dubl"};

    Spinner departureText, arrivalText;
    TextView responseView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseView = (TextView) findViewById(R.id.responseView);
        departureText = (Spinner)findViewById(R.id.txt_departure);
        arrivalText = (Spinner)findViewById(R.id.txt_arrival);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        Button queryButton = (Button) findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String dept = stat_abbr[departureText.getSelectedItemPosition()];
                String arri = stat_abbr[arrivalText.getSelectedItemPosition()];
                new RetrieveFeedTask(dept,arri,departureText.getSelectedItem().toString(),
                        arrivalText.getSelectedItem().toString()).execute();

            }
        });

    }

    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        String dept, arri, departure, arrival;

        private RetrieveFeedTask(String dept, String arri, String departure, String arrival){
            this.dept = dept;
            this.arri = arri;
            this.departure = departure;
            this.arrival = arrival;
        }

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");

        }

        protected String doInBackground(Void... urls) {

            String YQL = String.format("%s&dest=%s", dept, arri);
            String endpoint = String.format("https://api.bart.gov/api/sched.aspx?cmd=fare&orig=%s&date=today&key=MW9S-E7SL-26DU-VV8V&json=y", YQL);

            // Do some validation here

            try {
                URL url = new URL(endpoint);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    JSONObject root = jsonObject.getJSONObject("root");
                    JSONObject trip = root.getJSONObject("trip");
                    String fare = trip.getString("fare");

                    return "Fare between " + departure + " and " + "\n" + arrival+ " is: " + fare + " dollars.";
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            responseView.setText(response);
            // TODO: check this.exception
            // TODO: do something with the feed

        }
    }
}
