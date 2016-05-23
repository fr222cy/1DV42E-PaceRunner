package se.filiprydberg.pacerunner.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

import se.filiprydberg.pacerunner.Services.LocationService;
import se.filiprydberg.pacerunner.R;

public class SessionActivity extends AppCompatActivity implements OnMapReadyCallback {
    private LatLng currentCoordinates;
    private LatLng previousCoorinates;
    private LatLng startPosition;
    private MarkerOptions positionMarker;
    private Marker marker;
    private GoogleMap map;
    private TextView time_spent;
    private Chronometer timer;
    private ServiceReceiver serviceReceiver;

    private TextView totalDistanceView;
    private TextView averagePaceView;
    private TextView gapToPaceView;

    private static int[] userSubmittedAveragePace;
    private static int REQUIRED_METERS_BEFORE_SHOWING_AVGPACE = 50;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        totalDistanceView = (TextView) findViewById(R.id.distance);
        averagePaceView = (TextView) findViewById(R.id.average_pace);
        gapToPaceView = (TextView) findViewById(R.id.gap_to_pace);

        startTimer();
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.MY_ACTION);
        registerReceiver(serviceReceiver, intentFilter);

        Intent previousIntent = getIntent();

        startPosition = new LatLng(previousIntent.getDoubleExtra("LATITUDE",0),
                                   previousIntent.getDoubleExtra("LONGITUDE",0));

        userSubmittedAveragePace = new int[] {previousIntent.getIntExtra("MINUTE", 0), previousIntent.getIntExtra("SECOND", 0)};

        positionMarker = new MarkerOptions()
                .position(startPosition);
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
        private float totalDistanceMeters;

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
                setTotalDistance(previousCoorinates, currentCoordinates);
                int[] averagePace = getAveragePace();
                int[] gap = getGapToAveragePace(averagePace);
                updateViewStatistics(averagePace, gap);
                previousCoorinates = new LatLng(currentCoordinates.latitude,currentCoordinates.longitude);
            }
            else
            {
                previousCoorinates = currentCoordinates;
            }
        }

        //Sets the total distance in meters that has been run during the session.
        private void setTotalDistance(LatLng prevCoords, LatLng curCoords){
            float[] results = new float[1];
            Location.distanceBetween(prevCoords.latitude, prevCoords.longitude,
                                     curCoords.latitude, curCoords.longitude, results);

            for (float i : results){
                totalDistanceMeters += i;
            }
        }

        //Sets the average pace, based on the distance and elapsed time.
        //Using Global variable "timer"
        private int[] getAveragePace(){
            long timeElapsed = SystemClock.elapsedRealtime() - timer.getBase();
            int seconds = (int)timeElapsed / 1000;

            double totalKilometer = totalDistanceMeters / 1000;
            double secondPerKilometer = seconds / totalKilometer;
            int minute = (int)(Math.floor(secondPerKilometer/60));
            int second = (int)secondPerKilometer % 60;

            return new int[] {minute,second};
        }

        private int[] getGapToAveragePace(int[] averagePace){
            SimpleDateFormat format = new SimpleDateFormat("mm:ss");

            try{
                Date averagePaceFormatted = format.parse(String.valueOf(averagePace[0])+":"+String.valueOf(averagePace[1]));
                Date userSubmittedAveragePaceFormatted = format.parse(String.valueOf(userSubmittedAveragePace[0])+":"+String.valueOf(userSubmittedAveragePace[1]));

                long diff = averagePaceFormatted.getTime() - userSubmittedAveragePaceFormatted.getTime();

                int minDiff = (int)diff / (60 * 1000) % 60;
                int secDiff = (int)diff / 1000 % 60;

                return new int[] {minDiff, secDiff};
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return new int[] {0,0};
        }
        //Using Google Maps to draw the route that has been run.
        private void drawRoute(LatLng prevCoords, LatLng curCoords){
            map.addPolyline(new PolylineOptions()
                    .add(prevCoords, curCoords)
                    .width(5)
                    .color(Color.rgb(255, 165, 0)));
        }

        //Updates the view with the Session stats
        private void updateViewStatistics(int[] averagePace, int[] gap){
            totalDistanceView.setText(String.format("%.2f", totalDistanceMeters / 1000) + "Km");

            if(totalDistanceMeters >= REQUIRED_METERS_BEFORE_SHOWING_AVGPACE){
                String avgMinute = String.valueOf(averagePace[0]);
                String avgSecond = String.valueOf(averagePace[1]);
                String gapMinute = String.valueOf(gap[0]);
                String gapSecond = String.valueOf(gap[1]);

                if(averagePace[0] < 10){
                     avgMinute = "0" + avgMinute;
                }
                if(averagePace[1] < 10){
                     avgSecond = "0" + avgSecond;
                }
                if(gap[0] >= 0 && gap[0] < 10){
                    gapMinute = "0" + gapMinute;
                }
                if(gap[1] >= 0 && gap[1] < 10 ){
                    gapSecond = "0" + gapSecond;
                }
                if(gap[0] < 0 || gap[1] < 0){
                    gapMinute = String.valueOf(Math.abs(gap[0]));
                    gapSecond = String.valueOf(Math.abs(gap[1]));

                    gapToPaceView.setText("-"+gapMinute+":"+gapSecond);
                }
                else{
                    gapToPaceView.setText("+"+gapMinute+":"+gapSecond);
                }
                averagePaceView.setText(avgMinute+":"+avgSecond);
            }
            else {
                averagePaceView.setText("Calculating...");
                gapToPaceView.setText("Calculating...");
            }
        }
    }
}
