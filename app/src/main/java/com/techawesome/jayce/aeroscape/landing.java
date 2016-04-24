package com.techawesome.jayce.aeroscape;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class landing extends AppCompatActivity {


    EditText number, letter;

    String flightno1, flightno2, str_date, name, str_url, str_url2 ,header, result, status;

    GPSTracker gps;
    GPSTracker gps1;

    String latitude;
    String longitude;

    CheckBox date1;
    CheckBox date2;
    CheckBox date3;

    String dt1, dt2, dt3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        number = (EditText)findViewById(R.id.number);
        letter = (EditText)findViewById(R.id.letter);




        //date
        date1 = (CheckBox)findViewById(R.id.date1);
        date2 = (CheckBox)findViewById(R.id.date2);
        date3 = (CheckBox)findViewById(R.id.date3);

        date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date2.setChecked(false);
                date3.setChecked(false);
                str_date = dt1;


            }
        });

        date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date1.setChecked(false);
                date3.setChecked(false);
                str_date = dt2;


            }
        });

        date3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date2.setChecked(false);
                date1.setChecked(false);
                str_date = dt3;


            }
        });

        getCurDate();


        //end of onCreate

        //https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/5J/111/dep/2016/04/24?
        // appId=717865e4&appKey=93155d66e132c05492e2dc9279d7a442&utc=false


        //GPS Get Location




    }


    //getting of current date from the server
    public void getCurDate() {

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(landing.this);
            builder.setTitle("Location Services is not active");
            builder.setMessage("Please enable Location Services and GPS.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog


                    Intent intent = new Intent(landing.this, splash.class);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }

        else{
            gps = new GPSTracker(landing.this);
            gps1 = new GPSTracker(landing.this);

            if(gps.canGetLocation()) {
                latitude = Double.toString(gps.getLatitude());
                longitude = Double.toString(gps1.getLongitude());

            } else {
                gps.showSettingsAlert();
            }
            String method = "gettime";
            status = "gettime";
            BackgroundTask backgroundTask = new BackgroundTask(this);
            backgroundTask.execute(method);
        }



    }


    public void process(View v){
        flightno1 = letter.getText().toString();
        flightno2 = number.getText().toString();

        if (flightno1.equals("")| flightno2.equals("")){
            Toast toast = Toast.makeText(landing.this, "Please enter your flight number to proceed.",
                    Toast.LENGTH_LONG);
            toast.show();
        }

        else{

            if (date1.isChecked()|date2.isChecked()|date3.isChecked()){
                String method = "getjson";
                status = "getjson";
                BackgroundTask backgroundTask = new BackgroundTask(this);
                backgroundTask.execute(method);
            }
            else {
                Toast toast = Toast.makeText(landing.this, "Please select the day of your departure.",
                        Toast.LENGTH_LONG);
                toast.show();
            }


        }



    }

    public class BackgroundTask extends AsyncTask<String,Void,String> {
        AlertDialog alertdialog;
        Context ctx;
        private Dialog loadingDialog;
        BackgroundTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            alertdialog = new AlertDialog.Builder(ctx).create();
            super.onPreExecute();
            loadingDialog = ProgressDialog.show(landing.this, "", "Loading...");

        }


        @Override
        protected String doInBackground(String... params) {



            str_url = "https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/"
                    + flightno1 + "/"
                    + flightno2 + "/dep/"
                    + str_date + "?appId=717865e4&appKey=93155d66e132c05492e2dc9279d7a442&utc=false"
            ;

            str_url2 = "http://api.timezonedb.com/?lat="+latitude+"&lng="+longitude+"&key=PIYBCQDGFI2Q&format=json";


            String method = params[0];
            if (method.equals("getjson")) {



                try {
                    URL url = new URL(str_url);
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

            }

            else if (method.equals("gettime")) {



                try {
                    URL url = new URL(str_url2);
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

            }



            return  result;

        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null){
                Toast toast = Toast.makeText(landing.this, "An error has occurred. Please check your Internet Connection or try again later.",
                        Toast.LENGTH_LONG);
                toast.show();
                loadingDialog.dismiss();
//                toast = Toast.makeText(landing.this, str_url,
//                        Toast.LENGTH_LONG);
//                toast.show();

            }
            else {


                if (status.equals("getjson")) {
                    loadingDialog.dismiss();



                    try {


                        String statuscheck = (new JSONObject(result)).getJSONArray("flightStatuses").toString();

                        if (statuscheck.equals("[]")){
                            Toast toast = Toast.makeText(landing.this, "Invalid Flight Number or no schedule for selected date.",
                            Toast.LENGTH_LONG);
                            toast.show();
                        }
                        else{
                            Intent i = new Intent(landing.this, process.class);
                            i.putExtra("json", result);
                            startActivity(i);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }

                if (status.equals("gettime")) {
                    loadingDialog.dismiss();
//                    Toast toast = Toast.makeText(landing.this, result,
//                            Toast.LENGTH_LONG);
//                    toast.show();

                    try {


                        String stat = (new JSONObject(result)).getString("status");
                        if (stat.equals("OK")){

                            //condition and checkboxes
                            String timestamp = (new JSONObject(result)).getString("timestamp");





                            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");

                            dt1 = df.format(new Date(Long.parseLong(timestamp) * 1000));



                            Calendar c = Calendar.getInstance();
                            try {
                                c.setTime(df.parse(timestamp));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            c.add(Calendar.DATE, 1);
                            dt2 = df.format(c.getTime());
                            c.add(Calendar.DATE, 1);
                            dt3 = df.format(c.getTime());



                            DateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                            try {
                                Date testDate = null;
                                Date testDate2 = null;
                                Date testDate3 = null;
                                testDate = format.parse(dt1);
                                testDate2 = format.parse(dt2);
                                testDate3 = format.parse(dt3);
                                SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                                date1.setText(formatter.format(testDate)+" (Today)");
                                date2.setText(formatter.format(testDate2));
                                date3.setText(formatter.format(testDate3));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }



                            //Toast.makeText(getApplicationContext(), dt1, Toast.LENGTH_LONG).show();
                        }
                        else{
                            //refresh fetching
                            getCurDate();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

        }

    }










}
