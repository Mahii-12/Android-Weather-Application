package com.example.weatherapp;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView cityname;
    TextView txtDateTime;
    TextView txtTemperature;
    TextView txtWeatherCondition;
    Button AddnewCity;
    String locationName = "";
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    ImageView img1,img2,img3;
    private static final int PERMISSION_REQUEST_CODE = 1;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setInitialization();
        getLocation();
    }

    public void setInitialization(){
        cityname = (TextView) findViewById(R.id.cityname);
        txtTemperature = (TextView) findViewById(R.id.txtTemperature);
        txtDateTime = (TextView) findViewById(R.id.txtDateTime);
        txtWeatherCondition = (TextView) findViewById(R.id.txtWeatherCondition);
        AddnewCity=(Button) findViewById(R.id.AddnewCity);
        img1=(ImageView) findViewById(R.id.img1);
        img2=(ImageView) findViewById(R.id.img2);
        img3=(ImageView) findViewById(R.id.img3);

    }

    protected void callWeatherApi() {

        String query = "select*from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"(" + (locationName.equals("") ? "23.777176,90.399452" : locationName) + ")\")";
        String format = "json";
        WeatherApiProvider.getApiClient().getWeatherQuery(query, format).enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                locationName = "";
                try {
                    cityname.setText(response.body().getQuery().getResults().getChannel().getLocation().getCity().trim().toUpperCase());
                    txtTemperature.setText((Integer.parseInt(response.body().getQuery().getResults().getChannel().getItem().getCondition().getTemp()) - 32) * 5 / 9 + "");
                    txtWeatherCondition.setText(response.body().getQuery().getResults().getChannel().getItem().getCondition().getText() + "\nWind: " + response.body().getQuery().getResults().getChannel().getWind().getSpeed() + " mph\nHumidity: " + response.body().getQuery().getResults().getChannel().getAtmosphere().getHumidity() + "%");
                    Picasso.with(MainActivity.this).load("http://l.yimg.com/a/i/us/we/52/" + response.body().getQuery().getResults().getChannel().getItem().getCondition().getCode() + ".gif").into(img1,img2,img3);
                } catch (NullPointerException e) {

                }
            }
            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getLocation() {
        if (checkPermission()) {
            getLocationData();
        } else {
            if (ActivityCompat.checkSelfPermission(this, mPermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{mPermission}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private boolean checkPermission() {
        int result = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void getLocationData() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            callWeatherApi();
        } else {
            SmartLocation.with(this).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            try {
                                locationName = location.getLatitude() + "," + location.getLongitude();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            callWeatherApi();
                        }
                    });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationData();
                } else {
                    callWeatherApi();
                }
                break;
        }
    }
}