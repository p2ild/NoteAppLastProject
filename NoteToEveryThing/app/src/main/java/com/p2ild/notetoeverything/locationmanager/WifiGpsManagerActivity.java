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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteItem;

import java.util.ArrayList;

/**
 * Created by duypi on 8/26/2016.
 */
public class WifiGpsManagerActivity extends MapFragment {
    private static final String TAG = WifiGpsManagerActivity.class.getSimpleName();
    private final ArrayList<NoteItem> arrNote;
    private Context context;
    private WifiManager wf;
    private String ssid;
    private android.location.LocationManager locationManager;
    private GoogleMap googleMap;
    private String externalIp = null;
    private Location location;
    private boolean canGetLocation;


    public WifiGpsManagerActivity(Context context, GoogleMap googleMap, ArrayList<NoteItem> arrNote) {
        this.context = context;
        this.googleMap = googleMap;
        this.arrNote = arrNote;
        wf = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        initMap();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);


        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /*Get location from GPS without cretial*/
    public static void getLocation(Context context, LocationListener gpsListioner, LocationListener netWorkListioner) {
        try {
            Location location = null;

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
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, netWorkListioner);
                    Log.d("Network", "Network Enabled");
                    if (lm != null) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    } else if (isGPSEnabled) {
                        if (location == null) {
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListioner);
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
    }

    private void initMap() {
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //        googleMap.setInfoWindowAdapter();
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Do u want to turn on Gps");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            ((Activity) context).startActivity(intent);
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(context, "Không thể xác định được vị trí vì bạn chưa bật gps", Toast.LENGTH_SHORT).show();
                        }
                    });
                    alertDialog.show();
                }
                return false;
            }
        });
        InfoWindowAdapter infoAdapter = new InfoWindowAdapter();
        googleMap.setInfoWindowAdapter(infoAdapter);
//        initLocationWithCriteria();
        initAllMarker();
        getLocation(context, new GeoListioner(), new GeoListioner());
    }

    private void initAllMarker() {
        Log.d(TAG, "initAllMarker: arrNote size: "+arrNote.size());
        for (int i = 0; i < arrNote.size(); i++) {
            try {
                double lat = Double.parseDouble(arrNote.get(i).getLatlong().split(",")[0]);
                double longi = Double.parseDouble(arrNote.get(i).getLatlong().split(",")[1]);
                String path = arrNote.get(i).getPathImg();
                if (path != null && !path.equals("")) {
                    LatLng latLng = new LatLng(lat, longi);
//                    Bitmap bitmap = decodeSampledBitmapFromResource(arrNote.get(i).getPathImg(), 50, 100);
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(latLng);
                    marker.title("" + i);
                    googleMap.addMarker(marker);
                }
            } catch (NumberFormatException e1) {
                arrNote.remove(i);
            }
        }
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

//    public void getIpAddress() {
//        Log.d(TAG, "getIpAddress: ");
//        final AsyncTask asyncTask = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                String ip = null;
//                try {
//                    HttpClient httpclient = new DefaultHttpClient();
//                    HttpGet httpget = new HttpGet("http://ifconfig.me/ip.json");
//                    HttpResponse response;
//                    response = httpclient.execute(httpget);
//                    HttpEntity entity = response.getEntity();
//                    entity.getContentLength();
//                    String str = EntityUtils.toString(entity);
//                    ip = str;
//                    Log.d(TAG, "doInBackground: ADSADASDASD");
//                }
//                catch (Exception e){
//                    Log.d(TAG, "getIpAddress: error "+e);
//                }
//                Log.d(TAG, "getIpAddress: ip::: " +ip);
//                return null;
//            }
//        };
//        asyncTask.execute();
//    }

//    public void getLocationFromExternalIp(){
//        AsyncTask<Void,String,Void> asyncTask = new AsyncTask<Void, String, Void>() {
//            @Override
//            protected Void doInBackground(Void...voids) {
//                try {
//                    JSONObject jsonObject = JSonReader.readJsonFromUrl("http://ip-api.com/json");
//                    if(jsonObject.has("lat")){
//                        lat = (jsonObject.getString("lat"));
//                    }
//
//                    if(jsonObject.has("lon")){
//                        lon = (jsonObject.getString("lon"));
//                    }
//
//                    if(jsonObject.has("query")){
//                        externalIp = jsonObject.getString("query");
//                    }
//
//                    Log.d(TAG, "doInBackground: "+"lat,long,externalIp: "+lat+","+lon+","+externalIp);
//                    publishProgress(lat,lon);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onProgressUpdate(String... values) {
//                googleMap.animateCamera(CameraUpdateFactory.
//                        newCameraPosition(CameraPosition.fromLatLngZoom(
//                                new LatLng(Double.parseDouble(values[0]),Double.parseDouble(values[1])),18)));
//                super.onProgressUpdate(values);
//            }
//        };
//        asyncTask.execute();
//    }

    public String getWifiInfomation() {
        ssid = wf.getConnectionInfo().getSSID();
        return ssid;
    }

    public void zoomToNote(int position) {
        try {
            double lat = Double.parseDouble(arrNote.get(position).getLatlong().split(",")[0]);
            double lon = Double.parseDouble(arrNote.get(position).getLatlong().split(",")[1]);
            LatLng latLng = new LatLng(lat, lon);
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 18)));
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Thằng này chưa có latlong nhé", Toast.LENGTH_SHORT).show();
        }
    }

    class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        public InfoWindowAdapter() {
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            final int positionInArray = Integer.parseInt(marker.getTitle());
            View view = LayoutInflater.from(context).inflate(R.layout.marker_info, null, false);
            ((ImageView) view.findViewById(R.id.iv_thumbnail)).setImageBitmap(decodeSampledBitmapFromResource(arrNote.get(positionInArray).getPathImg(), 200, 300));
            ((TextView) view.findViewById(R.id.tv_title_map)).setText(arrNote.get(positionInArray).getNoteTitle());
            ((TextView) view.findViewById(R.id.tv_content_map)).setText(arrNote.get(positionInArray).getNoteContent());
            return view;
        }
    }
}
