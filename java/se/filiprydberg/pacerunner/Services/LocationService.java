package se.filiprydberg.pacerunner.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service {
    public final static String MY_ACTION = "MY_ACTION";


    private static double latidude;
    private static double longitude;
    private static double accuracy;
    private static boolean providerEnabled = true;
    private static int MILLISECONDS_UPDATE_LOCATION = 3000;
    private boolean isServiceEnabled = true;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DataPassingThread thread;
    public LocationService() {

    }


    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        thread = new DataPassingThread();
        thread.start();
        isServiceEnabled = true;
       return START_STICKY;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        isServiceEnabled = false;


    }

    public void startLocationListener(LocationManager locationManager){
        this.locationManager = locationManager;
        this.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latidude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                providerEnabled = true;
            }

            @Override
            public void onProviderDisabled(String provider) {
                providerEnabled = false;
            }
        };
        //noinspection ResourceType
        locationManager.requestLocationUpdates("gps", MILLISECONDS_UPDATE_LOCATION, 0, locationListener);
    }

    public class DataPassingThread extends Thread{

        @Override
        public void run() {

            while(isServiceEnabled){
                try {
                    Thread.sleep(3000);
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);
                    intent.putExtra("LATITUDE", LocationService.latidude);
                    intent.putExtra("LONGITUDE", LocationService.longitude);
                    intent.putExtra("GPS_ACCURACY", LocationService.accuracy);

                    if(!providerEnabled){
                        intent.putExtra("PROVIDER_ENABLED", LocationService.providerEnabled);
                    }
                    sendBroadcast(intent);
                }catch (InterruptedException e) {

                    e.printStackTrace();
                    break;
                }
            }

        }

    }
}
