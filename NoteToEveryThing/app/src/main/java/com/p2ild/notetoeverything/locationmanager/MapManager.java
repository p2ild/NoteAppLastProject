package com.p2ild.notetoeverything.locationmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.observer.DataChangeMapListener;

import java.util.ArrayList;

/**
 * Created by duypi on 8/26/2016.
 */
public class MapManager extends MapFragment {
    private static final String TAG = MapManager.class.getSimpleName();
    private final ArrayList<NoteItem> arrNote;
    private final DataChangeMapListener dataChangeMapListener;
    private Context context;
    private android.location.LocationManager locationManager;
    private GoogleMap googleMap;
    private String externalIp = null;
    private Location location;
    private boolean canGetLocation;


    public MapManager(Context context, GoogleMap googleMap, ArrayList<NoteItem> oriArrayNote, DataChangeMapListener dataChangeMapListener) {
        this.context = context;
        this.googleMap = googleMap;

        arrNote = new ArrayList<>();
        arrNote.addAll(oriArrayNote);

        this.dataChangeMapListener = dataChangeMapListener;//Chưa sử dụng đến

        initMap();
    }



    /*Get location from GPS without cretial
    * Đã check permission ở ngoài*/
    public static Location getLocation(Context context, LocationListener gpsListioner, LocationListener netWorkListioner) {
        Location location = null;
        try {
            LocationManager lm = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = lm
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "getLocation: isGPSEnabled " + isGPSEnabled);

            // getting network status
            boolean isNetworkEnabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "getLocation: isNetworkEnabled " + isNetworkEnabled);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                Log.d(TAG, "getLocation: CA 2 CUNG SU DUNG DC");
                if (isNetworkEnabled) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 2, netWorkListioner);
                    Log.d("Network", "Network Enabled");
                    if (lm != null) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    } else if (isGPSEnabled) {
                        if (location == null) {
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 2, gpsListioner);
                            Log.d("GPS", "GPS Enabled");
                            if (lm != null) {
                                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return location;
    }

    public static String getSSIDWifi(Context context) {
        WifiManager wf = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ssid = wf.getConnectionInfo().getSSID();
        return ssid;
    }

    public void initLocationWithCriteria() {
        Criteria criteria = new Criteria();
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
//        locationManager.requestLocationUpdates(provider, 1, 2, locationListener);
    }

    /**Khởi tạo thuộc tính của bản đồ (Gọi ở Hàm khởi tạo)*/
    private void initMap() {
        googleMap.getUiSettings().setCompassEnabled(true);//Bản đồ có la bàn
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//Bản đồ ở chế độ xem đường phố
        googleMap.getUiSettings().setAllGesturesEnabled(true);//Bản đồ có thể tương tác bằng gesture
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);//Bản đồ có nút xác định vị trí hiện tại

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);//Lấy dịch vụ Location

        /*Khi ấn vào nút Xác định vị trí hiện tại*/
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//Nếu setting Gps chưa bật
                    /*Tạo hộp thoại bật gps*/
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Bạn có muốn bật GPS trên thiết bị này không?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//Intent mở setting location change
                            ((Activity) context).startActivity(intent);//Mở setting dựa vào intent
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(context, "Không thể xác định được vị trí vì bạn vì chưa bật gps", Toast.LENGTH_SHORT).show();
                        }
                    });
                    alertDialog.show();
                }
                return false;
            }
        });

        /*Khởi tạo windowAdapter*/
        InfoWindowAdapter infoAdapter = new InfoWindowAdapter();
        googleMap.setInfoWindowAdapter(infoAdapter);//gán window adapter vào bản đồ
//        initLocationWithCriteria();


        initAllMarker();

        location = getLocation(context, new DetectGps(context, null), new DetectGps(context, null));
    }

    /** Add tất cả các marker của note có lat long (Gọi ở initMap)*/
    private void initAllMarker() {
        for (int i = 0; i < arrNote.size(); i++) {
            MarkerOptions marker = new MarkerOptions();//Khởi tạo đối tượng
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_map));//Sửa ảnh cho đối tượng marker
            double lat = Double.parseDouble(arrNote.get(i).getLatlong().split(",")[0]);//Lấy lat
            double longi = Double.parseDouble(arrNote.get(i).getLatlong().split(",")[1]);//Lấy long
            LatLng latLng = new LatLng(lat, longi);
            marker.position(latLng);//Set vị trí cho marker
            marker.title("" + i);//set title chính là vị trí của note trong mảng. (Sử dụng khi InfoWindow lấy dữ liệu qua vị trí này)
            googleMap.addMarker(marker);//Add vào bản đồ
        }
    }

    /**Zoom camera tới vị trí truyền vào (Gọi ở onClick ItemNote)
     * Tất cả đều được lọc ở bước add item vào adapter nên Exception NumberFormat không vào*/
    public void zoomToNote(int position) {
            double lat = Double.parseDouble(arrNote.get(position).getLatlong().split(",")[0]);
            double lon = Double.parseDouble(arrNote.get(position).getLatlong().split(",")[1]);
            LatLng latLng = new LatLng(lat, lon);
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 18)));
    }

    /**Cài đặt đối tượng infowindowAdapter*/
    class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        public InfoWindowAdapter() {
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        //Cài đặt nội dung hiển thị cho adapter
        @Override
        public View getInfoContents(Marker marker) {
            final int positionInArray = Integer.parseInt(marker.getTitle());//Lấy ra vị trí note cần hiển thị dựa vào title của marker được click
            View view = LayoutInflater.from(context).inflate(R.layout.marker_info, null, false);
            /*Note có ảnh quá lớn sẽ được scale lại
            * Tại sao k sử dụng Glide ở đây ? */
            if (arrNote.get(positionInArray).getPathImg() != null
                    || !arrNote.get(positionInArray).getPathImg().equals("")) {
                ((ImageView) view.findViewById(R.id.iv_thumbnail)).setImageBitmap(DatabaseManager.decodeSampledBitmapFromResource(arrNote.get(positionInArray).getPathImg(), 200, 300));
            }
            /*Remove image view vì note không có ảnh không cần dùng đến*/
            else {
                ((ImageView) view.findViewById(R.id.iv_thumbnail)).setVisibility(View.GONE);
            }
            /*Hiển thị title và content*/
            ((TextView) view.findViewById(R.id.tv_title_map)).setText(arrNote.get(positionInArray).getNoteTitle());
            ((TextView) view.findViewById(R.id.tv_content_map)).setText(arrNote.get(positionInArray).getNoteContent());
            return view;
        }
    }
}
