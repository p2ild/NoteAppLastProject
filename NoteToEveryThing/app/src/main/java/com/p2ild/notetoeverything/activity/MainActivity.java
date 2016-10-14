package com.p2ild.notetoeverything.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.p2ild.notetoeverything.DatabaseManagerCopyDb;
import com.p2ild.notetoeverything.MessageEventBus;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.adapter.RecycleNoteAdapter;
import com.p2ild.notetoeverything.frgment.FragmentMain;
import com.p2ild.notetoeverything.frgment.FrgAddNote;
import com.p2ild.notetoeverything.frgment.FrgCapture;
import com.p2ild.notetoeverything.frgment.FrgEdit;
import com.p2ild.notetoeverything.locationmanager.WifiGpsManager;
import com.p2ild.notetoeverything.other.DataSerializable;
import com.p2ild.notetoeverything.service.AppService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_ARR_DATA = "KEY_ARR_DATA";
    public static final String KEY_OBJECT_DB = "KEY_OBJECT_DB";
    public static final String KEY_POSTION_CLICK = "KEY_POSTION_CLICK";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    private static final String SWITCH_SCREENSHOT = "SWITCH_SCREENSHOT";
    private static final String SWITCH_CLIPBOARD = "SWITCH_CLIPBOARD";
    private static final int REQUEST_PICK_IMAGE = 111;
    private static final int SERVICE_SCREEN_SHOOT = 333;
    private static final int MSG_UPDATE_RCV = 777;
    private static final int SERVICE_BACKUP = 444;
    private static final int SERVICE_RESTORE = 555;
    private static final int SERVICE_DELETE = 666;
    private static final String KEY_SERVICE = "KEY_SERVICE";
    private static final String KEY_TYPE_SAVE = "KEY_TYPE_SAVE";
    private static final int REQUEST_PERMISSION_DRAW_OVER_APP = 888;
    private static final int REQUEST_PERMISSION = 111;

    private ImageButton btAddNote;

    private Button btBackup, btRestore, btReset, btMapView;

    private boolean isExit;
    //Binh thuong hom nao a cung ngu muon the nay a
    private WifiGpsManager wifiGpsManager;
    // TODO: 2016-10-05 Chuyển cách import database
    private DatabaseManagerCopyDb db;
    private DrawerLayout drw;
    private Switch swScreenShots;
    private Switch swClipBoard;
    private SimpleDateFormat dateFormat;
    private Date date;

    private DrawerLayout.DrawerListener myDrawerListener;
    private SharedPreferences sharedPreferences;
    private FragmentMain frgMain;
    private EventBus eventBus;
    private boolean canBackup, canRestore, canDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        initDefaultValue();
        checkAppPermission();
    }

    private void initDefaultValue() {
        canDelete = true;
        canBackup = true;
        canRestore = true;

        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        date = new Date();

        eventBus = EventBus.getDefault();
        eventBus.register(this);

        db = new DatabaseManagerCopyDb(this);

        sharedPreferences = getSharedPreferences(SHARE_PREFERENCE, MODE_PRIVATE);

        if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) == null) {
            sharedPreferences.edit().putString(SWITCH_SCREENSHOT, "off").apply();
        }
        if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) == null) {
            sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "off").apply();
        }
    }

    public void initView() {
        drw = (DrawerLayout) findViewById(R.id.drawer_layout);
        (btBackup = (Button) findViewById(R.id.bt_backup)).setOnClickListener(this);
        (btRestore = (Button) findViewById(R.id.bt_restore)).setOnClickListener(this);
        (btReset = (Button) findViewById(R.id.bt_reset_note)).setOnClickListener(this);
        (btMapView = (Button) findViewById(R.id.bt_map_view)).setOnClickListener(this);
        swScreenShots = (Switch) findViewById(R.id.sw_scr_shot);
        swClipBoard = (Switch) findViewById(R.id.sw_clip_board);
        showFrgMain();
    }

    private void initAll() {
        //Khởi tạo cơ sở dữ liệu load ảnh thêm sửa xóa
        Log.d(TAG, "initAll: ");
        initView();
        drw.addDrawerListener(myDrawerListener);
        initScreenShot();
        initClipBoard();
        //Khởi tạo view
        isExit = false;
        initOpenCV();

    }


    private void checkAppPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission_group.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.CAMERA
                                , Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.INTERNET
                        }, REQUEST_PERMISSION
                );
            }
        } else {
            initAll();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0) {
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(MainActivity.this, "Vui lòng đồng ý hết các quyền để chương trình chạy không gặp lỗi", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            initAll();
        }

        if (requestCode == REQUEST_PERMISSION_DRAW_OVER_APP && grantResults.length > 0) {
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(MainActivity.this, "Nếu không cho phép quyền này chế độ screen shot và clipboard sẽ không hoạt động", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }
            }
        }
    }

    private void initScreenShot() {
        (swScreenShots).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sharedPreferences.edit().putString(SWITCH_SCREENSHOT, "on").apply();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(MainActivity.this)) {
                            Intent itScreenShot = new Intent(MainActivity.this, AppService.class);
                            startService(itScreenShot);
                        } else {
                            Toast.makeText(MainActivity.this, "Find app NoteEveryThing on this list\n" +
                                    "And switch Permit drawing over other app to on", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                        }
                    } else {
                        sharedPreferences.edit().putString(SWITCH_SCREENSHOT, "on").apply();
                        Intent itScreenShot = new Intent(MainActivity.this, AppService.class);
                        startService(itScreenShot);
                    }
                } else {
                    sharedPreferences.edit().putString(SWITCH_SCREENSHOT, "off").apply();
                    Intent itScreenShot = new Intent(MainActivity.this, AppService.class);
                    startService(itScreenShot);
                }
            }
        });

        if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) != null) {
            switch (sharedPreferences.getString(SWITCH_SCREENSHOT, null)) {
                case "on":
                    swScreenShots.setChecked(true);
                    break;
                case "off":
                    swScreenShots.setChecked(false);
                    break;
                default:
                    break;
            }
        }
    }

    private void initClipBoard() {
        (swClipBoard).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "on").apply();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(MainActivity.this)) {
                            Intent itClipboard = new Intent(MainActivity.this, AppService.class);
                            startService(itClipboard);
                        } else {
                            Toast.makeText(MainActivity.this, "Find app NoteEveryThing on this list\n" +
                                    "And switch Permit drawing over other app to on", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                        }
                    } else {
                        sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "on").apply();
                        Intent itClipboard = new Intent(MainActivity.this, AppService.class);
                        startService(itClipboard);
                    }
                } else {
                    sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "off").apply();
                    Intent itClipboard = new Intent(MainActivity.this, AppService.class);
                    startService(itClipboard);
                }
            }
        });

        if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) != null) {
            switch (sharedPreferences.getString(SWITCH_CLIPBOARD, null)) {
                case "on":
                    swClipBoard.setChecked(true);
                    break;
                case "off":
                    swClipBoard.setChecked(false);
                    break;
                default:
                    break;
            }
        }
    }

    public boolean showArrayWifiDetect() {
        if (getIntent().getSerializableExtra(AppService.WIFI_DETECT + "") != null) {
            ArrayList arrayList = (ArrayList) ((DataSerializable) getIntent().getSerializableExtra(AppService.WIFI_DETECT + "")).getData();
            frgMain.getRecycleNoteAdapter().swapDataUseArray(arrayList);
            Log.d(TAG, "showArrayWifiDetect: Chạy đến cuối");
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        initView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) != null) {
                    if (sharedPreferences.getString(SWITCH_SCREENSHOT, null).equals("on")) {
                        swScreenShots.setChecked(true);
                        Intent itScreenShot = new Intent(MainActivity.this, AppService.class);
                        startService(itScreenShot);
                    } else {
                        swScreenShots.setChecked(false);
                        Intent itScreenShot = new Intent(MainActivity.this, AppService.class);
                        startService(itScreenShot);
                    }
                }

                if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) != null) {
                    if (sharedPreferences.getString(SWITCH_CLIPBOARD, null).equals("on")) {
                        swClipBoard.setChecked(true);
                        Intent itClipboard = new Intent(MainActivity.this, AppService.class);
                        startService(itClipboard);
                    } else {
                        swClipBoard.setChecked(false);
                        Intent itClipboard = new Intent(MainActivity.this, AppService.class);
                        startService(itClipboard);
                    }
                }
            }
        } else {
            if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) != null) {
                if (sharedPreferences.getString(SWITCH_SCREENSHOT, null).equals("on")) {
                    swScreenShots.setChecked(true);
                    Intent itScreenShot = new Intent(MainActivity.this, AppService.class);
                    startService(itScreenShot);
                } else {
                    swScreenShots.setChecked(false);
                    Intent itScreenShot = new Intent(MainActivity.this, AppService.class);
                    startService(itScreenShot);
                }
            }

            if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) != null) {
                if (sharedPreferences.getString(SWITCH_CLIPBOARD, null).equals("on")) {
                    swClipBoard.setChecked(true);
                    Intent itClipboard = new Intent(MainActivity.this, AppService.class);
                    startService(itClipboard);
                } else {
                    swClipBoard.setChecked(false);
                    Intent itClipboard = new Intent(MainActivity.this, AppService.class);
                    startService(itClipboard);
                }
            }
        }
        super.onResume();
    }

    private void resetAllDataBase() {
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        alert.setTitle("Cảnh báo!");
        alert.setMessage("Nếu cần lưu dữ liệu hãy sao lưu lại. Bạn có chắc chắn muốn xóa hết các note đã lưu không?");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent itResetData = new Intent(MainActivity.this, AppService.class);
                itResetData.putExtra(KEY_SERVICE, SERVICE_DELETE);
                startService(itResetData);
                alert.dismiss();
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alert.dismiss();
            }
        });
        alert.show();
    }

    private void initOpenCV() {
        // TODO: 8/25/2016 Khởi tạo OpenCv xử lý ảnh
    }


    private void showFrgMain() {
//        removeAllFragment();
        // TODO: 8/31/2016 ---Done--- Update data recycle view chưa sử dụng notifyDataSetChange
        if (frgMain == null) {
            frgMain = new FragmentMain();
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main, frgMain)
                .commit();

        if (myDrawerListener == null) {
            myDrawerListener = new DrawerLayout.DrawerListener() {
                RecyclerView.OnItemTouchListener onItemTouchListener;

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    if (frgMain != null && frgMain.getRcvOnItemTouchListioner() != null) {
                        onItemTouchListener = frgMain.getRcvOnItemTouchListioner();
                        frgMain.getCustomStaggeredGridLayoutManager().setCanScroll(false);
                        frgMain.getRcv().removeOnItemTouchListener(onItemTouchListener);
                    }
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    if (!frgMain.isHidden() && frgMain.getRcvOnItemTouchListioner() != null) {
                        onItemTouchListener = frgMain.getRcvOnItemTouchListioner();
                        frgMain.getCustomStaggeredGridLayoutManager().setCanScroll(true);
                        frgMain.getRcv().addOnItemTouchListener(onItemTouchListener);
                    }
                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            };
        }
    }

    public DatabaseManagerCopyDb getDb() {
        return db;
    }

    public String getTypeSave() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARE_PREFERENCE, Context.MODE_PRIVATE);
        String typeSave = sharedPreferences.getString(KEY_TYPE_SAVE, "");
        return typeSave;
    }

    public void showFrgAddNote(final String imgPath, final String imgThumbnailPath, final String typeSave) {
        FrgAddNote frgAdd = new FrgAddNote(imgPath, imgThumbnailPath, typeSave);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main, frgAdd)
                .commit();
    }

    public void showFrgCapture() {
        FrgCapture frgCapture = new FrgCapture();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main, frgCapture)
                .commit();
    }

    public void showFrgEdit(String title, String content, String pathImg) {
        FrgEdit frgEdit = new FrgEdit(title, content, pathImg);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main, frgEdit)
                .commit();
    }

    public void insertToDataBase(String noteTitle, String noteContent, final String imgPath, final String imgThumbnailPath, final String typeSave, String latlong) {
        db.insert(noteTitle, noteContent, imgPath, imgThumbnailPath, typeSave, latlong, "", "", true);
        showFrgMain();
    }

    public void updateDataBase(String oldTitle, String newTitle, String newContent) {
        db.update(oldTitle, newTitle, newContent);
        showFrgMain();
    }

    public void deleteDb(String noteTitleDelete, String noteContentDelete, String noteImg, String noteThumbnail, String typeSave, String latlong, String alarm, String wifiName) {
        db.deleteNote(noteTitleDelete, noteContentDelete, noteImg, noteThumbnail, typeSave, latlong, alarm, wifiName);
        showFrgMain();
    }

    @Override
    public void onBackPressed() {
        showFrgMain();
        if (isExit) {
            super.onBackPressed();
        } else
            Toast.makeText(MainActivity.this, "- Press back to refesh data\n- Double press back to exit", Toast.LENGTH_SHORT).show();
        isExit = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isExit = false;
            }
        }, 500);
    }

    public void startNoteActivity(ArrayList<NoteItem> data, int position) {
        // 23TODO: 9/19/2016 Mở ViewPager k ra ảnh
        Intent intent = new Intent(MainActivity.this, NoteContentActivity.class);
        intent.putExtra(KEY_ARR_DATA, new DataSerializable(data));
        intent.putExtra(KEY_POSTION_CLICK, position);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.bt_backup:
                if (canBackup) {
                    canBackup = false;
                    Intent itBackup = new Intent(this, AppService.class);
                    itBackup.putExtra(KEY_SERVICE, SERVICE_BACKUP);

                    startService(itBackup);
                    drw.closeDrawer(Gravity.LEFT);
                    String type = sharedPreferences.getString(FragmentMain.KEY_TYPE_SAVE, null);
                    if (type != null) {
                        frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb(type));
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Backup is running", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_restore:
                if (canRestore) {
                    canRestore = false;
                    //chay may ao 7.0 thu no chet khong
//                E lai chua cai thang 7.0 r @@
                    Intent itRestore = new Intent(this, AppService.class);
                    itRestore.putExtra(KEY_SERVICE, SERVICE_RESTORE);
                    startService(itRestore);
                    String type1 = sharedPreferences.getString(FragmentMain.KEY_TYPE_SAVE, null);
                    if (type1 != null) {
                        frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb(type1));
                    }
                    drw.closeDrawer(Gravity.LEFT);
                } else {
                    Toast.makeText(MainActivity.this, "Restore is running", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_reset_note:
                if (canDelete) {
                    canDelete = false;
                    resetAllDataBase();
                    drw.closeDrawer(Gravity.LEFT);
                } else {
                    Toast.makeText(MainActivity.this, "Delete is running", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_map_view:
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                ArrayList<NoteItem> noteItemArrayList = RecycleNoteAdapter.cursorToArrayList(db.readAllData("All"));
                intent.putExtra(KEY_OBJECT_DB, new DataSerializable(noteItemArrayList));
                startActivity(intent);
                drw.closeDrawer(Gravity.LEFT);
                break;
            default:
                break;
        }
    }

    public void showDrw() {
        drw.openDrawer(Gravity.LEFT);
    }

    public void showGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: 9/19/2016 Pick 2 ảnh thành 1 ảnh
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
            if (data == null) {
                return;
            }

            String fileNameImage = "note" + dateFormat.format(date) + ".jpg";
            String fileNameImageThumbnail = "note" + dateFormat.format(date) + "_thumbnail.jpg";
            db.createDefaultFolderInternal();
            try {
                InputStream inputStreamImg = getContentResolver().openInputStream(data.getData());

                final File fileOutputImg = new File(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave/" + fileNameImage);
                final File fileOutputThumbnail = new File(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave/" + fileNameImageThumbnail);
                FileObserver fileObserver = new FileObserver(DatabaseManagerCopyDb.PATH_APP_INTERNAL + "/imageSave") {
                    @Override
                    public void onEvent(int event, String path) {
                        Log.d(TAG, "onEvent: " + path);
                        if (event == FileObserver.CLOSE_WRITE && path.equals(fileOutputImg.getName())) {
                            showFrgAddNote(fileOutputImg.getPath(), fileOutputThumbnail.getPath(), DatabaseManagerCopyDb.TYPE_GALLERY);
                            this.stopWatching();
                        }
                    }
                };
                fileObserver.startWatching();
                FileOutputStream outputImg = new FileOutputStream(fileOutputImg);
                byte[] b = new byte[1024];
                int length;
                while ((length = inputStreamImg.read(b)) != -1) {
                    outputImg.write(b, 0, length);
                }
                inputStreamImg.close();
                outputImg.close();

                InputStream inputStreamThumbnail = getContentResolver().openInputStream(data.getData());
                Bitmap src = BitmapFactory.decodeStream(inputStreamThumbnail);
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(src, src.getWidth() / 3, src.getHeight() / 3);

                FileOutputStream outputThumbnail = new FileOutputStream(fileOutputThumbnail);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputThumbnail);

                inputStreamThumbnail.close();
                outputThumbnail.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
//    public void onStickyNoteEvent() {
//        Integer integer = eventBus.getStickyEvent(Integer.class);
//        Log.d(TAG, "onStickyNoteEvent: " + integer);
//        if(integer!=null && integer==)
//    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    public Cursor swapDb(String typeSelect) {
        return db.readAllData(typeSelect);
    }

    @Subscribe(sticky = true)
    public void onEvent(MessageEventBus bus) {
        Log.d(TAG, "onEvent: onEvent Bus");
        try {
            switch (bus.getType()) {
                case AppService.SERVICE_BACKUP:
                    frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb("All"));
                    canBackup = true;
                    break;
                case AppService.SERVICE_DELETE:
                    frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb("All"));
                    canDelete = true;
                    break;
                case AppService.SERVICE_RESTORE:
                    canRestore = true;
                    frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb("All"));
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
        }

    }
}
