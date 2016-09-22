package com.p2ild.demogooglemap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by duypi on 9/18/2016.
 */
public class MapManager implements android.location.LocationListener{
    private static final String TAG = MapManager.class.getSimpleName();
    private final GoogleMap mGMap;
    private final Context mContext;
    private LocationManager locationMng;
    private PolylineOptions pLineOption;

    public MapManager(Context context, GoogleMap googleMap) {
        mContext = context;
        mGMap = googleMap;
        initMap();
        checkGpsIsEnable();
        myLocationChangeRegister2();
    }

    private void checkGpsIsEnable() {
        locationMng = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean result = locationMng.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!result) {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Do u want to turn on Gps");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Activity) mContext).finish();
                }
            });
            alertDialog.show();
        }
    }

    private void initMap() {
        mGMap.getUiSettings().setAllGesturesEnabled(true);
        mGMap.getUiSettings().setZoomControlsEnabled(true);
        mGMap.getUiSettings().setCompassEnabled(true);
        mGMap.setMyLocationEnabled(true);
        mGMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mGMap.setInfoWindowAdapter(infoWindowAdapter);

        initPolyGonLine();
    }

    private void initPolyGonLine() {
        pLineOption = new PolylineOptions();
        pLineOption.color(Color.BLACK);
        pLineOption.width(20);

    }

    private void myLocationChangeRegister() {
        mGMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.d(TAG, "onMyLocationChange: ");
                addMarker("No descripe", new LatLng(location.getLatitude(), location.getLongitude()));
                mGMap.clear();
            }
        });
    }

    private void myLocationChangeRegister2() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(true);
        String provider = locationMng.getBestProvider(criteria,true);

        locationMng.requestLocationUpdates(provider,500,1,this);
    }

    private void addMarker(String title, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(title);
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        mGMap.addMarker(markerOptions);
        goole
        mGMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(position, 16)));
    }

    @Override
    public void onLocationChanged(Location location) {
        mGMap.clear();
        addMarker("No descripe", new LatLng(location.getLatitude(), location.getLongitude()));
        pLineOption.add(new LatLng(location.getLatitude(), location.getLongitude()));
        mGMap.addPolyline(pLineOption);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        if(s.equals(LocationManager.GPS_PROVIDER)){
            locationMng.removeUpdates(this);
        }
    }
    GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {

            return inflateView(marker.getTitle());
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private View inflateView(String title){
            View infoView = LayoutInflater.from(mContext).inflate(R.layout.marker_info,null,false);
            if(title.equals("")){

            }
            return infoView;
        }
    };
}
