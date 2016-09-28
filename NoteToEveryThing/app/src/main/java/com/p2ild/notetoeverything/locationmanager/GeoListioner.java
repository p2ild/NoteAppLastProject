package com.p2ild.notetoeverything.locationmanager;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by duypi on 9/27/2016.
 */
public class GeoListioner implements LocationListener {
    private static final String TAG = GeoListioner.class.getSimpleName();

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: "+location.getProvider()+" | "+location.getLatitude()+" | "+location.getLongitude()+" | "+(float)location.getAccuracy());
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
