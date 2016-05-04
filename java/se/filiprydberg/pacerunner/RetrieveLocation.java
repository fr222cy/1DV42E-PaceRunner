package se.filiprydberg.pacerunner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Filip on 4/21/2016.
 */
public class RetrieveLocation
{
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng _location;
    private Context context;
    public RetrieveLocation(Context context)
    {
        this.context = context;
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void listen()
    {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                _location = new LatLng(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        };
        //noinspection ResourceType
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public LatLng getCoordinates()
    {
        if(_location != null)
            return _location;
        else
            return new LatLng(0,0);
    }

    public boolean hasFoundLocation()
    {
        if (_location != null)
            return true;
        else
            return false;
    }
}
