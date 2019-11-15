package com.example.weather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;import com.example.estudiantes.demoweather.CAFData;


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExecutorService queue = Executors.newSingleThreadExecutor();

    private final static String KEY = "6112087e259146b376c27017bda9667c";
    private final static String DOMAIN = "https://api.openweathermap.org/data/2.5/weather";
    private final static String IMGDOMAIN = "https://openweathermap.org/img/w/";

    private final static String FORMAT = "https://api.openweathermap.org/data/2.5/weather?q=Cali,co&appid=6498e268f2de120d0cd71288c41cbcc6";

    private EditText txtSearch;
    private Button btnSearch;
    private TextView lblCurrent;
    private TextView lblmin;
    private TextView lblmax;

    private TextView textViewWeather;
    private ImageView imgWeather;

    private Button btnGo = null;

    private double lng = 0;
    private double lat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSearch = findViewById(R.id.txtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        lblCurrent = findViewById(R.id.lblCurrent);
        lblmin = findViewById(R.id.lblmin);
        lblmax = findViewById(R.id.lblmax);

        textViewWeather = findViewById(R.id.textViewWeather);
        imgWeather = findViewById(R.id.imgWeather);

        btnGo = findViewById(R.id.btnGo);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = txtSearch.getText().toString();
                search(query);
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lng != 0 && lat != 0) {
                    Uri uri = Uri.parse("geo:" + lat + "," + lng);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                }
            }
        });

    }

    public void search(String query){

        final String queryTmp = query;

        Runnable thread = new Runnable() {
            @Override
            public void run() {
                String strUrl = DOMAIN + "?q=" + queryTmp + "&appid=" + KEY + "&units=metric&lang=es";
                URL url = null;
                CAFData remoteData = null;

                try {
                    url = new URL(strUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                if (url != null){
                    remoteData = CAFData.dataWithContentsOfURL(url);
                    Log.d("DemoWeather", remoteData.toText());

                    try {
                        JSONObject root = new JSONObject(remoteData.toText());
                        JSONObject coord = root.getJSONObject("coord");
                        JSONArray weather = root.getJSONArray("weather");
                        JSONObject main = root.getJSONObject("main");

                        String desc = "";
                        String icon = "";
                        Bitmap bitmap = null;

                        lat = coord.getDouble("lat");
                        lng = coord.getDouble("lon");

                        if(weather.length() > 0){
                            JSONObject aWeather = weather.getJSONObject(0);
                            desc = aWeather.getString("description");
                            icon = aWeather.getString("icon");

                            strUrl = IMGDOMAIN + icon + ".png";
                            url = new URL(strUrl);
                            remoteData = CAFData.dataWithContentsOfURL(url);
                            if(remoteData != null) {
                                bitmap = remoteData.toImage();
                            }
                        }


                        final String descTemp = desc;
                        final Bitmap bitmapTemp = bitmap;
                        final float temp = (float) main.getDouble("temp");
                        final float tempMin = (float) main.getDouble("temp_min");
                        final float tempMax = (float) main.getDouble("temp_max");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lblCurrent.setText(String.valueOf(temp));
                                lblmin.setText(String.valueOf(tempMin));
                                lblmax.setText(String.valueOf(tempMax));
                                textViewWeather.setText(descTemp);
                                imgWeather.setImageBitmap(bitmapTemp);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        queue.execute(thread);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_index, menu);
        return true;
    }
}