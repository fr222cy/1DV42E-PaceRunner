package se.filiprydberg.pacerunner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class Runner extends AppCompatActivity implements OnMapReadyCallback {
    private LatLng currentCoordinates;
    private LatLng previousCoorinates;
    private LatLng startPosition;
    private MarkerOptions positionMarker;
    private Marker marker;
    private GoogleMap map;
    private TextView time_spent;
    private Chronometer timer;
    private ServiceReceiver serviceReceiver;
    private TextView test;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        test = (TextView) findViewById(R.id.average_pace);
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.MY_ACTION);
        registerReceiver(serviceReceiver, intentFilter);

        Intent previousIntent = getIntent();

        startPosition = new LatLng(previousIntent.getDoubleExtra("LATITUDE",0),
                                   previousIntent.getDoubleExtra("LONGITUDE",0));

        positionMarker = new MarkerOptions()
                .position(startPosition);

        startTimer();

    }


    public void onMapReady(GoogleMap googleMap){
        map = googleMap;
        marker = map.addMarker(positionMarker);
        marker.setPosition(startPosition);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 17));
    }

    private void startTimer(){
        timer = (Chronometer) findViewById(R.id.chronometer);

        timer.start();
    }


    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            double latitude = arg1.getDoubleExtra("LATITUDE",0);
            double longitude = arg1.getDoubleExtra("LONGITUDE", 0);

            if(longitude != 0 && latitude != 0){
                currentCoordinates = new LatLng(latitude,longitude);
                marker.setPosition(currentCoordinates);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 17));
            }

            if(previousCoorinates != null){
                drawRoute(previousCoorinates, currentCoordinates);
                previousCoorinates = new LatLng(currentCoordinates.latitude,currentCoordinates.longitude);
            }
            else
            {
                previousCoorinates = currentCoordinates;
            }


            test.setText(String.valueOf(latitude));

        }

        public void drawRoute(LatLng previousPosition, LatLng currentPosition){

            Polyline line = map.addPolyline(new PolylineOptions()
                    .add(previousPosition, currentPosition)
                    .width(5)
                    .color(Color.YELLOW));

        }



    }




}
