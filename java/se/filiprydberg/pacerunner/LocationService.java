package se.filiprydberg.pacerunner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service {
    final static String MY_ACTION = "MY_ACTION";


    public static double latidude;
    public static double longitude;

    public LocationService() {

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


        DataPassingThread thread = new DataPassingThread();
        thread.start();

       return START_STICKY;
    }
    @Override
    public void onDestroy()
    {

    }

    public void startLocationListener(LocationManager locationManager){

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latidude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast toast = Toast.makeText(getApplicationContext(), "Your GPS provider has been modified.", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast toast = Toast.makeText(getApplicationContext(), "Your GPS provider has been enabled.", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        //noinspection ResourceType
        locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
    }


    public class DataPassingThread extends Thread{

        @Override
        public void run() {

            while(true){
                try {
                    Thread.sleep(1000);
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);
                    intent.putExtra("LATITUDE", LocationService.latidude);
                    intent.putExtra("LONGITUDE", LocationService.longitude);
                    //TODO: PASS ACCURACY TO MAIN ACTIVITY
                    sendBroadcast(intent);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    break;
                }
            }

        }

    }
}
