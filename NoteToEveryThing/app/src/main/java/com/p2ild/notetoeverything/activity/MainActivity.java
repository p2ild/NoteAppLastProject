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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
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

import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.MessageEventBus;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.frgment.FragmentMain;
import com.p2ild.notetoeverything.frgment.FrgAddNote;
import com.p2ild.notetoeverything.frgment.FrgCapture;
import com.p2ild.notetoeverything.frgment.FrgEdit;
import com.p2ild.notetoeverything.locationmanager.MapManager;
import com.p2ild.notetoeverything.observer.NeedUpdateData;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, NeedUpdateData {

    public static final String KEY_ARR_DATA = "KEY_ARR_DATA";
    public static final String KEY_OBJECT_DB = "KEY_OBJECT_DB";
    public static final String KEY_POSTION_CLICK = "KEY_POSTION_CLICK";
    public static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    public static final int ORIENT_HORIZONTAL = 2222;
    public static final int ORIENT_VERTICAL = 3333;
    public static final int REQUEST_PERMISSION_DRAW_OVER_APP = 888;
    private static final String TAG = MainActivity.class.getSimpleName();
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
    private static final int REQUEST_PERMISSION = 111;
    int showFrgMain = 0;
    private int orientDevice;
    private ImageButton btAddNote;
    private Button btBackup, btRestore, btReset, btMapView;
    private boolean isExit;
    //Binh thuong hom nao a cung ngu muon the nay a
    private MapManager mapManager;
    // TODO: 2016-10-05 Chuyển cách import database
    private DatabaseManager db;
    private DrawerLayout drw;
    private Switch swScreenShots;
    private Switch swClipBoard;
    private SimpleDateFormat dateFormat;
    private Date date;
    private DrawerLayout.DrawerListener myDrawerListener;
    private SharedPreferences sharedPreferences;
    private FragmentMain frgMain;
    private EventBus eventBus;
    private boolean canDelete, canBackup, canRestore;
    private SensorManager sensorManager;
    private Sensor mDefaultSensor;
    private FrgAddNote frgAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        initDefaultValue();
        checkAppPermission();//Check permission, k đủ permission thì finish()
    }

    /**
     * Gán các đối tượng luôn được set giá trị khi khởi tạo activity (Gọi ở onCreate)
     */
    private void initDefaultValue() {
        canDelete = true;//Không cho ấn del lần thứ 2 khi đang chạy
        canBackup = true;//Không cho ấn backup lần thứ 2 khi đang chạy
        canRestore = true;//Không cho ấn restore lần thứ 2 khi đang chạy

        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");//cài đặt định dạng thời gian để thêm vào tên file khi pick file (start activity for result)

        /**Event bus
         * Đăng ký lắng nghe khi có stickyNote gửi đến. Khi Backup,restore,delete xong (gửi từ Service)
         * */
        eventBus = EventBus.getDefault();
        eventBus.register(this);

        db = new DatabaseManager(this, this);//Đối tượng thực thi các method lq đến database

        sharedPreferences = getSharedPreferences(SHARE_PREFERENCE, MODE_PRIVATE);//sharedPreferences

        /**Nếu chưa tồn tại sharePre với key ScreenShot và Clipboard thì gán mặc định là value OFF*/
        if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) == null) {
            sharedPreferences.edit().putString(SWITCH_SCREENSHOT, "off").apply();
        }
        if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) == null) {
            sharedPreferences.edit().putString(SWITCH_CLIPBOARD, "off").apply();
        }


        /**Khi quay máy chụp ảnh sẽ dựa vào mức độ nghiêng theo trục Z để quay camera*/
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mDefaultSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }


    /**
     * Kiểm tra permission nếu máy >= Android M
     * Nếu tồn tại 1 DENIED thì finish trương trình // GRANT hết thì thực thi initAll() ở onRequestPermissionsResult()
     * <p>
     * < ANDROID M thì thực thi initAll(nếu )
     */
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


    /**
     * Khởi tạo tất cả (Khi checkPermission thành công và gọi ở onRequestPermissionsResult())
     */
    private void initAll() {
        startService(new Intent(this, AppService.class));//Chạy vào chương trình là startService luôn
        initView();
        showFrgMain();
        drw.addDrawerListener(myDrawerListener);//Lắng nghe để vô hiệu hóa longClick vào itemView
        initScreenShot();
        initClipBoard();
        //Khởi tạo view
        isExit = false;
    }

    /**
     * Khởi tạo các view (gọi ở initAll)
     */
    public void initView() {
        drw = (DrawerLayout) findViewById(R.id.drawer_layout);
        (btBackup = (Button) findViewById(R.id.bt_backup)).setOnClickListener(this);
        (btRestore = (Button) findViewById(R.id.bt_restore)).setOnClickListener(this);
        (btReset = (Button) findViewById(R.id.bt_reset_note)).setOnClickListener(this);
        (btMapView = (Button) findViewById(R.id.bt_map_view)).setOnClickListener(this);
        swScreenShots = (Switch) findViewById(R.id.sw_scr_shot);
        swClipBoard = (Switch) findViewById(R.id.sw_clip_board);
    }


    /**
     * Khởi tạo SWITCH screenshot (Gọi ở initAll)
     */
    private void initScreenShot() {
        /*Switch lister
        * Không trực tiếp chạy mà chạy thông qua sw.setChecked
        */

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
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQUEST_PERMISSION_DRAW_OVER_APP);
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

        /*setCheck cho sw
        * (Lần chạy đầu key đã được gán giá trị off ở các lần sau đã edit key lại)*/
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

    /**
     * Khởi tạo SWITCH clipboard (Gọi ở initAll)
     * Tương tự với initScreenShot
     */
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
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQUEST_PERMISSION_DRAW_OVER_APP);
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


    /**
     * Trả về kết quả của permission
     */
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
    }

    /**
     * Phân loại note sẽ hiển thị (Gọi ở Frg Main) TH sử dụng khi click vào noti wifi hoặc gps
     * Trả về true nếu click vào noti wifi hoặc Gps
     * Trả về false nếu không click vào noti (TH này loại note sẽ hiển thị là loại được lưu tại lần lưu trước)
     */
    public boolean showArrayNoteDetect() {
        String type = getIntent().getStringExtra(FragmentMain.KEY_TYPE_SAVE);// KEY này phân biệt khi click vào noti nào (được đính vào intent ở Notification Service cùng với theo arrayListNote)
        if (getIntent().getSerializableExtra(AppService.WIFI_DETECT + "") != null || getIntent().getSerializableExtra(AppService.GPS_DETECT + "") != null) {
            ArrayList arrayList;
            switch (type) {
                case AppService.WIFI_DETECT + "":
                    arrayList = (ArrayList) ((DataSerializable) getIntent().getSerializableExtra(AppService.WIFI_DETECT + "")).getData();//Lấy array từ intent khi click vào noti
//                    Log.d(TAG, "showArrayNoteDetect: "+arrayList.size());
                    Log.d(TAG, "showArrayNoteDetect: Vừa click vào WIFI");
                    frgMain.getRecycleNoteAdapter().swapDataUseArray(arrayList);//cập nhât mới lại list
                    frgMain.setTypeWifi(getIntent().getStringExtra(FragmentMain.KEY_TYPE_SAVE));//để
                    break;
                case AppService.GPS_DETECT + "":
                    arrayList = (ArrayList) ((DataSerializable) getIntent().getSerializableExtra(AppService.GPS_DETECT + "")).getData();//Lấy array từ intent khi click vào noti
                    Log.d(TAG, "showArrayNoteDetect: Vừa click vào GPS: " + arrayList.size());
                    frgMain.getRecycleNoteAdapter().swapDataUseArray(arrayList);//cập nhât mới lại list
                    frgMain.setTypeGps(getIntent().getStringExtra(FragmentMain.KEY_TYPE_SAVE));//Để phân biệt readDataWithWifi hay readDataWithTypeSave lúc refresh
                    break;
                default:
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        /*?? chưa hiểu lúc đấy nghĩ gì mà initView ở đây này*/
        initView();
        super.onResume();
    }

    /**
     * Xóa tất cả các note (Gọi khi click vào btn delete)
     */
    private void resetAllDataBase() {
        /*Tạo thông báo chắc chắn xóa chưa*/
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        alert.setTitle("Cảnh báo!");
        alert.setMessage("Nếu cần lưu dữ liệu hãy sao lưu lại. Bạn có chắc chắn muốn xóa hết các note đã lưu không?");

        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                canDelete = false;//Cờ không cho phép ấn xóa khi đang chạy . Set = true khi stickyNote Delete được trả về
                /*Gửi cho service thực thi*/
                Intent itResetData = new Intent(MainActivity.this, AppService.class);
                itResetData.putExtra(KEY_SERVICE, SERVICE_DELETE);
                startService(itResetData);
                alert.dismiss();
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                canDelete = true;//Ấn không thì được phép click Button delete vào lần sau
                alert.dismiss();
            }
        });
        alert.show();
    }


    /**
     * Chưa phát triển
     */
    private void initOpenCV() {
        // TODO: 8/25/2016 Khởi tạo OpenCv xử lý ảnh
    }


    private void showFrgMain() {
        sensorManager.unregisterListener(this);//Dừng lắng nghe rotation của máy (Gọi sau khi tắt FrgCapture)

        /*Khởi tạo frgMain một lần duy nhất*/
        if (frgMain == null) {
            Log.d(TAG, "showFrgMain: ");
            frgMain = new FragmentMain();
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main, frgMain)
                .commit();

        /** Lắng nghe khi drawer được mở hoặc tắt
         * Cho phép/Không cho phép long click*/
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

    public DatabaseManager getDb() {
        return db;
    }

    /**
     * Lấy Giá trị từ KEY TYPE SAVE
     */
    public String getTypeSave() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARE_PREFERENCE, Context.MODE_PRIVATE);
        String typeSave = sharedPreferences.getString(KEY_TYPE_SAVE, "");
        return typeSave;
    }

    /**
     * Hiển thị FrgAddNote
     */
    public void showFrgAddNote(final String imgPath, final String imgThumbnailPath, final String typeSave) {
        frgAdd = new FrgAddNote(imgPath, imgThumbnailPath, typeSave);
        /*Chỉ có Khi chụp ảnh thì sensorManager mới được register
        * Unregister ngay sau khi chụp xong ảnh (Khi đó phương thức này được thực thi)*/
        if (typeSave.equals(DatabaseManager.TYPE_CAPTURE) && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }


        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main, frgAdd)
                .commit();
    }

    /**
     * Hiển thị FrgCapture
     */
    public void showFrgCapture() {
        sensorManager.registerListener(this, mDefaultSensor, SensorManager.SENSOR_DELAY_FASTEST);//Đăng ký lắng nghe nếu thiết bị được quay theo trục Z ở đây
        FrgCapture frgCapture = new FrgCapture();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main, frgCapture)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Hiển thị FrgEdit
     */
    public void showFrgEdit(String title, String content, String pathImg) {
        FrgEdit frgEdit = new FrgEdit(title, content, pathImg);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main, frgEdit)
                .commit();
    }

    /**
     * Update note vào csdl(Gọi khi Click button save của FrgEdit)
     */
    public void updateDataBase(String oldTitle, String newTitle, String newContent) {
        db.update(oldTitle, newTitle, newContent);
        showFrgMain();//Delete xong hiển thị lại frgMain
    }

    /**
     * Delete note vào csdl(Gọi khi Click button delete ở FrgMain/FloatOption/ActionUp)
     */
    public void deleteDb(String noteTitleDelete, String noteContentDelete, String noteImg, String noteThumbnail, String typeSave, String latlong, String alarm, String wifiName) {
        db.deleteNote(noteTitleDelete, noteContentDelete, noteImg, noteThumbnail, typeSave, latlong, alarm, wifiName);
        //Vì delete k gọi 1 frgment khác lên nên replace sẽ k vào onCreateView nữa. Ở đây dùng showFrgMain() sẽ k cập nhật lại list
        frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb(getTypeSave()));//Cập nhật lại list sau khi delete
    }

    /**
     * Insert note vào csdl (Gọi khi click vào button Save ở Frg bất kỳ))
     */
    public void insertToDataBase(String noteTitle, String noteContent, final String imgPath, final String imgThumbnailPath, final String typeSave, String latlong) {
        db.insert(noteTitle, noteContent, imgPath, imgThumbnailPath, typeSave, latlong, "", "", true);
        showFrgMain();
    }

    @Override
    public void onBackPressed() {
        showFrgMain();//Show frgMain nếu frg khác đang hiển thị
        /*Exit phải ấn 2 lần*/
        if (isExit) {
            super.onBackPressed();
        } else
            Toast.makeText(MainActivity.this, "Double press back to exit", Toast.LENGTH_SHORT).show();
        isExit = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isExit = false;
            }
        }, 500);
    }

    /**
     * Start activity noteContent (Khi click vào itemView gọi ở FrgMain/onTouchListener/onClick )
     * Mảng đang được hiển thị
     * Vị trí khi click
     */
    public void startNoteActivity(ArrayList<NoteItem> data, int position) {
        Intent intent = new Intent(MainActivity.this, NotePagerActivity.class);
        intent.putExtra(KEY_ARR_DATA, new DataSerializable(data));
        intent.putExtra(KEY_POSTION_CLICK, position);
        startActivity(intent);
    }

    /**
     * Khi click vào các button ở Drawer
     */
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

                    drw.closeDrawer(Gravity.LEFT);//Thu nhỏ drawer

                    String type = sharedPreferences.getString(FragmentMain.KEY_TYPE_SAVE, null);//Chọn loại list sẽ hiển thị ở ViewPager
                    if (type != null) {
                        frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb(type));
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Backup is running", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_restore:
                if (canRestore) {
                    canRestore = false;//Không cho phép click 2 lần
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
                    resetAllDataBase();
                    drw.closeDrawer(Gravity.LEFT);
                } else {
                    Toast.makeText(MainActivity.this, "Delete is running", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_map_view:
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                ArrayList<NoteItem> noteItemArrayList = frgMain.getRecycleNoteAdapter().getArrData();
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

    /**
     * Hiển thị gallery (Gọi khi click button Gallery từ Dialog Pick Method)
     */
    public void showGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    /**
     * Ghi file khi pick xong ảnh
     * Draw over App
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: 9/19/2016 Pick 2 ảnh thành 1 ảnh
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            date = new Date();
            String fileNameImage = "note" + dateFormat.format(date) + ".jpg";
            String fileNameImageThumbnail = "note" + dateFormat.format(date) + "_thumbnail.jpg";
            db.createDefaultFolderInternal();
            final File fileOutputImg = new File(DatabaseManager.PATH_APP_INTERNAL + "/imageSave/" + fileNameImage);
            final File fileOutputThumbnail = new File(DatabaseManager.PATH_APP_INTERNAL + "/imageSave/" + fileNameImageThumbnail);
            try {
                FileObserver fileObserver = new FileObserver(DatabaseManager.PATH_APP_INTERNAL + "/imageSave") {
                    @Override
                    public void onEvent(int event, String path) {
                        Log.d(TAG, "onEvent: " + path);
                        if (event == FileObserver.CLOSE_WRITE && path.equals(fileOutputImg.getName())) {
                            showFrgAddNote(fileOutputImg.getPath(), fileOutputThumbnail.getPath(), DatabaseManager.TYPE_GALLERY);
                            this.stopWatching();
                        }
                    }
                };
                fileObserver.startWatching();

                //Copy img vào bộ nhớ trong
                InputStream inputStreamImg = getContentResolver().openInputStream(data.getData());
                FileOutputStream outputImg = new FileOutputStream(fileOutputImg);
                Log.d(TAG, "onActivityResult: outPut name: " + fileOutputImg.getName());
                byte[] b = new byte[1024];
                int length;
                while ((length = inputStreamImg.read(b)) != -1) {
                    outputImg.write(b, 0, length);
                }
                inputStreamImg.close();
                outputImg.close();

                //Copy imgThumbnail vào bộ nhớ trong
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == REQUEST_PERMISSION_DRAW_OVER_APP) {
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
                } else {
                    Toast.makeText(MainActivity.this, "Nếu không cho phép quyền này chế độ screen shot và clipboard sẽ không hoạt động", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                    swScreenShots.setChecked(false);
                    swClipBoard.setChecked(false);
                }

            }
        }
    }

    public Cursor swapDb(String typeSelect) {
        return db.readAllDataWithColumnTypeSave(typeSelect);
    }

    /**
     * Phương thức thực thi  này khi activity này được start và
     * có sticky note được gửi khi service chạy xong các hành động Restore, delete và backup
     */
    @Subscribe(sticky = true)
    public void onEvent(MessageEventBus bus) {
        try {
            switch (bus.getType()) {
                case AppService.SERVICE_BACKUP:
                    frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb("All"));
                    canBackup = true;
                    break;
                case AppService.SERVICE_DELETE:
                    frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb("All"));
                    frgMain.initSnipper();
                    canDelete = true;
                    break;
                case AppService.SERVICE_RESTORE:
                    canRestore = true;
                    frgMain.getRecycleNoteAdapter().swapDataUseCursor(swapDb("All"));
                    frgMain.initSnipper();
                    break;
                case AppService.SWITCH_CHANGE:
                    Log.d(TAG, "onEvent: ");
                    if (sharedPreferences.getString(SWITCH_CLIPBOARD, null) != null) {
                        if (sharedPreferences.getString(SWITCH_CLIPBOARD, null).equals("on")) {
                            swClipBoard.setChecked(true);
                        }else {
                            swClipBoard.setChecked(false);
                        }
                    }
                    if (sharedPreferences.getString(SWITCH_SCREENSHOT, null) != null) {
                        if (sharedPreferences.getString(SWITCH_SCREENSHOT, null).equals("on")) {
                            swScreenShots.setChecked(true);
                        }else {
                            swScreenShots.setChecked(false);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
        }
    }

    /**
     * Phương thức lắng nghe khi xoay máy theo trục Z
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mDefaultSensor) {
            float[] values = event.values;
            float zAxis = values[2];
            if ((zAxis > 45 && zAxis < 90) || (zAxis < -45 && zAxis > -90)) {
                Log.d(TAG, "onSensorChanged: Máy đang ở trạng thái nằm ngang");
                orientDevice = ORIENT_HORIZONTAL;
            } else {
                orientDevice = ORIENT_VERTICAL;
                Log.d(TAG, "onSensorChanged: Máy đang ở trạng thái nằm dọc");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }

    /**
     * Thực thi khi xoay camera khi chụp ảnh
     */
    public int getOrientDevice() {
        return orientDevice;
    }

    /*Update lại snipper khi có các hành động Delete 1 note*/
    @Override
    public void updateSpinner() {
        frgMain.initSnipper();
    }
}
