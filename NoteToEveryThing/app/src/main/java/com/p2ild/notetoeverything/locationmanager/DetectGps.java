package com.p2ild.notetoeverything.locationmanager;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.p2ild.notetoeverything.observer.DetectGpsListioner;

/**
 * Created by duypi on 9/27/2016.
 */
public class DetectGps implements LocationListener {
    private static final String TAG = DetectGps.class.getSimpleName();
    private final Context context;
    private final DetectGpsListioner detectGpsListioner;

    public DetectGps(Context context,DetectGpsListioner detectGpsListioner) {
        this.context = context;
        this.detectGpsListioner = detectGpsListioner;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: location: "+location);
        if(detectGpsListioner!=null){
            detectGpsListioner.detectedGps(location);
        }

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
}
