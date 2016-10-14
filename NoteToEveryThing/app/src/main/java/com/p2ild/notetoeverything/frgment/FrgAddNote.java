package com.p2ild.notetoeverything.frgment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;
import com.p2ild.notetoeverything.locationmanager.DetectGps;
import com.p2ild.notetoeverything.locationmanager.WifiGpsManager;
import com.p2ild.notetoeverything.observer.DetectGpsListioner;
import com.p2ild.notetoeverything.service.AppService;

import java.io.File;
import java.util.Locale;


/**
 * Created by duypi on 8/21/2016.
 */
public class FrgAddNote extends Fragment implements View.OnTouchListener, DetectGpsListioner {
    public static final int BUTTON_CAPTURE = 2;
    private static final String TAG = FrgAddNote.class.getSimpleName();
    private static final int BUTTON_BACK = 0;
    private final String imgPath;
    private final String imgThumbnailPath;
    private final String typeSave;
    private EditText edNoteTitle, edNoteContent;
    private InputMethodManager im;
    private Drawable[] buttonDrawable;
    private ImageView iv_img_frg_add_note;
    private String latlong = "";
    private LocationManager locationManager;
    private DetectGps network, gps;
    private Location location;
    private ProgressBar progressBar;
    private TextView tvStatusGps;

    public FrgAddNote(String imgPath, String imgThumbnailPath, String typeSave) {
        this.imgPath = imgPath;
        this.imgThumbnailPath = imgThumbnailPath;
        this.typeSave = typeSave;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frg_add_note, container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        turnOnGps(getActivity());
    }

    private void turnOnGps(Context context) {
        // TODO: 2016-10-09 Appservice chưa có location
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Gợi ý");
            alertDialog.setMessage("Bạn có muốn lưu vị trí(location) của note này không?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    (getActivity()).startActivity(intent);
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    progressBar.setVisibility(View.GONE);
                    tvStatusGps.setText("Ghi chú này sẽ không có vị trí trên map");
                }
            });
            alertDialog.show();
        } else {
            network = new DetectGps(getActivity(), this);
            gps = new DetectGps(getActivity(), this);
            location = WifiGpsManager.getLocation(getActivity(), network, gps);
        }
    }

    @Override
    public void onResume() {
        network = new DetectGps(getActivity(), this);
        gps = new DetectGps(getActivity(), this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = WifiGpsManager.getLocation(getActivity(), network, gps);
        }
        progressBar.setVisibility(View.VISIBLE);
        super.onResume();
    }

    private void initView(View rootView) {
        edNoteTitle = (EditText) rootView.findViewById(R.id.ed_note_title);
        edNoteContent = (EditText) rootView.findViewById(R.id.ed_note_content);
        edNoteTitle.setOnTouchListener(this);
        iv_img_frg_add_note = (ImageView) rootView.findViewById(R.id.iv_img_frg_add_note);
        progressBar = (ProgressBar) rootView.findViewById(R.id.prg_gps);
        tvStatusGps = (TextView) rootView.findViewById(R.id.tv_status_location);

        buttonDrawable = edNoteTitle.getCompoundDrawables();

        //Type text only sẽ không có ảnh
        if (imgPath != null) {
            Glide
                    .with(getActivity())
                    .load(new File(imgPath))
                    .into(iv_img_frg_add_note);
        }
        // TODO: 8/26/2016 ---Done--- keyboard không biến mất khi click capture
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public String getNoteTitle() {
        return edNoteTitle.getText().toString();
    }

    public String getNoteContent() {
        return edNoteContent.getText().toString();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getRawX() > (edNoteTitle.getRight() - buttonDrawable[BUTTON_CAPTURE].getBounds().width())) {

                    edNoteTitle.setFocusable(false);
                    edNoteContent.setFocusable(false);
                    im.hideSoftInputFromWindow(edNoteTitle.getWindowToken(), 0);
                    im.hideSoftInputFromWindow(edNoteContent.getWindowToken(), 0);

                    if (location != null) {
                        latlong = location.getLatitude() + "," + location.getLongitude();
                    } else {
                        Toast.makeText(getActivity(), "Chưa xác định được vị trí của bạn ", Toast.LENGTH_SHORT).show();
                    }

                    ((MainActivity) getActivity()).insertToDataBase(getNoteTitle(), getNoteContent(), imgPath, imgThumbnailPath, typeSave, latlong);
                    break;
                }
                if (motionEvent.getRawX() < (edNoteTitle.getLeft() + buttonDrawable[BUTTON_BACK].getBounds().width())) {
                    edNoteTitle.setFocusable(false);
                    edNoteContent.setFocusable(false);
                    im.hideSoftInputFromWindow(edNoteTitle.getWindowToken(), 0);
                    im.hideSoftInputFromWindow(edNoteContent.getWindowToken(), 0);
                    ((MainActivity) getActivity()).onBackPressed();
                    break;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void detectedGps(Location location) {
        String latlon =
                String.format(Locale.US, "Your location: %f,%f \n" + "Accurary: %f", location.getLatitude(), location.getLongitude(), location.getAccuracy());
        tvStatusGps.setText(latlon);
        progressBar.setVisibility(View.INVISIBLE);
        this.location = location;
    }
}
