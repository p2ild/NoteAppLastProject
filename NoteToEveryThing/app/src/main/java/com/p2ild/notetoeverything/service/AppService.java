package com.p2ild.notetoeverything.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.p2ild.notetoeverything.DatabaseManagerCopyDb;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.observer.ServiceFogroundNotiComplete;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by duypi on 9/8/2016.
 */
public class AppService extends Service implements View.OnClickListener, ServiceFogroundNotiComplete {
    public static final int ID_NOTI_SERVICE = 6666;
    public static final int SERVICE_BACKUP = 444;
    public static final int SERVICE_RESTORE = 555;
    public static final int SERVICE_DELETE = 666;
    public static final int WIFI_DETECT = 999;
    public static final int STATE_CONNECTED = 111;
    public static final int STATE_DISCONNECT = 1010;
    public static final String KEY_SERVICE_INTERFACE = "KEY_SERVICE_INTERFACE";

    public static final int NOTI_SWITCH = 10000;
    public static final int NOTI_CLIPBOAD = 11000;
    public static final int NOTI_SCREENSHOT = 12000;
    public static final int ID_NOTI_BACKUP = 77;
    public static final int ID_NOTI_RESTORE = 88;
    public static final int ID_NOTI_DELETE = 99;
    private static final String TAG = AppService.class.getSimpleName();
    private static final String KEY_SERVICE = "KEY_SERVICE";
    private static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    private static final String SWITCH_SCREENSHOT = "SWITCH_SCREENSHOT";
    private static final String SWITCH_CLIPBOARD = "SWITCH_CLIPBOARD";
    // TODO: 2016-10-05 Chuyển cách import database
    private static DatabaseManagerCopyDb databaseManagerCopyDb;
    private static TaskStackBuilder taskStackBuilder;
    private FileObserver fileObserver;
    private InputMethodManager ipm;
    private ImageView img;
    private ImageButton bt_cancel, bt_yes;
    private LinearLayout llConfirm, llAdd;
    private EditText edNoteTitle, edNoteContent;
    private String pathScreenShot = "";
    private ImageButton ibSave, ibCancelNote;
    private int w, h;
    private String pathInternal;
    private String fileName;
    private EventBus eventBus;
    private SharedPreferences sharedPreferences;
    private ClipboardManager clb;
    private ClipData primaryClip;
    private boolean onceRunClipboard; // tránh initViewScreenAndClipListener được khởi tạo nhiều lần khi vào phương thức onPrimaryClipChanged
    private boolean onceRunScreenShot; // tránh initViewScreenAndClipListener được khởi tạo nhiều lần khi vào phương thức onPrimaryClipChanged
    private boolean onceRunSwich; // tránh initViewScreenAndClipListener được khởi tạo nhiều lần khi vào phương thức onPrimaryClipChanged
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private MyContentObserver myContentObserver;
    private SimpleDateFormat dateFormat;
    private Date date;
    private NotificationService notificationService, notiAllwayShow;
    private boolean stateScreenShot = false, stateClipboard = false;
    private int x, y;
    private float touchedX, touchedY;
    private MyViewGroup myViewGroupSwitchMode, myViewGroupScListener;
    private View viewInflateSwitchMode, viewInflateScListener;
    private WindowManager windowManager;
    private TextView tvCopy;
    private ImageButton ibMove;
    private ImageButton ibExpand;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override

    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: ");

        databaseManagerCopyDb = new DatabaseManagerCopyDb(this);
        eventBus = EventBus.getDefault();

        sharedPreferences = getSharedPreferences(SHARE_PREFERENCE, MODE_PRIVATE);

        myContentObserver = new MyContentObserver(handler);

        clb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        primaryClip = clb.getPrimaryClip();

        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (taskStackBuilder == null) {
            taskStackBuilder = TaskStackBuilder.create(this);
        }
        notiAllwayShow = new NotificationService(AppService.this, ID_NOTI_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //ScreenShot và Clipboard
        allwayRunService();

        //Backup,Restore,Delete
        runServiceIfClick(intent);

        return START_STICKY;
    }

    private void allwayRunService() {
        String strNoti = "";
        //Chế độ chụp màn hình
        Log.d(TAG, "allwayRunService: ");
        if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) != null) {
            switch (sharedPreferences.getString(SWITCH_SCREENSHOT, null)) {
                case "on":
                    this.stateScreenShot = true;
                    strNoti = "ScreenShot mode : On";
                    getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, myContentObserver);
                    onceRunScreenShot = true;
                    break;

                case "off":
                    this.stateScreenShot = false;
                    strNoti = "ScreenShot mode : Off";
                    getContentResolver().unregisterContentObserver(myContentObserver);
                    break;
                default:
                    break;
            }
        } else {
            Log.d(TAG, "allwayRunService: sharepref : KHÔNG CÓ KEY SCREENSHOT");
        }

        //Chế độ clipboard save
        if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) != null) {
            switch (sharedPreferences.getString(SWITCH_CLIPBOARD, null)) {
                case "on":
                    this.stateClipboard = true;
                    strNoti += "\nClipboard mode : On\n\n" +
                            "Click to change mode";
                    clb.addPrimaryClipChangedListener(new ClipBoardListener());
                    onceRunClipboard = true;
                    break;
                case "off":
                    this.stateClipboard = false;
                    strNoti += "\nClipboard mode : Off\n\n" +
                            "Click to change mode";
                    clb.removePrimaryClipChangedListener(new ClipBoardListener());
                    break;
                default:
                    break;
            }
        }

        notiAllwayShow.setTitleNoti(strNoti);
        startForeground(ID_NOTI_SERVICE, notiAllwayShow.build());
    }

    private void runServiceIfClick(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "runServiceIfClick: intent!=null , getIntExtra= " + intent.getIntExtra(NotificationService.NOTI_SHOW_TYPE, -1));
            int type = intent.getIntExtra(NotificationService.NOTI_SHOW_TYPE, -1);
            switch (type) {
                case NOTI_SWITCH:
                    initViewSwitchMode();
                    break;
                case NOTI_CLIPBOAD:
                    initViewScreenAndClipListener(type);
                    break;
                case NOTI_SCREENSHOT:
                    initViewScreenAndClipListener(type);
                    break;
                default:
                    break;
            }

            switch (intent.getIntExtra(KEY_SERVICE, 0)) {
                case SERVICE_BACKUP:
                    if (databaseManagerCopyDb.readAllData("All").getCount() > 0) {
                        notificationService = new NotificationService(this, SERVICE_BACKUP);
                        NotificationService.upToNotify(ID_NOTI_BACKUP, notificationService.build(), AppService.this);
                        databaseManagerCopyDb.backupAllNote(this, notificationService, eventBus);
                    } else {
                        Toast.makeText(AppService.this, "Không tồn tại dữ liệu nào", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SERVICE_RESTORE:
                    notificationService = new NotificationService(this, SERVICE_RESTORE);
                    NotificationService.upToNotify(ID_NOTI_RESTORE, notificationService.build(), AppService.this);
                    databaseManagerCopyDb.restoreAllNote(this, notificationService, eventBus);
                    break;
                case SERVICE_DELETE:
                    notificationService = new NotificationService(this, SERVICE_DELETE);
                    NotificationService.upToNotify(ID_NOTI_DELETE, notificationService.build(), AppService.this);
                    databaseManagerCopyDb.delAllDataTable(notificationService, eventBus);
                    break;
                default:
                    break;
            }
        } else {
            Log.d(TAG, "onStartCommand: INTENT == null || KHÔNG CLICK BACKUP/RESTORE/DELETE");
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(AppService.this, "NoteEverything stop service", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public void initViewSwitchMode() {
        if (!onceRunSwich) {
            onceRunSwich = true;

            final WindowManager.LayoutParams paramsSwitchMode = new WindowManager.LayoutParams();
            paramsSwitchMode.width = WindowManager.LayoutParams.WRAP_CONTENT;
            paramsSwitchMode.height = WindowManager.LayoutParams.WRAP_CONTENT;
            paramsSwitchMode.gravity = Gravity.CENTER;
            paramsSwitchMode.type = WindowManager.LayoutParams.TYPE_TOAST;
            paramsSwitchMode.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            paramsSwitchMode.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            paramsSwitchMode.format = PixelFormat.TRANSLUCENT;

            myViewGroupSwitchMode = new MyViewGroup(this);
            viewInflateSwitchMode = View.inflate(AppService.this, R.layout.dialog_service_switch_mode, myViewGroupSwitchMode);
            final Switch swScrShot = (Switch) viewInflateSwitchMode.findViewById(R.id.sw_scr_shot_service);
            swScrShot.setChecked(stateScreenShot);
            (swScrShot).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        sharedPreferences.edit().putString(SWITCH_SCREENSHOT, "on").apply();
                    } else {
                        sharedPreferences.edit().putString(SWITCH_SCREENSHOT, "off").apply();
                    }
                    allwayRunService();
                }
            });

            final Switch swClipBoard = (Switch) viewInflateSwitchMode.findViewById(R.id.sw_clip_board_service);
            swClipBoard.setChecked(stateClipboard);
            swClipBoard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "on").apply();
                    } else {
                        sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "off").apply();
                    }
                    allwayRunService();
                }
            });

            ((ImageButton) viewInflateSwitchMode.findViewById(R.id.ib_exit_wm)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    windowManager.removeViewImmediate(myViewGroupSwitchMode);
                    myViewGroupSwitchMode = null;
                    onceRunSwich = false;
                }
            });

            final LinearLayout llLayout = (LinearLayout) viewInflateSwitchMode.findViewById(R.id.ll_service_switch);
            ibExpand = (ImageButton) viewInflateSwitchMode.findViewById(R.id.ib_show_expand);
            ibExpand.setImageResource(R.drawable.ic_back_show);
            (ibExpand).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkPermissionDrawOverlay(swClipBoard)) {
                        if (llLayout.getVisibility() == View.VISIBLE) {
                            Animation animationNextHide = AnimationUtils.loadAnimation(AppService.this, R.anim.rotation_ib_expand_next_hide);
                            ibExpand.startAnimation(animationNextHide);
                            YoYo.with(Techniques.FadeOutLeft)
                                    .withListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            llLayout.setVisibility(View.GONE);
                                            ibExpand.setImageResource(R.drawable.ic_back_show);
                                            windowManager.updateViewLayout(myViewGroupSwitchMode, paramsSwitchMode);
                                            super.onAnimationEnd(animation);
                                        }
                                    })
                                    .duration(500)
                                    .playOn(llLayout);

                        } else {
                            llLayout.setVisibility(View.VISIBLE);
                            windowManager.updateViewLayout(myViewGroupSwitchMode, paramsSwitchMode);
                            Animation animationBackShow = AnimationUtils.loadAnimation(AppService.this, R.anim.rotation_ib_expand_back_show);
                            ibExpand.startAnimation(animationBackShow);
                            YoYo.with(Techniques.FadeInLeft)
                                    .withListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            ibExpand.setImageResource(R.drawable.ic_back_show);
                                            super.onAnimationEnd(animation);
                                        }
                                    })
                                    .duration(500)
                                    .playOn(llLayout);
                        }
                    }

                }
            });
            ibMove = (ImageButton) viewInflateSwitchMode.findViewById(R.id.ib_move);
            (ibMove).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    WindowManager.LayoutParams update = paramsSwitchMode;
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x = update.x;
                            y = update.y;
                            touchedX = motionEvent.getRawX();
                            touchedY = motionEvent.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            update.x = (int) (x + (motionEvent.getRawX() - touchedX));
                            update.y = (int) (y + (motionEvent.getRawY() - touchedY));
                            windowManager.updateViewLayout(myViewGroupSwitchMode, update);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            windowManager.addView(myViewGroupSwitchMode, paramsSwitchMode);
            onceRunSwich = true;
        }
    }

    private boolean checkPermissionDrawOverlay(Switch sw) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(AppService.this)) {
                sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "on").apply();
                return true;
            } else {
                sw.setChecked(false);
                Toast.makeText(AppService.this, "Click NoteToEveryThing and switch Permit drawing over other app", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return false;
            }
        } else {
            sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "on").apply();
            return true;
        }
    }

    public void initViewScreenAndClipListener(int type) {
        onceRunClipboard = false;
        onceRunScreenShot = false;

        WindowManager.LayoutParams paramsScListener = new WindowManager.LayoutParams();
        paramsScListener.width = WindowManager.LayoutParams.MATCH_PARENT;
        paramsScListener.height = WindowManager.LayoutParams.MATCH_PARENT;

        paramsScListener.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        paramsScListener.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAGS_CHANGED;
        paramsScListener.format = PixelFormat.TRANSLUCENT;
        paramsScListener.gravity = Gravity.CENTER;

        switch (type) {
            case NOTI_CLIPBOAD:
                myViewGroupScListener = new MyViewGroup(this);
                viewInflateScListener = View.inflate(this, R.layout.dialog_service_listener, myViewGroupScListener);
                myViewGroupScListener.setTag(NOTI_CLIPBOAD);

                img = (ImageView) viewInflateScListener.findViewById(R.id.img_screen_shot);
                tvCopy = (TextView) viewInflateScListener.findViewById(R.id.tv_copy_content);
                paramsScListener.height = WindowManager.LayoutParams.WRAP_CONTENT;
                img.setVisibility(View.GONE);
                tvCopy.setText(tvCopy.getText() + "\n" + clb.getPrimaryClip().getItemAt(0).getText());


                windowManager.addView(myViewGroupScListener, paramsScListener);
                break;
            case NOTI_SCREENSHOT:
                myViewGroupScListener = new MyViewGroup(this);
                myViewGroupScListener.setTag(NOTI_SCREENSHOT);
                viewInflateScListener = View.inflate(this, R.layout.dialog_service_listener, myViewGroupScListener);

                img = (ImageView) viewInflateScListener.findViewById(R.id.img_screen_shot);
                tvCopy = (TextView) viewInflateScListener.findViewById(R.id.tv_copy_content);

                tvCopy.setVisibility(View.GONE);
                final File screenshot = new File(pathScreenShot);
                Log.d(TAG, "initViewScreenAndClipListener: pathScreenShot: "+pathScreenShot);
                Glide.with(getBaseContext())
                        .load(screenshot)
                        .into(img);
//                fileObserver = new FileObserver(screenshot.getParent()) {
//                    @Override
//                    public void onEvent(int i, String s) {
//                        Log.d(TAG, "onEvent: i,s: " + i + " path: " + s);
//                        if (i == FileObserver.CLOSE_WRITE && s.equals(screenshot.getName())) {
//                            AsyncTask asyncTask = new AsyncTask() {
//                                @Override
//                                protected Object doInBackground(Object[] objects) {
//                                    publishProgress();
//                                    return null;
//                                }
//
//                                @Override
//                                protected void onProgressUpdate(Object[] values) {
//                                    Glide.with(getBaseContext())
//                                            .load(screenshot)
//                                            .into(img);
//                                    super.onProgressUpdate(values);
//                                }
//                            };
//                            asyncTask.execute();
//                            this.stopWatching();
//                        }
//                    }
//                };
//                try{
//                    fileObserver.startWatching();
//                }catch (Exception e){
//                    Log.d(TAG, "initViewScreenAndClipListener: fileObserverStartWatching: "+e);
//                }

                windowManager.addView(myViewGroupScListener, paramsScListener);
                break;
            default:
                break;
        }

        (bt_yes = (ImageButton) viewInflateScListener.findViewById(R.id.ib_yes)).setOnClickListener(this);
        (bt_cancel = (ImageButton) viewInflateScListener.findViewById(R.id.ib_cancel)).setOnClickListener(this);

        llConfirm = (LinearLayout) viewInflateScListener.findViewById(R.id.frm_dialog_confirm_scr_shot);
        llAdd = (LinearLayout) viewInflateScListener.findViewById(R.id.frm_dialog_add_note);
        edNoteTitle = (EditText) viewInflateScListener.findViewById(R.id.ed_note_title);
        edNoteContent = (EditText) viewInflateScListener.findViewById(R.id.ed_note_content);

        (ibSave = (ImageButton) viewInflateScListener.findViewById(R.id.ib_save_note)).setOnClickListener(this);
        (ibCancelNote = (ImageButton) viewInflateScListener.findViewById(R.id.ib_cancel_note)).setOnClickListener(this);

        llConfirm.setPivotX(w / 2);
        llConfirm.setPivotY(h / 2);
        llConfirm.setRotationY(0);

        llAdd.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_yes:
                if ((int)myViewGroupScListener.getTag() == NOTI_CLIPBOAD) {
                    edNoteContent.setFocusable(true);
                    edNoteContent.setText("" + clb.getPrimaryClip().getItemAt(0).getText());
                    tvCopy.setVisibility(View.GONE);
                }

                llAdd.setVisibility(View.VISIBLE);
                llAdd.setPivotX(w / 2);
                llAdd.setPivotY(h / 2);

                AsyncTask<Void, int[], Void> anim = new AsyncTask<Void, int[], Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        int[] arr = {0, -90};
                        int j;
                        int i;
                        for (i = 0; i <= 90; i++) {
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
                        if (values[0][0] == 90) {
                            llConfirm.setVisibility(View.GONE);
                        }
                        llConfirm.setRotationY(values[0][0]);
                        llAdd.setRotationY(values[0][1]);

                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                    }
                };
                anim.execute();
                break;

            case R.id.ib_save_note:
                //Chụp ảnh thì path mới được set trong phương thức fileObserver nếu chỉ là clipboard path được gán = ""
                // TODO: 2016-10-14 Chỗ này nếu người dùng clipboard trước và scshot sau . Sau đó ấn vào clipboard trước thì clipboard vẫn hiện mà screenshot cũng vẫn hiện
                if ((int)myViewGroupScListener.getTag() == NOTI_SCREENSHOT) {
//                    fileObserver.stopWatching();
                    final File fileIn = new File(pathScreenShot);
                    File fileOut = new File(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave/" + fileName);
                    databaseManagerCopyDb.copyFile(fileIn, fileOut, false);
                    databaseManagerCopyDb.insert(edNoteTitle.getText().toString(), edNoteContent.getText().toString(), fileOut.getPath(), fileOut.getPath(), DatabaseManagerCopyDb.TYPE_SCREEN_SHOT, "", "", "", true);
                    fileIn.delete();
                    windowManager.removeViewImmediate(myViewGroupScListener);
                } else {
                    databaseManagerCopyDb.insert(edNoteTitle.getText().toString(), edNoteContent.getText().toString(), "", "", DatabaseManagerCopyDb.TYPE_CLIP_BOARD, "", "", "", true);
                    windowManager.removeViewImmediate(myViewGroupScListener);
                }

                onceRunClipboard = true;
                onceRunScreenShot = true;
                break;
            case R.id.ib_cancel:
                windowManager.removeViewImmediate(myViewGroupScListener);
                onceRunClipboard = true;
                onceRunScreenShot = true;
                break;
            case R.id.ib_cancel_note:
                windowManager.removeViewImmediate(myViewGroupScListener);
                onceRunClipboard = true;
                onceRunScreenShot = true;
                break;
            default:
                break;
        }
    }

    public static class WifiBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();

            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();
                String ssid = "";
                ssid = info.getSSID();
                try {
                    ArrayList<NoteItem> result = databaseManagerCopyDb.readDataWithWifi(ssid);
                    if (!ssid.equals("") && result != null) {
                        int noteDetect = result.size();
                        if (noteDetect > 0) {
                            String contentTitle = noteDetect + " note được tìm thấy [Xem thêm]";
                            NoteItem infoNoteLast = result.get(0);
                            String contentText = "Mới nhất: " + infoNoteLast.getNoteTitle() + "\n" + infoNoteLast.getNoteContent();
                            String path = infoNoteLast.getPathThumbnail();
                            // TODO: 2016-10-07 chưa rõ NotificationCompat hỗ trợ từ version bao nhiêu
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            NotificationService builder = new NotificationService(context, result, WIFI_DETECT, noteDetect + "", contentTitle, contentText, path);
                            NotificationService.upToNotify(11, builder.build(), context);
                        }
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "onReceive: Chưa cấp quyền");
                }
            }
        }
    }

    private class ClipBoardListener implements ClipboardManager.OnPrimaryClipChangedListener {
        @Override
        public void onPrimaryClipChanged() {
            if (onceRunClipboard) {
//                initViewScreenAndClipListener();
                onceRunClipboard = false;
                String noteContentClipBoard = clb.getPrimaryClip().getItemAt(0).getText() + "";
                NotificationService notificationService = new NotificationService(AppService.this, null, NOTI_CLIPBOAD, "", "", noteContentClipBoard, "");
                NotificationService.upToNotify(222, notificationService.build(), AppService.this);
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
                            onceRunScreenShot = false;
//                            initViewScreenAndClipListener();
                            NotificationService notificationService = new NotificationService(AppService.this, null, NOTI_SCREENSHOT, "", "", "", pathScreenShot);
                            NotificationService.upToNotify(333, notificationService.build(), AppService.this);
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

    class MyViewGroup extends FrameLayout {
        public MyViewGroup(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                windowManager.removeViewImmediate(this);
                onceRunClipboard = true;
                onceRunScreenShot = true;
                onceRunSwich = true;
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
