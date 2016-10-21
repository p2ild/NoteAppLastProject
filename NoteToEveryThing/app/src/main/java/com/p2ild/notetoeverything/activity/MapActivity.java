package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.adapter.RecycleViewAdapterMap;
import com.p2ild.notetoeverything.dialog.DialogMapShowContent;
import com.p2ild.notetoeverything.locationmanager.MapManager;
import com.p2ild.notetoeverything.observer.DataChangeMapListener;
import com.p2ild.notetoeverything.other.DataSerializable;
import com.p2ild.notetoeverything.other.RecycleViewOnItemTouch;

import java.util.ArrayList;

public class MapActivity extends Activity{

    public static final int REQUEST_LOCATION = 111;
    private static final String TAG = MapActivity.class.getSimpleName();
    private ArrayList<NoteItem> arrNoteItem;
    private RecycleViewOnItemTouch onItemTouchRcv;
    private MapManager mapManagerFrg;
    private RecyclerView recyclerView;
    private MapFragment mapFragment;
    private RecycleViewAdapterMap rcvAdap;
    private DataChangeMapListener dataChangeMapListener;
    private DialogMapShowContent dialogMapShowContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_google_map);

        createMapFrg();



        getAndRemoveItemNoLatlong();
        initRcv(arrNoteItem);

    }

    private void getAndRemoveItemNoLatlong() {
        arrNoteItem = new ArrayList<>();
        arrNoteItem.addAll((ArrayList) ((DataSerializable) getIntent().getSerializableExtra(MainActivity.KEY_OBJECT_DB)).getData());
        for (int i=arrNoteItem.size()-1;i>=0;i--) {//Duyệt lùi
            /*Thử tryCatch với những string rỗng hoặc null sẽ remove itemNote khỏi mảng*/
            try {
                double lat = Double.parseDouble(arrNoteItem.get(i).getLatlong().split(",")[0]);
                double longi = Double.parseDouble(arrNoteItem.get(i).getLatlong().split(",")[1]);
            } catch (NumberFormatException e1) {
                Log.d(TAG, "initAllMarker: error at|| " + arrNoteItem.get(i).getNoteTitle() + "," + arrNoteItem.get(i).getNoteContent() + "||" + arrNoteItem.get(i).getLatlong());
                arrNoteItem.remove(arrNoteItem.get(i));
            }
        }
    }

    /**Lấy ra đối tượng GoogleMap khi GoogleMap đã sẵn sàng
     * Đã check permission từ activity Main*/
    private void createMapFrg() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.frg_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapManagerFrg = new MapManager(MapActivity.this, googleMap, arrNoteItem,dataChangeMapListener);
                googleMap.setMyLocationEnabled(true);
            }
        });
    }

    /**Khởi tạo Recycle View*/
    private void initRcv(ArrayList<NoteItem> newData) {
        recyclerView = (RecyclerView) findViewById(R.id.rcv_map);

        /*Hành động click*/
        onItemTouchRcv = new RecycleViewOnItemTouch(this, recyclerView, new RecycleViewOnItemTouch.onItemClick() {
            @Override
            public void onClick(View view, int position) {
                mapManagerFrg.zoomToNote(position);//Zoom tới vị trí của marker có latlong
            }
            /*Hiển thị dialog có hình ảnh và nội dung
            * Show khi long click
            * Dismiss khi actionUp*/
            @Override
            public void onLongClick(View view, int position, float rawX, float rawY) {
                dialogMapShowContent = new DialogMapShowContent(MapActivity.this,
                        rcvAdap.getArrayNote().get(position).getNoteTitle(),
                        rcvAdap.getArrayNote().get(position).getNoteContent(),
                        rcvAdap.getArrayNote().get(position).getPathImg());
                dialogMapShowContent.show();
            }

            @Override
            public void onActionFocus(float rawX, float rawY) {
            }

            /*Nhả tay thì dismiss dialog nếu nó khác rỗng và đang hiển thị*/
            @Override
            public void onActionUp(float rawX, float rawY, int position) {
                if(dialogMapShowContent!=null && dialogMapShowContent.isShowing()){
                    dialogMapShowContent.dismiss();
                    dialogMapShowContent=null;
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));//Set kiểu quản lý view
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnItemTouchListener(onItemTouchRcv);//Set đối tượng để xử lý on item click
        rcvAdap = new RecycleViewAdapterMap(MapActivity.this,newData);//set array cho adapter
        recyclerView.setAdapter(rcvAdap);//set adapter cho recycle view
    }
}
