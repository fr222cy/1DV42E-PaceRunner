package se.filiprydberg.pacerunner.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import se.filiprydberg.pacerunner.Services.LocationService;
import se.filiprydberg.pacerunner.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button proceedButton;
    private GoogleMap map;
    private MarkerOptions userPositionMarker;
    private LocationService service;
    private Marker marker;
    private ServiceReceiver serviceReceiver;
    private boolean canProceed = false;
    private LatLng coordinates;
    private boolean hasAskedForPermission = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pacerunner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        startService(new Intent(getBaseContext(), LocationService.class));

        service = new LocationService();
        checkPlayServices();
        checkForPermission();

        userPositionMarker = new MarkerOptions()
                .position(new LatLng(0, 0));


            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


        proceedButton = (Button) findViewById(R.id.proceedButton);

        proceedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (canProceed) {
                    Intent intent = new Intent(getApplicationContext(), SetPaceActivity.class);

                    intent.putExtra("LATITUDE", coordinates.latitude);
                    intent.putExtra("LONGITUDE", coordinates.longitude);

                    startActivity(intent);
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "You need to go outside for the GPS to work properly.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

    }
    @Override
    protected void onStart(){
        super.onStart();

        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.MY_ACTION);
        registerReceiver(serviceReceiver, intentFilter);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, 1000).show();
            }

            return false;
        }

        return true;
    }

    public void checkForPermission()
    {
          /*
        Check if it's API level 23 or higher,
        due to the new permission rules.
         */
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }
        else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            service.startLocationListener(locationManager);
        }
    }

    public void disableButton(){
        proceedButton.setText("TRYING TO LOCATE YOU...");
        proceedButton.setBackgroundColor(Color.rgb(255, 165, 0));
    }
    public void enableButton(){
        proceedButton.setText("SET PACE AND GO");
        proceedButton.setBackgroundColor(Color.GREEN);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        marker = map.addMarker(userPositionMarker);
    }
    
    // Called from requestPermission()
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    service.startLocationListener(locationManager);
                }
        }
    }

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            double latitude = arg1.getDoubleExtra("LATITUDE",0);
            double longitude = arg1.getDoubleExtra("LONGITUDE", 0);
            boolean providerEnabled = arg1.getBooleanExtra("PROVIDER_ENABLED", true);

            if(!providerEnabled && !hasAskedForPermission){
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                hasAskedForPermission = true;
            }

            if(longitude != 0 && latitude != 0){
                coordinates = new LatLng(latitude,longitude);
                marker.setPosition(coordinates);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 17));
                enableButton();
                canProceed = true;
            }
            else{
                disableButton();
                canProceed = false;
            }


        }

    }
}

