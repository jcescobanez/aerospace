package com.techawesome.jayce.aeroscape;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class process extends AppCompatActivity {


    String str;

    String departureAirportCode;
    String arrivalAirportCode;
    String departureTimeStamp;
    String arrivalTimeStamp;

    String departureAirportName = "",
            departureLong = "",
            departureLat = "",

    arrivalAirportName = "",
            arrivalLong = "",
            arrivalLat = "";

    String result, timezone, icon, time, temperature, summary, latitude, longitude, humidity, windspeed, status, depflightdetails;

    private static int FIRST_ELEMENT = 0;

    TextView tryjson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);


        Intent intent = getIntent();
        str = intent.getStringExtra("json");
        tryjson = (TextView)findViewById(R.id.json);
        tryjson.setText(str);


        try {



            departureAirportCode = (new JSONObject(str)).getJSONArray("flightStatuses").getJSONObject(FIRST_ELEMENT).getString("departureAirportFsCode");
            arrivalAirportCode = (new JSONObject(str)).getJSONArray("flightStatuses").getJSONObject(FIRST_ELEMENT).getString("arrivalAirportFsCode");
            departureTimeStamp = (new JSONObject(str)).getJSONArray("flightStatuses").getJSONObject(FIRST_ELEMENT).getJSONObject("departureDate").getString("dateLocal");
            arrivalTimeStamp = (new JSONObject(str)).getJSONArray("flightStatuses").getJSONObject(FIRST_ELEMENT).getJSONObject("arrivalDate").getString("dateLocal");
            depflightdetails = (new JSONObject(str)).getJSONObject("appendix").getJSONArray("airlines").getJSONObject(FIRST_ELEMENT).getString("name")
                    + " flight " +
                               (new JSONObject(str)).getJSONObject("appendix").getJSONArray("airlines").getJSONObject(FIRST_ELEMENT).getString("fs")
                    + " " +     (new JSONObject(str)).getJSONObject("request").getJSONObject("flight").getString("interpreted");


            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);

                Date testDate = null;
                Date testDate1 = null;
                testDate = format.parse(departureTimeStamp);
                testDate1 = format.parse(arrivalTimeStamp);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                departureTimeStamp = formatter.format(testDate);
                arrivalTimeStamp = formatter.format(testDate1);

            JSONArray airport = (new JSONObject(str)).getJSONObject("appendix").getJSONArray("airports");
            for(int i=0;i<airport.length();i++) {
                String name = airport.getJSONObject(i).getString("fs");
                if (name.equals(departureAirportCode)){
                    departureAirportName = airport.getJSONObject(i).getString("name");
                    departureLong = airport.getJSONObject(i).getString("longitude");
                    departureLat = airport.getJSONObject(i).getString("latitude");


                }
                else if (name.equals(arrivalAirportCode)){
                    arrivalAirportName = airport.getJSONObject(i).getString("name");
                    arrivalLong = airport.getJSONObject(i).getString("longitude");
                    arrivalLat = airport.getJSONObject(i).getString("latitude");
                }
            }

            tryjson.setText(departureAirportCode + departureAirportName + "    "+arrivalAirportCode + arrivalAirportName + " " + departureTimeStamp + "  " + arrivalTimeStamp);


            status = "1";
            BackgroundTask backgroundTask = new BackgroundTask(this);
            backgroundTask.execute();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public class BackgroundTask extends AsyncTask<String, Void, String> {
        AlertDialog alertdialog;
        Context ctx;
        private Dialog loadingDialog;

        BackgroundTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            alertdialog = new AlertDialog.Builder(ctx).create();
            loadingDialog = ProgressDialog.show(process.this, "", "Loading...");
            super.onPreExecute();


        }


        @Override
        protected String doInBackground(String... params) {

            String weather_url = null;

            if (status.equals("1")) {
                weather_url = "https://api.forecast.io/forecast/e18c1e373524d5cfe9cc36a05a7d6075/" + departureLat + "," + departureLong + "," + departureTimeStamp;
            }
            else if (status.equals("2")){
                weather_url = "https://api.forecast.io/forecast/e18c1e373524d5cfe9cc36a05a7d6075/" + arrivalLat + "," + arrivalLong + "," + arrivalTimeStamp;
            }


            try {
                URL url = new URL(weather_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {

                    response.append(line);
                }
                result = response.toString();
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }



            return result;

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }



        @Override
        protected void onPostExecute(String result) {

            if (result == null) {
                Toast toast = Toast.makeText(process.this, "An error has occurred. Please check your Internet Connection or try again later.",
                        Toast.LENGTH_LONG);
                toast.show();
                loadingDialog.dismiss();
            } else {
                loadingDialog.dismiss();
//                Toast toast = Toast.makeText(process.this, result,
//                        Toast.LENGTH_LONG);
//                toast.show();


                try {

                    JSONObject response = new JSONObject(result);
                    latitude = response.getString("latitude");
                    longitude = response.getString("longitude");
                    timezone = response.getString("timezone");
                    time = (new JSONObject(result)).getJSONObject("currently").getString("time");
                    summary = (new JSONObject(result)).getJSONObject("currently").getString("summary");
                    icon = (new JSONObject(result)).getJSONObject("currently").getString("icon");
                    humidity = (new JSONObject(result)).getJSONObject("currently").getString("humidity");
                    windspeed = (new JSONObject(result)).getJSONObject("currently").getString("windSpeed") + " km/h";
                    temperature = (new JSONObject(result)).getJSONObject("currently").getString("temperature");

                    Double percenthumid = Double.parseDouble(humidity)*100;

                    humidity = percenthumid.toString() + "%";

                    Double fahrenheit = Double.parseDouble(temperature);
                    Double celcius = (fahrenheit - 32) * 5 / 9;

                    Double roundOff = Math.round(celcius * 100.0) / 100.0;
                    temperature = roundOff.toString() + " °C";


                    if (status.equals("1")) {
                        switchimage();
                    }
                    else if (status.equals("2")){
                        switchimage2();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    public void switchimage(){
        final int sdk = android.os.Build.VERSION.SDK_INT;
        final String colorgreen = "#00ff00";
        final String coloryellow = "#ffff00";
        final String colorred = "#ff0000";

        TextView dep_temp = (TextView)findViewById(R.id.dep_temp);
        TextView dep_humid = (TextView)findViewById(R.id.dep_humid);
        TextView dep_windspeed = (TextView)findViewById(R.id.dep_windspeed);
        TextView dep_airport = (TextView)findViewById(R.id.dep_airport);
        TextView dep_date = (TextView)findViewById(R.id.dep_date);
        TextView dep_details = (TextView)findViewById(R.id.depflightdetails);
        dep_details.setText(depflightdetails);

        dep_temp.setText(temperature);
        dep_humid.setText(humidity);
        dep_windspeed.setText(windspeed);
        dep_airport.setText(departureAirportName);



        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

        try {
            Date testDate = null;


            testDate = format.parse(departureTimeStamp);

            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy, hh:mm aaa");
            departureTimeStamp = formatter.format(testDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        dep_date.setText(departureTimeStamp);


        String weatherstatus = icon;
        TextView warning = (TextView)findViewById(R.id.depwarning);

        ImageView img = (ImageView)findViewById(R.id.depimage);

        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (weatherstatus.equalsIgnoreCase("rain")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.rain));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("sleet")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.sleet));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("clear-day")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.clearday));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("clear-night")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.clearnight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("snow")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.sleet));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("wind")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.wind));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("fog")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.fog));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("cloudy")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.cloud));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-day")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.partlycloudy));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-night")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.partlycloudynight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("hail")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.hail));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("thunderstorm")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.thunderstorm));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("tornado")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.tornado));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            }

        } else {

            if (weatherstatus.equalsIgnoreCase("rain")) {
                img.setBackground(getResources().getDrawable(R.drawable.rain));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("sleet")) {
                img.setBackground(getResources().getDrawable(R.drawable.sleet));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("clear-day")) {
                img.setBackground(getResources().getDrawable(R.drawable.clearday));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("clear-night")) {
                img.setBackground(getResources().getDrawable(R.drawable.clearnight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("snow")) {
                img.setBackground(getResources().getDrawable(R.drawable.snow));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("wind")) {
                img.setBackground(getResources().getDrawable(R.drawable.wind));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("fog")) {
                img.setBackground(getResources().getDrawable(R.drawable.fog));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("cloudy")) {
                img.setBackground(getResources().getDrawable(R.drawable.cloud));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-day")) {
                img.setBackground(getResources().getDrawable(R.drawable.partlycloudy));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-night")) {
                img.setBackground(getResources().getDrawable(R.drawable.partlycloudynight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("hail")) {
                img.setBackground(getResources().getDrawable(R.drawable.hail));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("thunderstorm")) {
                img.setBackground(getResources().getDrawable(R.drawable.thunderstorm));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("tornado")) {
                img.setBackground(getResources().getDrawable(R.drawable.tornado));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            }

        }

        status = "2";
        BackgroundTask backgroundTask = new BackgroundTask(this);
        backgroundTask.execute();
    }

    public void switchimage2(){
        final int sdk = android.os.Build.VERSION.SDK_INT;
        final String colorgreen = "#00ff00";
        final String coloryellow = "#ffff00";
        final String colorred = "#ff0000";

        TextView dep_temp = (TextView)findViewById(R.id.arr_temp);
        TextView dep_humid = (TextView)findViewById(R.id.arr_humid);
        TextView dep_windspeed = (TextView)findViewById(R.id.arr_windspeed);
        TextView dep_airport = (TextView)findViewById(R.id.arr_airport);
        TextView dep_date = (TextView)findViewById(R.id.arr_date);
        TextView dep_details = (TextView)findViewById(R.id.arrflightdetails);
        dep_details.setText(depflightdetails);

        dep_temp.setText(temperature);
        dep_humid.setText(humidity);
        dep_windspeed.setText(windspeed);
        dep_airport.setText(arrivalAirportName);


        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

        try {
            Date testDate = null;


            testDate = format.parse(arrivalTimeStamp);

            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy, hh:mm aaa");
            arrivalTimeStamp = formatter.format(testDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        dep_date.setText(arrivalTimeStamp);


        String weatherstatus = icon;
        TextView warning = (TextView)findViewById(R.id.arrwarning);

        ImageView img = (ImageView)findViewById(R.id.arrimage);

        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (weatherstatus.equalsIgnoreCase("rain")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.rain));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("sleet")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.sleet));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("clear-day")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.clearday));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("clear-night")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.clearnight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("snow")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.sleet));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("wind")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.wind));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("fog")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.fog));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("cloudy")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.cloud));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-day")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.partlycloudy));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-night")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.partlycloudynight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("hail")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.hail));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("thunderstorm")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.thunderstorm));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("tornado")) {
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.tornado));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            }

        } else {

            if (weatherstatus.equalsIgnoreCase("rain")) {
                img.setBackground(getResources().getDrawable(R.drawable.rain));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("sleet")) {
                img.setBackground(getResources().getDrawable(R.drawable.sleet));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("clear-day")) {
                img.setBackground(getResources().getDrawable(R.drawable.clearday));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("clear-night")) {
                img.setBackground(getResources().getDrawable(R.drawable.clearnight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("snow")) {
                img.setBackground(getResources().getDrawable(R.drawable.snow));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("wind")) {
                img.setBackground(getResources().getDrawable(R.drawable.wind));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("fog")) {
                img.setBackground(getResources().getDrawable(R.drawable.fog));
                warning.setText("Weather condition might be terrible. We suggest you to contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(coloryellow));
            } else if (weatherstatus.equalsIgnoreCase("cloudy")) {
                img.setBackground(getResources().getDrawable(R.drawable.cloud));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-day")) {
                img.setBackground(getResources().getDrawable(R.drawable.partlycloudy));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("partly-cloudy-night")) {
                img.setBackground(getResources().getDrawable(R.drawable.partlycloudynight));
                warning.setText("Weather seems fine. You may follow your flight schedule accordingly.");
                warning.setTextColor(Color.parseColor(colorgreen));
            } else if (weatherstatus.equalsIgnoreCase("hail")) {
                img.setBackground(getResources().getDrawable(R.drawable.hail));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("thunderstorm")) {
                img.setBackground(getResources().getDrawable(R.drawable.thunderstorm));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            } else if (weatherstatus.equalsIgnoreCase("tornado")) {
                img.setBackground(getResources().getDrawable(R.drawable.tornado));
                warning.setText("Weather condition is terrible. Please contact your Airline Carrier if your flight is affected.");
                warning.setTextColor(Color.parseColor(colorred));
            }

        }
    }




}
