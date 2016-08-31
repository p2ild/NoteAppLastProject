package com.p2ild.notetoeverything.frgment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.SurfaceView;
import com.p2ild.notetoeverything.activity.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by duypi on 8/22/2016.
 */
public class FrgCapture extends Fragment implements View.OnClickListener {
    private static final String PATH_INTERNAL = Environment.getDataDirectory() + "/data/" + "com.p2ild.notetoeverything" + "/imageSave/";
    private static final String TAG = FrgCapture.class.getSimpleName();
    private final SimpleDateFormat dateFormat;
    private final Date date;
    private ImageButton btCapture;
    private SurfaceView surfaceView;
    private String pathImg, pathThumbnailImg, fileNameImage, fileNameImageThumbnail;
    private Camera.PictureCallback takePicture;

    public FrgCapture() {
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        date = new Date();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.from(container.getContext()).inflate(R.layout.frg_capture, container, false);
        surfaceView = (SurfaceView) rootView.findViewById(R.id.surface_view);
        (btCapture = (ImageButton) rootView.findViewById(R.id.bt_save_note)).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        File file = new File(PATH_INTERNAL);
        if (!file.exists()) {
            file.mkdirs();
        }

        takePicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                FileOutputStream op = null;
                try {
                    //Lưu file gốc
                    op = new FileOutputStream(PATH_INTERNAL + fileNameImage);
                    op.write(bytes);
                    op.close();

                    //Lưu file thumbnail
                    op = new FileOutputStream(PATH_INTERNAL + fileNameImageThumbnail);
                    int width = (int) (camera.getParameters().getPictureSize().width / 7);
                    int height = (int) (camera.getParameters().getPictureSize().height / 7);
                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), width, height);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, op);
                    op.close();
                } catch (IOException e) {
                    Log.d(TAG, "EXCEPTION");
                    e.printStackTrace();
                }
            }
        };
    }

    public void saveImgToInternal() {
        fileNameImage = "note" + dateFormat.format(date) + ".jpg";
        fileNameImageThumbnail = "note" + dateFormat.format(date) + "_thumbnail.jpg";
        surfaceView.getCamera().takePicture(null, null, takePicture);
    }

    @Override
    public void onClick(View view) {
        saveImgToInternal();
        preview();
    }

    private void preview() {
        pathImg = PATH_INTERNAL + fileNameImage;
        pathThumbnailImg = PATH_INTERNAL + fileNameImageThumbnail;
        ((MainActivity) getActivity()).insertToDataBase(pathImg, pathThumbnailImg);
    }

    // TODO: 8/25/2016(-----Done-----) Chưa xử lý khi ấn back
}
