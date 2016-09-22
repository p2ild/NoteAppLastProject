package com.p2ild.notetoeverything.other;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteItem;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by duypi on 8/26/2016.
 */
public class WifiGpsManagerActivity extends MapFragment {
    private static final String TAG = WifiGpsManagerActivity.class.getSimpleName();
    private final ArrayList<NoteItem> arrNote;
    private Context context;
    private WifiManager wf;
    private android.location.LocationManager locationManager;
    private String ssid;
    private double latitude, longitude;
    private LocationListener locationListener;
    private GoogleMap googleMap;

    public WifiGpsManagerActivity(Context context, GoogleMap googleMap, ArrayList<NoteItem> arrNote) {
        this.context = context;
        this.googleMap = googleMap;
        this.arrNote = arrNote;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        initMap();
        wf = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    private void initMap() {
        googleMap.getUiSettings().setCompassEnabled(true);
//        googleMap.setInfoWindowAdapter();
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        initLocation();

        initAllMarker();
    }

    private void initAllMarker() {
        MarkerOptions marker = new MarkerOptions();
//        for (NoteItem)
//        marker.title()
//
//        googleMap.clear();
//        marker.position(latlong);
        googleMap.addMarker(marker);
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latlong,18)));
    }

    public void initLocation(){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
                LatLng latlong = new LatLng(latitude, longitude);
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

        Criteria criteria = new Criteria();
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        String provider = locationManager.getBestProvider(criteria,true);
        locationManager.requestLocationUpdates(provider, 1, 5, locationListener);
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

    class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
        private NoteItem noteItem;
        public InfoWindowAdapter(NoteItem noteItem) {
            this.noteItem = noteItem;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.marker_info,null,false);
            Glide.with(getContext())
                    .load(new File(noteItem.getPathImg()))
                    .into(((ImageView)view.findViewById(R.id.iv_thumbnail)));
            ((TextView)view.findViewById(R.id.tv_title_map)).setText(noteItem.getNoteTitle());
            ((TextView)view.findViewById(R.id.tv_content_map)).setText(noteItem.getNoteContent());
            return null;
        }
    }

}
