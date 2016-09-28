package com.p2ild.notetoeverything.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by duypi on 9/8/2016.
 */
public class AppService extends Service implements View.OnClickListener {
    private static final String PATH_SCREEN_SHOTS_MOTOROLA = Environment.getExternalStorageDirectory() + "/Pictures/Screenshots";
    private static final String PATH_SCREEN_SHOTS_ASUS = Environment.getExternalStorageDirectory() + "/Screenshots";

    private static final String TAG = AppService.class.getSimpleName();
    private static final int SHOW_DIALOG = 222;
    private static final int SERVICE_SCREEN_SHOOT = 333;
    private static final int SERVICE_BACKUP = 444;
    private static final int SERVICE_RESTORE = 555;
    private static final int SERVICE_DELETE = 666;
    private static final String KEY_SERVICE = "KEY_SERVICE";
    private static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    private static final String SWITCH_SCREENSHOT = "SWITCH_SCREENSHOT";
    private static final String SWITCH_CLIPBOARD = "SWITCH_CLIPBOARD";
    private static final Integer MSG_UPDATE_RCV = 777;
    private FileObserver fileObserver;
    private InputMethodManager ipm;
    private ImageView img;
    private ImageButton bt_cancel, bt_yes;
    private WindowManager.LayoutParams wmParam;
    private WindowManager windowManager;
    private View viewInflate;
    private LinearLayout llConfirm, llAdd;
    private EditText edNoteTitle, edNoteContent;
    private String pathScreenShot = "";
    private ImageButton ibSave, ibCancelNote;
    private int w, h;
    private DatabaseManager databaseManager;
    private String pathInternal;
    private String fileName;
    private EventBus eventBus = EventBus.getDefault();
    private SharedPreferences sharedPreferences;
    private ClipboardManager clb;
    private ClipData primaryClip;
    private boolean onceRunClipboard; // tránh initView được khởi tạo nhiều lần khi vào phương thức onPrimaryClipChanged
    private boolean onceRunScreenShot; // tránh initView được khởi tạo nhiều lần khi vào phương thức onPrimaryClipChanged
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private MyContentObserver myContentObserver;
    private SimpleDateFormat dateFormat;
    private Date date;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        databaseManager = new DatabaseManager(this, eventBus);

        sharedPreferences = getSharedPreferences(SHARE_PREFERENCE, MODE_PRIVATE);

        myContentObserver = new MyContentObserver(handler);

        clb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        primaryClip = clb.getPrimaryClip();

        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        allwayRunService();
        runServiceIfClick(intent);
        Log.d(TAG, "runServiceIfClick: AUTO START SERVICE");
        return START_STICKY;
    }

    private void runServiceIfClick(Intent intent) {
        if (intent != null) {
            switch (intent.getIntExtra(KEY_SERVICE, 0)) {
                case SERVICE_BACKUP:
                    databaseManager.backupAllNote(this);
                    break;
                case SERVICE_RESTORE:
                    databaseManager.restoreAllNote(this);
                    break;
                case SERVICE_DELETE:
                    databaseManager.delAllDataTable(this);
                    break;
                default:
                    break;
            }
        } else {
            Log.d(TAG, "onStartCommand: INTENT == null || KHÔNG CLICK BACKUP/RESTORE/DELETE");
        }
    }

    private void allwayRunService() {
        //Chế độ chụp màn hình
        Log.d(TAG, "allwayRunService: ");
        if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) != null) {
            Log.d(TAG, "allwayRunService: sharepref : "+sharedPreferences.getString(SWITCH_SCREENSHOT, null));
            switch (sharedPreferences.getString(SWITCH_SCREENSHOT, null)) {
                case "on":
                    getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, myContentObserver);
                    Log.d(TAG, "allwayRunService: REGISTER");
                    onceRunScreenShot = true;
                    break;
                case "off":
                    getContentResolver().unregisterContentObserver(myContentObserver);
                    break;
                default:
                    break;
            }
        }else {
            Log.d(TAG, "allwayRunService: sharepref : KHÔNG CÓ KEY SCREENSHOT");

        }

        //Chế độ clipboard save
        if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) != null) {
            switch (sharedPreferences.getString(SWITCH_CLIPBOARD, null)) {
                case "on":
                    clb.addPrimaryClipChangedListener(new ClipBoardListener());
                    onceRunClipboard = true;
                    break;
                case "off":
                    clb.removePrimaryClipChangedListener(new ClipBoardListener());
                    break;
                default:
                    break;
            }
        }


    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        Toast.makeText(AppService.this, "Screen shot mode turn off", Toast.LENGTH_SHORT).show();
        getContentResolver().unregisterContentObserver(myContentObserver);
        super.onDestroy();
    }

    private void initView() {
        Log.d(TAG, "initView: initView");
        onceRunClipboard = false;
        onceRunScreenShot = false;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        viewInflate = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog_screen_shots, null, false);
        w = getResources().getDisplayMetrics().widthPixels - 100;
        h = getResources().getDisplayMetrics().heightPixels - 100;
        wmParam = new WindowManager.LayoutParams(
                w
                , h
                , WindowManager.LayoutParams.TYPE_TOAST
                , WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                , PixelFormat.TRANSPARENT);
        wmParam.gravity = Gravity.CENTER | Gravity.CENTER;
        windowManager.addView(viewInflate, wmParam);

        img = (ImageView) viewInflate.findViewById(R.id.img_screen_shot);

        if (pathScreenShot.equals("")) {
            wmParam.width = w;
            wmParam.height = h / 3;
            windowManager.updateViewLayout(viewInflate, wmParam);
            img.setVisibility(View.GONE);
        } else {
            final File screenshot = new File(pathScreenShot);
            Glide.with(getBaseContext())
                    .load(screenshot)
                    .into(img);
            fileObserver = new FileObserver(screenshot.getParent()) {
                @Override
                public void onEvent(int i, String s) {
                    Log.d(TAG, "onEvent: i,s: " + i + " path: " + s);
                    if (i == FileObserver.CLOSE_WRITE && s.equals(screenshot.getName())) {
                        AsyncTask asyncTask = new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] objects) {
                                publishProgress();
                                return null;
                            }

                            @Override
                            protected void onProgressUpdate(Object[] values) {
                                Glide.with(getBaseContext())
                                        .load(screenshot)
                                        .into(img);
                                super.onProgressUpdate(values);
                            }
                        };
                        asyncTask.execute();
                        this.stopWatching();
                    }
                }
            };
            fileObserver.startWatching();
        }

        (bt_cancel = (ImageButton) viewInflate.findViewById(R.id.ib_cancel)).setOnClickListener(this);
        (bt_yes = (ImageButton) viewInflate.findViewById(R.id.ib_yes)).setOnClickListener(this);

        llConfirm = (LinearLayout) viewInflate.findViewById(R.id.frm_dialog_confirm_scr_shot);
        llAdd = (LinearLayout) viewInflate.findViewById(R.id.frm_dialog_add_note);
        edNoteTitle = (EditText) viewInflate.findViewById(R.id.ed_note_title);
        edNoteContent = (EditText) viewInflate.findViewById(R.id.ed_note_content);

        llConfirm.setPivotX(w / 2);
        llConfirm.setPivotY(h / 2);
        llConfirm.setRotationY(0);

        llAdd.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_yes:
                if (pathScreenShot.equals("")) {
                    edNoteContent.setFocusable(true);
                    Log.d(TAG, "onClick: " + clb.getPrimaryClip().getItemAt(0).getText());
                    edNoteContent.setText("" + clb.getPrimaryClip().getItemAt(0).getText());
                }

                llAdd.setVisibility(View.VISIBLE);
                llAdd.setPivotX(w / 2);
                llAdd.setPivotY(h / 2);

                (ibSave = (ImageButton) viewInflate.findViewById(R.id.ib_save_note)).setOnClickListener(this);
                (ibCancelNote = (ImageButton) viewInflate.findViewById(R.id.ib_cancel_note)).setOnClickListener(this);

                AsyncTask<Void, int[], Void> anim = new AsyncTask<Void, int[], Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        int[] arr = {0, -90};
                        int j;
                        int i;
                        for (i = 0; i < 90; i++) {
                            arr[0] = i;
                            SystemClock.sleep(3);
                            publishProgress(arr);
                        }

                        for (j = -90; j <= 0; j++) {
                            arr[1] = j;
                            SystemClock.sleep(3);
                            publishProgress(arr);
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(int[]... values) {
                        llConfirm.setRotationY(values[0][0]);
                        llAdd.setRotationY(values[0][1]);
                    }
                };
                anim.execute();

                break;
            case R.id.ib_save_note:
                //Chụp ảnh thì path mới được set trong phương thức fileObserver nếu chỉ là clipboard path được gán = ""
                if (!pathScreenShot.equals("")) {
                    fileObserver.stopWatching();
                    final File fileIn = new File(pathScreenShot);
                    File fileOut = new File(DatabaseManager.PATH_APP_INTERNAL + "/imageSave/" + fileName);
                    databaseManager.copyFile(fileIn, fileOut, false);
                    databaseManager.insert(edNoteTitle.getText().toString(), edNoteContent.getText().toString(), fileOut.getPath(), fileOut.getPath(), DatabaseManager.TYPE_SCREEN_SHOT, null);
                    fileIn.delete();
                    Log.d(TAG, "onClick: path file out: " + fileOut.getPath());
                    pathScreenShot = "";
                } else {
                    databaseManager.insert(edNoteTitle.getText().toString(), edNoteContent.getText().toString(), "", "", DatabaseManager.TYPE_CLIP_BOARD, null);
                }

                windowManager.removeViewImmediate(viewInflate);
                onceRunClipboard = true;
                onceRunScreenShot = true;
                Integer msgUpdateRcv = new Integer(MSG_UPDATE_RCV);
                eventBus.postSticky(msgUpdateRcv);
                break;
            case R.id.ib_cancel:
                windowManager.removeViewImmediate(viewInflate);
                onceRunClipboard = true;
                onceRunScreenShot = true;
                pathScreenShot = "";
                break;
            case R.id.ib_cancel_note:
                windowManager.removeViewImmediate(viewInflate);
                onceRunClipboard = true;
                onceRunScreenShot = true;
                pathScreenShot = "";
                break;
            default:
                break;
        }
    }

    private class ClipBoardListener implements ClipboardManager.OnPrimaryClipChangedListener {
        @Override
        public void onPrimaryClipChanged() {
            if (onceRunClipboard) {
                initView();
            }
        }
    }

    private class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // TODO: 9/20/2016 Copy file có đuôi screenShot vào bộ nhớ máy phát hiện nhầm và hiện WindowManager
            String[] project = {MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.DATA};
            try {
                Cursor cursor = getContentResolver().query(uri, project, null, null, null);
                if (cursor != null) {
                    cursor.moveToLast();
                    String fileNameContentProvider = cursor.getString(cursor.getColumnIndex(project[0]));
                    String data = cursor.getString(cursor.getColumnIndex(project[1]));

                    date = new Date();
                    String timeCurrent = dateFormat.format(date);
                    Log.d(TAG, "onChange: timeCurrent: " + timeCurrent);
                    String year = timeCurrent.split("_")[0];
                    String month = timeCurrent.split("_")[1];
                    String day = timeCurrent.split("_")[2];
                    String hour = timeCurrent.split("_")[3];
                    String minute = timeCurrent.split("_")[4];

                /* Lọc điều kiện khi máy tạo file screenshot
                 * Những screenshot copy từ ngoài vào sẽ k thể trùng thời gian với hệ thống tại lúc này
                 * */
                    if (fileNameContentProvider.contains("Screenshot")
                            && fileNameContentProvider.contains(year)
                            && fileNameContentProvider.contains(month)
                            && fileNameContentProvider.contains(day)
                            && fileNameContentProvider.contains(hour)
                            && fileNameContentProvider.contains(minute)) {
                        Log.d(TAG, "onChange: ScreenShot Listioner fileNameContentProvider: " + fileNameContentProvider);
                        fileName = fileNameContentProvider;
                        pathScreenShot = data;
                        if (onceRunScreenShot) {
                            initView();
                        }
                    } else {
                        Log.d(TAG, "onChange: Doesn't ScreenShot fileNameContentProvider: " + fileNameContentProvider);
                    }
                }
            } catch (IllegalStateException e) {
                Log.d(TAG, "onChange: " + e);
            }
        }
    }
}
