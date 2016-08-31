package com.p2ild.notetoeverything;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by duypi on 8/26/2016.
 */
public class WifiGpsManager {
    private static final String TAG = WifiGpsManager.class.getSimpleName();
    private Context context;
    private WifiManager wf;
    private android.location.LocationManager locationManager;
    private String ssid;
    private double latitude,longitude;
    private LocationListener locationListener;

    public WifiGpsManager(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        wf = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void getLocation() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 100);
            }
        }, 100);
    }

    public void getGpsInfomation(){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        if (ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 10, locationListener);
    }

    public String getWifiInfomation(){
        ssid = wf.getConnectionInfo().getSSID();
        return ssid;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
