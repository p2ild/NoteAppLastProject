package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteAdapter;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.other.DatabaseManager;
import com.p2ild.notetoeverything.other.WifiGpsManagerActivity;

import java.util.ArrayList;

public class MapActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_google_map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.frg_map);

        /*Test DAtabaseManager . Test xong xóa*/
        DatabaseManager databaseManager = new DatabaseManager(this,null);
        Cursor cursor = databaseManager.readAllData("All");
        final ArrayList<NoteItem> arrNoteItem = NoteAdapter.cursorToArrayList(cursor);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                WifiGpsManagerActivity wifiGpsManagerFrg = new WifiGpsManagerActivity(MapActivity.this,googleMap,arrNoteItem);
            }
        });

    }
}
