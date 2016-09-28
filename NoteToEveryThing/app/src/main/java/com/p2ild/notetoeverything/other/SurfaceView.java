package com.p2ild.notetoeverything.other;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by duypi on 8/15/2016.
 */
public class SurfaceView extends android.view.SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = SurfaceView.class.getSimpleName();
    public static Camera camera;

    public SurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    /*Implement from Surfaceholder Callback*/
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        //Camera preview
        camera = null;
        camera = camera.open();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setCamera(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            try {
                Camera.Parameters cp = camera.getParameters();
                cp.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(cp);
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
