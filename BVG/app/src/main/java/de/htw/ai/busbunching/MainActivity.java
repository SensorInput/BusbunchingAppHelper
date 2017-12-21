package de.htw.ai.busbunching;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button button_stop;
    private TextView text;
    //Location service
    private LocationManager locationManager;
    //Listen for location changes
    private LocationListener listener;
    private String URL_ADDRESS = "http://h2650399.stratoserver.net:4545/position";
    private int ID = 1;
    private Date time = Calendar.getInstance().getTime();
    private EditText journey;
    private int journeyID = 0;




    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.Start_button);
        button_stop = (Button) findViewById(R.id.Stop_button);
        journey = (EditText) findViewById(R.id.JourneyEditText);

        //Location via GPS anfragen ist ein System Service
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {

            //Immer wenn de Location upgedatet wird
            @Override
            public void onLocationChanged(Location location) {
                text.append("\n "  + location.getLatitude()+" " + location.getLongitude());
                //text.setText("\n " + location.getLongitude() + " " + location.getLatitude());
                try {
                    sendPost(location);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            //Falls GPS aus ist wird der user zu den Location settings geführt
            @Override
            public void onProviderDisabled(String s) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        configure_button();
    }

    //Locationmanager abzuschalten
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button(){


        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDestroy();
            }
        });


        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //String array mit Permission die wir in die AndroidManifest datei geschrieben haben, request code kann eine randomnummer sein, wichtig aber fuer "onRequestPermissionResult
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                journeyID = Integer.valueOf(journey.getText().toString());
                //minTime = wie oft sollen neue GPS daten geholt werden, minDistance bei welcher entfernung sollen neue GPS daten geholt werden
                locationManager.requestLocationUpdates("gps", 10000, 0, listener);
            }
        });
    }

    private static AsyncHttpClient httpClient = new AsyncHttpClient();

    public void sendPost(final Location location) throws JSONException, UnsupportedEncodingException {
        JSONObject jsonParam = new JSONObject();
        JSONObject geoJsonObject = new JSONObject();
        geoJsonObject.put("lng", location.getLongitude());
        geoJsonObject.put("lat", location.getLatitude());
        jsonParam.put("lngLat", geoJsonObject);
        jsonParam.put("journeyId", journeyID);
        jsonParam.put("time", System.currentTimeMillis());

        httpClient.post(getBaseContext(), URL_ADDRESS, new StringEntity(jsonParam.toString()), "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("Post success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                System.out.println("Failed success " + statusCode);
            }
        });
    }

}
