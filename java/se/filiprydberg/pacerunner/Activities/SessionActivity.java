package se.filiprydberg.pacerunner.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import se.filiprydberg.pacerunner.NotificationSounds;
import se.filiprydberg.pacerunner.Services.LocationService;
import se.filiprydberg.pacerunner.R;

public class SessionActivity extends AppCompatActivity implements OnMapReadyCallback {
    private LatLng currentCoordinates;
    private LatLng previousCoorinates;
    private LatLng startPosition;
    private MarkerOptions positionMarker;
    private Marker marker;
    protected GoogleMap map;
    private Chronometer timer;
    private ServiceReceiver serviceReceiver;

    private TextView totalDistanceView;
    private TextView averagePaceView;
    private TextView gapToPaceView;
    private Button pauseButton;
    private Button finishButton;
    private NotificationSounds sounds = new NotificationSounds();

    private int[] userSubmittedAveragePace;
    private int REQUIRED_METERS_BEFORE_SHOWING_AVGPACE;
    private int AMOUNT_OF_SECONDS_WHEN_TRIGGERING_NOTIFICATION;
    private String NOTIFICATION_SOUND;
    private boolean isPaused = false;
    private long timeElapsed;
    private List<Polyline> path;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Setting Views
        totalDistanceView = (TextView) findViewById(R.id.distance);
        averagePaceView = (TextView) findViewById(R.id.average_pace);
        gapToPaceView = (TextView) findViewById(R.id.gap_to_pace);
        pauseButton = (Button) findViewById(R.id.discardButton);
        finishButton = (Button) findViewById(R.id.saveButton);
        //Start the Chronometer
        startTimer();
        //Retrieve the user-settings
        getSettings();

        //Retrieve the data from the previous activity.
        Intent previousIntent = getIntent();
        startPosition = new LatLng(previousIntent.getDoubleExtra("LATITUDE",0),
                previousIntent.getDoubleExtra("LONGITUDE",0));
        NOTIFICATION_SOUND = previousIntent.getStringExtra("NOTIFICATION_SOUND");
        userSubmittedAveragePace = new int[] {previousIntent.getIntExtra("MINUTE", 0), previousIntent.getIntExtra("SECOND", 0)};

        //Initialize the sounds chosen.
        sounds.setMediaPlayerSounds(NOTIFICATION_SOUND, getApplicationContext());

        //Initialize the ServiceReceiver.
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.MY_ACTION);
        registerReceiver(serviceReceiver, intentFilter);

        //Pause and Finish button functionality
        onClicks(this);
        //Place a marker on the users startPosition.
        positionMarker = new MarkerOptions()
                .position(startPosition);
    }
    /*
    User settings. Use default value if none a set.
     */
    private void getSettings(){
        SharedPreferences sp = getSharedPreferences("PACERUNNER_SETTINGS", Activity.MODE_PRIVATE);
        AMOUNT_OF_SECONDS_WHEN_TRIGGERING_NOTIFICATION = sp.getInt("NOTIFICATION_SENSITIVITY", -15);
        REQUIRED_METERS_BEFORE_SHOWING_AVGPACE = sp.getInt("NOTIFICATION_METERS_BEFORE_START", 500);
    }

    /*
    Gets a dialog based on the type.
    0 - Finished Dialog
    1 - Back Dialog
     */
    private AlertDialog getDialog(int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(type == 0){
            final Intent summary = new Intent(this, SummaryActivity.class);
            builder.setMessage("Press proceed to see your session summary.")
                    .setTitle("Are you done?");
            // Add the buttons
            builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(summary);
                    finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
        }
        else if(type == 1){
            builder.setMessage("You are about to exit and end this session, no data will be saved.")
                    .setTitle("Exit?");
            // Add the buttons
            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.setNegativeButton("Continue Running", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
        }


        return builder.create();
    }

    public void onClicks(SessionActivity currentActivity){
        final SessionActivity activity = currentActivity;
        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (!isPaused) {
                    stopService(new Intent(activity, LocationService.class));
                    previousCoorinates = null;
                    timer.stop();
                    isPaused = true;
                    pauseButton.setText("RESUME");
                } else {
                    startService(new Intent(activity, LocationService.class));

                    timer.setBase(SystemClock.elapsedRealtime() - timeElapsed);
                    timer.start();
                    isPaused = false;
                    pauseButton.setText("PAUSE");
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                getDialog(0).show();
            }
        });
    }

    public void onBackPressed() {
        getDialog(1).show();
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
        private int secondsUnderPace;
        private int secondsOverPace;
        private boolean notifiedUnderPace = false;
        private boolean isOverPace;


        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if(isPaused){
                previousCoorinates = null;
            }
            double latitude = arg1.getDoubleExtra("LATITUDE",0);
            double longitude = arg1.getDoubleExtra("LONGITUDE", 0);
            //If the Latlong is legit.
            if(longitude != 0 && latitude != 0){
                currentCoordinates = new LatLng(latitude,longitude);
                marker.setPosition(currentCoordinates);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 17));
            }

            //Call all methods when the app has fetched the second coords.
            if(previousCoorinates != null ){
                drawRoute(previousCoorinates, currentCoordinates);
                setTotalDistance(previousCoorinates, currentCoordinates);
                int[] averagePace = getAveragePace();
                int[] gap = getGapToAveragePace(averagePace);


                if(isRequiredMetersComplete()){
                    if(!isPaused){
                        sounds.playSoundByType(getWhatSoundThatShouldBePlayed(gap[2]));
                    }else{
                        sounds.playSoundByType(0);
                    }
                }
                else{
                    //If no notification should be given, send in 0(No sound) as value.
                    sounds.playSoundByType(0);
                }

                updateViewStatistics(averagePace, gap);
                previousCoorinates = new LatLng(currentCoordinates.latitude,currentCoordinates.longitude);
            }
            else{
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
        //Using chronometer variable "timer"
        private int[] getAveragePace(){
            timeElapsed = SystemClock.elapsedRealtime() - timer.getBase();
            int seconds = (int)timeElapsed / 1000;

            double totalKilometer = totalDistanceMeters / 1000;
            double secondPerKilometer = seconds / totalKilometer;
            int minute = (int)(Math.floor(secondPerKilometer/60));
            int second = (int)secondPerKilometer % 60;

            return new int[] {minute,second};
        }
        /*
        Return: int[] with min- and sec difference formatted. and a third value
        with seconds not formatted. ex. if gap is 1:30: {1, 30, 90}
         */
        private int[] getGapToAveragePace(int[] averagePace){
            SimpleDateFormat format = new SimpleDateFormat("mm:ss");

            try{
                Date averagePaceFormatted = format.parse(String.valueOf(averagePace[0])+":"+String.valueOf(averagePace[1]));
                Date userSubmittedAveragePaceFormatted = format.parse(String.valueOf(userSubmittedAveragePace[0])+":"+String.valueOf(userSubmittedAveragePace[1]));

                long diff = averagePaceFormatted.getTime() - userSubmittedAveragePaceFormatted.getTime();

                int minDiff = (int)diff / (60 * 1000) % 60;
                int secDiff = (int)diff / 1000 % 60;

                //Using secDiffNoFormat to make it easier to calculate for the notification.
                int secDiffNoFormat = (int)diff / 1000;
                return new int[] {minDiff, secDiff, secDiffNoFormat};
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return new int[] {0,0};
        }
        //Using Google Maps to draw the route that has been run.
        private void drawRoute(LatLng prevCoords, LatLng curCoords) {

            if(!isRequiredMetersComplete()){
                map.addPolyline(new PolylineOptions()
                        .add(prevCoords, curCoords)
                        .width(10)
                        .color(getResources().getColor(R.color.colorPrimary)));
            }
            else{
                if(isOverPace){
                    map.addPolyline(new PolylineOptions()
                            .add(prevCoords, curCoords)
                            .width(10)
                            .color(getResources().getColor(R.color.goodGreen)));
                }else{
                    map.addPolyline(new PolylineOptions()
                            .add(prevCoords, curCoords)
                            .width(10)
                            .color(getResources().getColor(R.color.badRed)));
                }
            }
        }


        //Updates the view with the Session stats
        private void updateViewStatistics(int[] averagePace, int[] gap){
            totalDistanceView.setText(String.format("%.2f", totalDistanceMeters / 1000) + "Km");

            if(isRequiredMetersComplete()){
                String avgMinute = String.valueOf(averagePace[0]);
                String avgSecond = String.valueOf(averagePace[1]);
                String gapMinute = String.valueOf(gap[0]);
                String gapSecond = String.valueOf(gap[1]);

                if(gap[2] < 0){
                    gapToPaceView.setTextColor(getResources().getColor(R.color.goodGreen));
                    isOverPace = true;
                }else if(gap[2] == 0){
                    gapToPaceView.setTextColor(getResources().getColor(R.color.textPrimary));
                    isOverPace = true;
                }else{
                    gapToPaceView.setTextColor(getResources().getColor(R.color.badRed));
                    isOverPace= false;
                }

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
                }else{
                    gapToPaceView.setText("+"+gapMinute+":"+gapSecond);
                }
                averagePaceView.setText(avgMinute + ":" + avgSecond);

            }else {
                averagePaceView.setText("Calculating...");
                gapToPaceView.setText("Calculating...");
            }


        }
        /*
        Returns 0-3,
        0 indicates that no sound should be played.
        1 indicates that the first and less annoying sound should be played.
        2 indicates that the second and more annoying sound should be played.
        3 indicates that a Voice notifies that you are below your submitted pace.
        */
        private int getWhatSoundThatShouldBePlayed(int gapAmountOfSeconds){

            if(gapAmountOfSeconds > 0 && !notifiedUnderPace  ){
                notifiedUnderPace = true;
                return 3;
            }
            if (gapAmountOfSeconds >= AMOUNT_OF_SECONDS_WHEN_TRIGGERING_NOTIFICATION && gapAmountOfSeconds <= 0){
                if (gapAmountOfSeconds >= AMOUNT_OF_SECONDS_WHEN_TRIGGERING_NOTIFICATION/2 && gapAmountOfSeconds <= 0){
                    return 2;
                }
                notifiedUnderPace = false;
                return 1;
            }


            return 0;
        }

        private boolean isRequiredMetersComplete(){
            return totalDistanceMeters >= REQUIRED_METERS_BEFORE_SHOWING_AVGPACE;
        }

        private int[] sessionSummary(){
            return new int[] {1};
        }
    }
}
