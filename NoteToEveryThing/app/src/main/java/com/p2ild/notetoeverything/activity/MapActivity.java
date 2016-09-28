package com.p2ild.notetoeverything.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.p2ild.notetoeverything.other.DataSerializable;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.other.RecycleViewOnItemTouch;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.adapter.RecycleViewAdapterMap;
import com.p2ild.notetoeverything.locationmanager.WifiGpsManagerActivity;

import java.util.ArrayList;

public class MapActivity extends Activity {

    private static final int REQUEST_LOCATION = 111;
    private static final String TAG = MapActivity.class.getSimpleName();
    private ArrayList<NoteItem> arrNoteItem;
    private RecycleViewOnItemTouch onItemTouchRcv;
    private WifiGpsManagerActivity wifiGpsManagerFrg;
    private RecyclerView recyclerView;
    private MapFragment mapFragment;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_google_map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            createMapFrgWithSupportLocation(true);
        }

        arrNoteItem = (ArrayList) ((DataSerializable) getIntent().getSerializableExtra(MainActivity.KEY_OBJECT_DB)).getData();
        recyclerView = (RecyclerView) findViewById(R.id.rcv_map);
        onItemTouchRcv = new RecycleViewOnItemTouch(this, recyclerView, new RecycleViewOnItemTouch.onItemClick() {
            @Override
            public void onClick(View view, int position) {
                wifiGpsManagerFrg.zoomToNote(position);
            }

            @Override
            public void onLongClick(View view, int position, float rawX, float rawY) {

            }

            @Override
            public void onActionFocus(float rawX, float rawY) {

            }

            @Override
            public void onActionUp(float rawX, float rawY, int position) {

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        RecycleViewAdapterMap rcvAdap = new RecycleViewAdapterMap(MapActivity.this, arrNoteItem);
        recyclerView.setAdapter(rcvAdap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, final int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(MapActivity.this, "Nếu không đồng ý quyền , chương trình sẽ không thể định vị vị trí của bạn", Toast.LENGTH_SHORT).show();
                    createMapFrgWithSupportLocation(false);
                }else {
                    createMapFrgWithSupportLocation(true);
                }
            }
        }
    }

    private void createMapFrgWithSupportLocation(final boolean gpsLocation) {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.frg_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                wifiGpsManagerFrg = new WifiGpsManagerActivity(MapActivity.this,googleMap,arrNoteItem);
                googleMap.setMyLocationEnabled(gpsLocation);
                recyclerView.addOnItemTouchListener(onItemTouchRcv);
            }
        });
    }
}
