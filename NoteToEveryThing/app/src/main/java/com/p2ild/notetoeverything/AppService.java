package com.p2ild.notetoeverything;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.p2ild.notetoeverything.other.DatabaseManager;

/**
 * Created by duypi on 9/8/2016.
 */
public class AppService extends Service implements View.OnClickListener {
    private static final String PATH_SCREEN_SHOTS = Environment.getExternalStorageDirectory() + "/Pictures/Screenshots";
    ;
    private static final String TAG = AppService.class.getSimpleName();
    private static final int SHOW_DIALOG = 222;
    private FileObserver fileObserver;
    private InputMethodManager ipm;
    private ImageView img;
    private ImageButton bt_cancel, bt_yes;
    private WindowManager.LayoutParams wmParam;
    private WindowManager windowManager;
    private View viewInflate;
    private LinearLayout llConfirm, llAdd;
    private EditText edNoteTitle,edNoteContent;
    private String pathScreenShot;
    private ImageButton ibSave,ibCancelNote;
    private int w,h;
    private DatabaseManager databaseManager;

    public AppService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override

    public void onCreate() {
        super.onCreate();
        databaseManager = new DatabaseManager(getBaseContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(AppService.this, "START COMPLETE", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStartCommand: ");
        fileObserver = new FileObserver(PATH_SCREEN_SHOTS) {
            @Override
            public void onEvent(int event, final String path) {
                switch (event) {
                    case FileObserver.CREATE:
                        pathScreenShot = PATH_SCREEN_SHOTS+"/"+path;
                        AsyncTask<Void, Integer, Void> showDialog = new AsyncTask<Void, Integer, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                publishProgress();
                                return null;
                            }

                            @Override
                            protected void onProgressUpdate(Integer... values) {
                                initScreenShotView(path);
                                super.onProgressUpdate(values);
                            }
                        };
                        showDialog.execute();
                        break;
                    case FileObserver.CLOSE_WRITE:
                        AsyncTask<Void, Integer, Void> showImage = new AsyncTask<Void, Integer, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                publishProgress();
                                return null;
                            }

                            @Override
                            protected void onProgressUpdate(Integer... values) {
                                super.onProgressUpdate(values);
                                Glide.with(getBaseContext())
                                        .load(PATH_SCREEN_SHOTS + "/" + path)
                                        .into(img);
                            }
                        };
                        showImage.execute();
                        break;
                    case FileObserver.DELETE:
                        break;
                    default:
                        break;
                }
            }
        };
        fileObserver.startWatching();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initScreenShotView(String path) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        viewInflate = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog_screen_shots, null, false);
        w = getResources().getDisplayMetrics().widthPixels - 50;
        h = getResources().getDisplayMetrics().heightPixels / 2;
        wmParam = new WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSPARENT);
        wmParam.x = 0;
        wmParam.y = 0;
        wmParam.verticalMargin = 100;
        windowManager.addView(viewInflate, wmParam);

        img = (ImageView) viewInflate.findViewById(R.id.img_screen_shot);

        (bt_cancel = (ImageButton) viewInflate.findViewById(R.id.ib_cancel)).setOnClickListener(this);
        (bt_yes = (ImageButton) viewInflate.findViewById(R.id.ib_yes)).setOnClickListener(this);

        llConfirm = (LinearLayout) viewInflate.findViewById(R.id.frm_dialog_confirm_scr_shot);
        llAdd = (LinearLayout) viewInflate.findViewById(R.id.frm_dialog_add_note);
        edNoteTitle = (EditText)viewInflate.findViewById(R.id.ed_note_title);
        edNoteContent = (EditText)viewInflate.findViewById(R.id.ed_note_content);

        llConfirm.setPivotX(w / 2);
        llConfirm.setPivotY(h / 2);
        llConfirm.setRotationY(0);

        llAdd.setVisibility(View.GONE);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_yes:
                llAdd.setVisibility(View.VISIBLE);
                llAdd.setPivotX(w / 2);
                llAdd.setPivotY(h / 2);

                (ibSave = (ImageButton)viewInflate.findViewById(R.id.ib_save_note)).setOnClickListener(this);
                (ibCancelNote = (ImageButton)viewInflate.findViewById(R.id.ib_cancel_note)).setOnClickListener(this);

                Log.d(TAG, "onClick: YES");
                AsyncTask<Void, int[], Void> anim = new AsyncTask<Void, int[], Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        int[] arr = {0, -90};
                        int j;
                        int i;
                        for (i = 0; i < 70; i++) {
                            arr[0] = i;
                            SystemClock.sleep(5);
                            publishProgress(arr);
                        }

                        for (j = -70; j <= 0; j++) {
                            arr[1] = j;
                            SystemClock.sleep(5);
                            publishProgress(arr);
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(int[]... values) {
                        llConfirm.setRotationY(values[0][0]);
                        Log.d(TAG, "onProgressUpdate: values[0][0] "+values[0][0]);
                        llAdd.setRotationY(values[0][1]);
                        Log.d(TAG, "onProgressUpdate: values[0][1] "+values[0][1]);
                    }
                };
                anim.execute();


                break;
            case R.id.ib_save_note:
                Log.d(TAG, "onClick: SAVE NOTE");
                databaseManager.insert(edNoteTitle.getText().toString(),edNoteContent.getText().toString(),pathScreenShot,pathScreenShot);
                windowManager.removeViewImmediate(viewInflate);
                break;
            case R.id.ib_cancel:
                Log.d(TAG, "onClick: CANCEL");
                windowManager.removeViewImmediate(viewInflate);
                break;
            case R.id.ib_cancel_note:
                Log.d(TAG, "onClick: CANCEL_NOTE");
                windowManager.removeViewImmediate(viewInflate);
                break;
            default:
                break;
        }
    }
}
