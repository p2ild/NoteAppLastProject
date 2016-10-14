package com.p2ild.notetoeverything.frgment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.p2ild.notetoeverything.DatabaseManagerCopyDb;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.other.SurfaceView;
import com.p2ild.notetoeverything.activity.MainActivity;
import com.p2ild.notetoeverything.DatabaseManagerCopyDb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by duypi on 8/22/2016.
 */
public class FrgCapture extends Fragment implements View.OnClickListener {
    private static final String TAG = FrgCapture.class.getSimpleName();
    private final SimpleDateFormat dateFormat;
    private final Date date;
    private ImageButton btCapture;
    private SurfaceView surfaceView;
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
        File file = new File(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave");
        if (!file.exists()) {
            file.mkdirs();
        }

        takePicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

                try {
                    String fileNameImage = "note" + dateFormat.format(date) + ".jpg";
                    final String fileNameImageThumbnail = "note" + dateFormat.format(date) + "_thumbnail.jpg";
                    final File fileOpImg = new File(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave/" + fileNameImage);
                    final File fileOpThumbnail = new File(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave/" + fileNameImageThumbnail);
                    FileOutputStream opImg = new FileOutputStream(fileOpImg);
                    FileOutputStream opThumbnail = new FileOutputStream(fileOpThumbnail);
                    FileObserver fileObserver = new FileObserver(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave") {
                        @Override
                        public void onEvent(int event, String path) {
                            Log.d(TAG, "onEvent: "+path);
                            if (event == FileObserver.CLOSE_WRITE && path.equals(fileNameImageThumbnail)) {
                                ((MainActivity) getActivity()).showFrgAddNote(fileOpImg.getPath(), fileOpThumbnail.getPath(), DatabaseManagerCopyDb.TYPE_CAPTURE);
                                this.stopWatching();
                            }
                        }
                    };
                    fileObserver.startWatching();
                    //Lưu file gốc

                    opImg.write(bytes);
                    opImg.close();

                    //Lưu file thumbnail

                    int width = (int) (camera.getParameters().getPictureSize().width / 7);
                    int height = (int) (camera.getParameters().getPictureSize().height / 7);
                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), width, height);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, opThumbnail);
                    opThumbnail.close();
                } catch (IOException e) {
                    Log.d(TAG, "EXCEPTION");
                    e.printStackTrace();
                }
                camera.stopPreview();
                camera.release();
            }
        };
    }

    public void saveImgToInternal() {
        surfaceView.getCamera().takePicture(null, null, takePicture);
    }

    @Override
    public void onClick(View view) {
        saveImgToInternal();
    }
    // TODO: 8/25/2016 ---Done--- Chưa xử lý khi ấn back
}
