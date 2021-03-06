package com.p2ild.notetoeverything.frgment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;
import com.p2ild.notetoeverything.other.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.p2ild.notetoeverything.other.SurfaceView.camera;

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
        File file = new File(DatabaseManager.PATH_APP_INTERNAL + "/imageSave");
        if (!file.exists()) {
            file.mkdirs();
        }

        takePicture = new Camera.PictureCallback() {
            public Camera mCamera;
            public FileObserver fileObserver;

            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                this.mCamera = camera;
                try {
                    final String fileNameImage = "note" + dateFormat.format(date) + ".jpg";
                    final File fileOpImg = new File(DatabaseManager.PATH_APP_INTERNAL + "/imageSave/" + fileNameImage);
                    FileOutputStream opImg = new FileOutputStream(fileOpImg);
//                    final String fileNameImageThumbnail = "note" + dateFormat.format(date) + "_thumbnail.jpg";
//                    final File fileOpThumbnail = new File(DatabaseManager.PATH_APP_INTERNAL + "/imageSave/" + fileNameImageThumbnail);
//                    FileOutputStream opThumbnail = new FileOutputStream(fileOpThumbnail);

                    fileObserver = new FileObserver(DatabaseManager.PATH_APP_INTERNAL + "/imageSave") {
                        @Override
                        public void onEvent(int event, String path) {
                            Log.d(TAG, "onEvent: " + path);
                            if (event == FileObserver.CLOSE_WRITE && path.equals(fileNameImage)) {
                                //chỉ khi save lại thì mới stop mCamera tránh lỗi khi onBackPress ở MainActivity stop thêm lần nữa gây lỗi
//                                mCamera.stopPreview();
//                                mCamera.release();
//                                mCamera = null;
                                ((MainActivity) getActivity()).showFrgAddNote(fileOpImg.getPath(), "", DatabaseManager.TYPE_CAPTURE);
                                this.stopWatching();
                            }
                        }
                    };
                    fileObserver.startWatching();

                    //Lưu ảnh chính
                    Bitmap bitmapSource = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    Bitmap bitmapOriented;
                        if(((MainActivity)getActivity()).getOrientDevice() == MainActivity.ORIENT_VERTICAL){
                            Matrix matrix = new Matrix();
                            matrix.setRotate(surfaceView.getRotationCamera());
                            bitmapOriented= Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.getWidth(), bitmapSource.getHeight(), matrix, true);
                        }else {
                            bitmapOriented=bitmapSource;
                        }


                    bitmapOriented.compress(Bitmap.CompressFormat.JPEG, 100, opImg);
                    opImg.close();

                    //Lưu ảnh thumbnail
//                    int width = (int) (camera.getParameters().getPictureSize().width / 7);
//                    int height = (int) (camera.getParameters().getPictureSize().height / 7);
//                    Bitmap bitmapThumbnail = ThumbnailUtils.extractThumbnail(bitmapOriented, width, height);
//                    bitmapThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, opThumbnail);
//                    opThumbnail.close();
                } catch (IOException e) {
                    Log.d(TAG, "EXCEPTION");
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        surfaceView.getCamera().takePicture(null, null, takePicture);
    }
    // TODO: 8/25/2016 ---Done--- Chưa xử lý khi ấn back


    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        camera.stopPreview();
        camera.release();
        super.onDestroyView();
    }
}
