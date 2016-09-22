package com.p2ild.notetoeverything.frgment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.p2ild.notetoeverything.CustomStaggeredGridLayoutManager;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.RecycleViewOnItemTouch;
import com.p2ild.notetoeverything.activity.MainActivity;
import com.p2ild.notetoeverything.adapter.NoteAdapter;
import com.p2ild.notetoeverything.adapter.SpinnerAdapterTypeSave;
import com.p2ild.notetoeverything.other.DatabaseManager;
import com.p2ild.notetoeverything.other.WifiGpsManagerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;


/**
 * Created by duypi on 8/20/2016.
 */
public class FragmentMain extends Fragment implements View.OnClickListener {

    private static final String TAG = FragmentMain.class.getSimpleName();
    private static final int BUTTON_EDIT = 1;
    private static final int BUTTON_ALARM = 2;
    private static final int BUTTON_DELETE = 3;
    private static final int BUTTON_SHARE = 4;
    private static final int BUTTON_UNSELECTED = 0;
    private static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    private static final String KEY_TYPE_SAVE = "KEY_TYPE_SAVE";
    private final DatabaseManager db;
    private final String typeSavePara;
    private Cursor cursor;
    private View rootView;
    private RecyclerView rcv;
    private RelativeLayout llActionBar;
    private ImageButton btAddNote;
    private RelativeLayout rlFloatOption;
    private ImageButton ibEdit, ibAlarm, ibDelete, ibShare;
    private int positionCurrentItem;
    private int buttonFloatOption;
    private float xMinIbEdit, yMinIbEdit, xMaxIbEdit, yMaxIbEdit;
    private float xMinIbAlarm, yMinIbAlarm, xMaxIbAlarm, yMaxIbAlarm;
    private float xMinIbDelete, yMinIbDelete, xMaxIbDelete, yMaxIbDelete;
    private float xMinIbShare, yMinIbShare, xMaxIbShare, yMaxIbShare;
    private int widthScreen, heightScreen;
    private WifiGpsManagerActivity wifiGpsManager;
    private Handler handler;
    private RecyclerView.OnItemTouchListener rcvOnItemTouchListioner;
    private CustomStaggeredGridLayoutManager customStaggeredGridLayoutManager;//Custom view cho phép dừng hoặc tiếp tục scroll recycleView
    private boolean isLongClick;//Khônng cho phép ACTION_FOCUS và ACTION_UP thực thi khi chưa LONG_CLICK
    private NoteAdapter noteAdapter;
    private int heightFloatOption;
    private int widthFloatOption;
    private Animation animation;
    private AsyncTask animRunOnce;
    private ImageButton ibMenu;
    private boolean multiSelect;
    private CheckBox cb;
    private Spinner spinner;
    private EventBus eventBus;
    private CharSequence saveTvSpinner;

    public FragmentMain(DatabaseManager db, String typeSave) {
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        this.db = db;
        this.typeSavePara = typeSave;
        if (typeSave == "") {
            this.cursor = db.readAllData("All");
        } else {
            this.cursor = db.readAllData(typeSave);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frg_layout_main, container, false);
        noteAdapter = new NoteAdapter(getActivity(), cursor);
        multiSelect = false;
        initViewChild();
        return rootView;
    }

    public void updateRecyclerView(Cursor cursor) {
        this.cursor = cursor;
        rcv.getAdapter().notifyDataSetChanged();
    }

    private void initViewChild() {
        (btAddNote = (ImageButton) rootView.findViewById(R.id.bt_add_note)).setOnClickListener(this);
        (ibMenu = (ImageButton) rootView.findViewById(R.id.ib_menu)).setOnClickListener(this);

        // TODO: 8/31/2016 ---Done---Chưa đặt snipper
        initSnipper();

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.float_option);
        isLongClick = false;

        initRcv();

        //init Float Option
        rlFloatOption = (RelativeLayout) rootView.findViewById(R.id.rl_float_option);
//        rlFloatOption.setVisibility(View.GONE);
        ibEdit = (ImageButton) rootView.findViewById(R.id.ib_pencil);
        ibAlarm = (ImageButton) rootView.findViewById(R.id.ib_alarm);
        ibDelete = (ImageButton) rootView.findViewById(R.id.ib_recycling);
        ibShare = (ImageButton) rootView.findViewById(R.id.ib_share);

        llActionBar = (RelativeLayout) rootView.findViewById(R.id.action_bar);
    }

    private void initRcv() {
        //init Recycle View
        rcv = (RecyclerView) rootView.findViewById(R.id.rcv);
        rcv.setHasFixedSize(true);
        customStaggeredGridLayoutManager = new CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rcv.setLayoutManager(customStaggeredGridLayoutManager);
        customStaggeredGridLayoutManager.setCanScroll(true);
        (cb = (CheckBox) rootView.findViewById(R.id.cb_checked)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    multiSelect = true;
                } else {
                    multiSelect = false;
                }
            }
        });
        rcv.setAdapter(noteAdapter);
        rcvOnItemTouchListioner = new RecycleViewOnItemTouch(getActivity(), rcv, new RecycleViewOnItemTouch.onItemClick() {
            // TODO: 8/25/2016 ---Done--- Chưa xử lý code long press khi action up thì option float sẽ biến mất
            @Override
            public void onClick(View view, int position) {
                ((MainActivity) getActivity()).startNoteActivity(((NoteAdapter) rcv.getAdapter()).getArrData(), position);
            }

            /*Hiện float option*/
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onLongClick(View view, int position, final float rawX, final float rawY) {
                // TODO: 8/26/2016 ---Done--- Lần đầu tiên load float option sai vị trí
                if (multiSelect) {
                    ((customStaggeredGridLayoutManager.getChildAt(position))).setVisibility(View.GONE);
                } else {
                    isLongClick = true;
                    widthScreen = getResources().getDisplayMetrics().widthPixels;
                    heightScreen = getResources().getDisplayMetrics().widthPixels;
                    rlFloatOption.setVisibility(View.VISIBLE);
                    rlFloatOption.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            widthFloatOption = rlFloatOption.getWidth();
                            heightFloatOption = rlFloatOption.getHeight();
                            rlFloatOption.setTranslationX(rawX - widthFloatOption / 2);
                            rlFloatOption.setTranslationY(rawY - 200 - heightFloatOption / 2);
                        }
                    });
                    rlFloatOption.startAnimation(animation);
                    positionCurrentItem = cursor.getCount() - 1 - position;
                    customStaggeredGridLayoutManager.setCanScroll(false);

                    setDefaultDrawableImageButton();
                }
            }

            @Override
            public void onActionFocus(float rawX, float rawY) {
                if (!isLongClick) {
                    return;
                }
                switchButton(rawX, rawY);
                if (buttonFloatOption == BUTTON_UNSELECTED) {
                    animRunOnce = null;
                }
                switch (buttonFloatOption) {
                    case BUTTON_EDIT:
                        startAnimRotate(BUTTON_EDIT);
                        ibEdit.setImageResource(R.drawable.ic_pencil_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
//                        runAnim=true;
                        break;
                    case BUTTON_ALARM:
                        startAnimRotate(BUTTON_ALARM);
                        ibAlarm.setImageResource(R.drawable.ic_alarm_clock_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    case BUTTON_SHARE:
                        startAnimRotate(BUTTON_SHARE);
                        ibShare.setImageResource(R.drawable.ic_share_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    case BUTTON_DELETE:
                        startAnimRotate(BUTTON_DELETE);
                        ibDelete.setImageResource(R.drawable.ic_recycling_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    default:
                        setDefaultDrawableImageButton();
                        break;
                }
            }

            @Override
            public void onActionUp(float rawX, float rawY, int position) {
                if (!isLongClick) {
                    return;
                }
                switchButton(rawX, rawY);
                switch (buttonFloatOption) {
                    case BUTTON_EDIT:
                        rlFloatOption.setVisibility(View.GONE);
                        cursor.moveToPosition(positionCurrentItem);
                        String title = cursor.getString(DatabaseManager.COLUMN_TITLE_NOTE);
                        String content = cursor.getString(DatabaseManager.COLUMN_CONTENT_NOTE);
                        String pathImg = cursor.getString(DatabaseManager.COLUMN_PATH_IMAGE_NOTE);
                        ((MainActivity) getActivity()).showFrgEdit(title, content,pathImg);
                        break;
                    case BUTTON_ALARM:
                        // TODO: 8/25/2016 Chưa xử lý code báo thức cho note
                        rlFloatOption.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "This function can't use in this version", Toast.LENGTH_SHORT).show();
                        break;
                    case BUTTON_SHARE:
                        // TODO: 8/31/2016 sử dụng Share Action Provider - TL Lập trình ANDROID P38
                        Toast.makeText(getActivity(), "This function can't use in this version", Toast.LENGTH_SHORT).show();
                        rlFloatOption.setVisibility(View.GONE);
                        break;
                    case BUTTON_DELETE:
                        cursor.moveToPosition(positionCurrentItem);
                        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setMessage("Do you want to delete this note?");
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String noteTitleDelete = cursor.getString(DatabaseManager.COLUMN_TITLE_NOTE);
                                String noteContentDelete = cursor.getString(DatabaseManager.COLUMN_CONTENT_NOTE);
                                String noteImg = cursor.getString(DatabaseManager.COLUMN_PATH_IMAGE_NOTE);
                                String noteThumbnail = cursor.getString(DatabaseManager.COLUMN_PATH_THUMBNAIL_IMAGE_NOTE);
                                String typeSave = cursor.getString(DatabaseManager.COLUMN_TYPE_SAVE);
                                ((MainActivity) getActivity()).deleteDb(noteTitleDelete,noteContentDelete, noteImg, noteThumbnail,typeSave);
                                alertDialog.dismiss();
                            }
                        });

                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.show();
                        rlFloatOption.setVisibility(View.GONE);
                        break;
                    default:
                        rlFloatOption.setVisibility(View.GONE);
                        break;
                }
                isLongClick = false;
//                buttonFloatOption=BUTTON_UNSELECTED;//Đặt lại default button tránh action focus nhảy lấy dữ button cũ để chạy
                customStaggeredGridLayoutManager.setCanScroll(true);
            }
        });
        rcv.addOnItemTouchListener(rcvOnItemTouchListioner);
    }

    private void initSnipper() {
        ArrayList<String> arrNoteType = new ArrayList<>();
        arrNoteType.add("All");
        Cursor allCursor = db.readAllData("All");
        for (allCursor.moveToFirst(); !allCursor.isAfterLast(); allCursor.moveToNext()) {
            String typeSave = allCursor.getString(DatabaseManager.COLUMN_TYPE_SAVE);
            if (!arrNoteType.contains(typeSave)) {
                arrNoteType.add(typeSave);
            }
        }

        final SpinnerAdapterTypeSave spinnerAdapterTypeSave = new SpinnerAdapterTypeSave(getActivity(), android.R.layout.simple_spinner_dropdown_item, arrNoteType);
        spinner = (Spinner) rootView.findViewById(R.id.sp_title_action_bar);
        spinner.setAdapter(spinnerAdapterTypeSave);
        spinner.setSelection(arrNoteType.indexOf(typeSavePara));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = adapterView.getItemAtPosition(i).toString();
                MainActivity activity = (MainActivity) getActivity();
                SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARE_PREFERENCE, Context.MODE_PRIVATE);
                switch (type) {
                    case DatabaseManager.TYPE_CAPTURE:
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).commit();//Khi Chụp hoặc lưu ảnh mới sẽ mở lại type cũ lên
                        cursor = activity.swapDb(type);
                        noteAdapter.swapData(cursor);
                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        saveTvSpinner =((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).getText();

                        break;
                    case DatabaseManager.TYPE_CLIP_BOARD:
                        // TODO: 9/20/2016 ---Done---Hiển thị thiếu clipboard
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).commit();
                        cursor = activity.swapDb(type);
                        noteAdapter.swapData(cursor);
                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        saveTvSpinner =((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).getText();
                        break;
                    case DatabaseManager.TYPE_GALLERY:
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).commit();
                        cursor = activity.swapDb(type);
                        noteAdapter.swapData(cursor);
                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        saveTvSpinner =((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).getText();
                        break;
                    case DatabaseManager.TYPE_SCREEN_SHOT:
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).commit();
                        cursor = activity.swapDb(type);
                        noteAdapter.swapData(cursor);
                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    case "All":
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, "All").commit();
                        cursor = activity.swapDb("All");
                        noteAdapter.swapData(cursor);
                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void startAnimRotate(final int buttonFocus) {
        if (animRunOnce == null) {
            AsyncTask<Void, Integer, Void> animRotate = new AsyncTask<Void, Integer, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    animRunOnce = this;
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    for (int i = 0; i < 360; i++) {
                        SystemClock.sleep(2);
                        publishProgress(i);
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    super.onProgressUpdate(values);
                    switch (buttonFocus) {
                        case BUTTON_EDIT:
                            ibEdit.setRotationX(values[0]);
                            break;
                        case BUTTON_ALARM:
                            ibAlarm.setRotationX(values[0]);
                            break;
                        case BUTTON_SHARE:
                            ibShare.setRotationX(values[0]);
                            break;
                        case BUTTON_DELETE:
                            ibDelete.setRotationX(values[0]);
                            break;
                        default:
                            break;
                    }

                }
            };
            animRotate.execute();
        }
    }

    private void setDefaultDrawableImageButton() {
        ibEdit.setImageResource(R.drawable.ic_pencil);
        ibAlarm.setImageResource(R.drawable.ic_alarm_clock);
        ibDelete.setImageResource(R.drawable.ic_recycling);
        ibShare.setImageResource(R.drawable.ic_share);
    }

    /*Lựa chọn các button của float option cho switch case*/
    private void switchButton(float rawX, float rawY) {
        /*Button Edit*/
        xMinIbEdit = rlFloatOption.getX() + ibEdit.getX();
        xMaxIbEdit = rlFloatOption.getX() + ibEdit.getX() + ibEdit.getWidth();
        yMinIbEdit = rlFloatOption.getY() + ibEdit.getY();
        yMaxIbEdit = rlFloatOption.getY() + ibEdit.getY() + ibEdit.getHeight();

        if ((rawX > xMinIbEdit && rawX < xMaxIbEdit)
                && (rawY > yMinIbEdit && rawY < yMaxIbEdit)) {
            buttonFloatOption = BUTTON_EDIT;
//            runAnim=false;
            return;
        }

        /*Button Alarm*/
        xMinIbAlarm = rlFloatOption.getX() + ibAlarm.getX();
        xMaxIbAlarm = rlFloatOption.getX() + ibAlarm.getX() + ibAlarm.getWidth();
        yMinIbAlarm = rlFloatOption.getY() + ibAlarm.getY();
        yMaxIbAlarm = rlFloatOption.getY() + ibAlarm.getY() + ibAlarm.getHeight();

        if ((rawX > xMinIbAlarm && rawX < xMaxIbAlarm)
                && (rawY > yMinIbAlarm && rawY < yMaxIbAlarm)) {
            buttonFloatOption = BUTTON_ALARM;
//            runAnim=false;
            return;
        }

        /*Button Share*/
        xMinIbShare = rlFloatOption.getX() + ibShare.getX();
        xMaxIbShare = rlFloatOption.getX() + ibShare.getX() + ibShare.getWidth();
        yMinIbShare = rlFloatOption.getY() + ibShare.getY();
        yMaxIbShare = rlFloatOption.getY() + ibShare.getY() + ibShare.getHeight();

        if ((rawX > xMinIbShare && rawX < xMaxIbShare)
                && (rawY > yMinIbShare && rawY < yMaxIbShare)) {
            buttonFloatOption = BUTTON_SHARE;
//            runAnim=false;
            return;

        }/*Button Delete*/
        xMinIbDelete = rlFloatOption.getX() + ibDelete.getX();
        xMaxIbDelete = rlFloatOption.getX() + ibDelete.getX() + ibDelete.getWidth();
        yMinIbDelete = rlFloatOption.getY() + ibDelete.getY();
        yMaxIbDelete = rlFloatOption.getY() + ibDelete.getY() + ibDelete.getHeight();

        if ((rawX > xMinIbDelete && rawX < xMaxIbDelete)
                && (rawY > yMinIbDelete && rawY < yMaxIbDelete)) {
            buttonFloatOption = BUTTON_DELETE;
//            runAnim=false;
            return;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                tvWifiInfo.setText(wifiGpsManager.getWifiInfomation());
//                tvGpsInfo.setText(wifiGpsManager.getLatitude()+"\n"+wifiGpsManager.getLongitude());
//
//                handler.postDelayed(this,1000);
//            }
//        },1000);
        rlFloatOption.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_add_note:
                DialogMethodPickImage dlgPick = new DialogMethodPickImage(getActivity());
                dlgPick.show();
                break;
            case R.id.ib_menu:
                ((MainActivity) getActivity()).showDrw();
                break;
            default:
                break;
        }
    }

    public CustomStaggeredGridLayoutManager getCustomStaggeredGridLayoutManager() {
        return customStaggeredGridLayoutManager;
    }

    public RecyclerView getRcv() {
        return rcv;
    }

    public RecyclerView.OnItemTouchListener getRcvOnItemTouchListioner() {
        return rcvOnItemTouchListioner;
    }

    @Subscribe
    public void onEvent(Integer[] data) {
        switch (data[0]) {
            case DatabaseManager.MSG_UPDATE_PERCENT_BACKUP:
                try {
                    // TODO: 9/16/2016 ---Done--- percent progress not set
                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText("Backup : " + data[1] + " %");
                } catch (java.lang.NullPointerException e) {
                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình backup dữ liệu", Toast.LENGTH_SHORT).show();
                }
                break;
            case DatabaseManager.MSG_BACKUP_COMPLETE:
                try {
                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(saveTvSpinner);
                } catch (java.lang.NullPointerException e) {
                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình backup dữ liệu", Toast.LENGTH_SHORT).show();
                }
                break;

            case DatabaseManager.MSG_UPDATE_PERCENT_RESTORE:
                try {
                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText("Restore : " + data[1] + " %");
                } catch (java.lang.NullPointerException e) {
                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình restore dữ liệu", Toast.LENGTH_SHORT).show();
                }
                break;
            case DatabaseManager.MSG_RESTORE_COMPLETE:
                try {
                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(saveTvSpinner);
//                    ((TextView) findViewById(R.id.tv_title_action_bar)).setText("ALL NOTE (" + data[1] + ")");
                } catch (java.lang.NullPointerException e) {
                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình restore dữ liệu", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }
}