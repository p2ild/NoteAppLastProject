package com.p2ild.notetoeverything.frgment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.p2ild.notetoeverything.DatabaseManagerCopyDb;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;
import com.p2ild.notetoeverything.adapter.RecycleNoteAdapter;
import com.p2ild.notetoeverything.adapter.SpinnerAdapterTypeSave;
import com.p2ild.notetoeverything.dialog.DialogMethodCreateNewNote;
import com.p2ild.notetoeverything.locationmanager.WifiGpsManager;
import com.p2ild.notetoeverything.other.CustomStaggeredGridLayoutManager;
import com.p2ild.notetoeverything.other.RecycleViewOnItemTouch;

import java.util.ArrayList;


/**
 * Created by duypi on 8/20/2016.
 */
public class FragmentMain extends Fragment implements View.OnClickListener {

    public static final String KEY_TYPE_SAVE = "KEY_TYPE_SAVE";
    private static final String TAG = FragmentMain.class.getSimpleName();
    private static final int BUTTON_EDIT = 1;
    private static final int BUTTON_ALARM = 2;
    private static final int BUTTON_DELETE = 3;
    private static final int BUTTON_SHARE = 4;
    private static final int BUTTON_SAVE = 5;
    private static final int BUTTON_UNSELECTED = 0;
    private static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    MainActivity activity;
    // TODO: 2016-10-05 Chuyển cách import database
    private DatabaseManagerCopyDb db;
    private String typeSavePara;
    private Cursor cursor;
    private View rootView;
    private RecyclerView rcv;
    private RelativeLayout rlFloatOption;
    private ImageButton ibEdit, ibAlarm, ibDelete, ibShare, ibSave;
    private int positionCurrentItem;
    private int buttonFloatOption;
    private float xMinIbEdit, yMinIbEdit, xMaxIbEdit, yMaxIbEdit;
    private float xMinIbAlarm, yMinIbAlarm, xMaxIbAlarm, yMaxIbAlarm;
    private float xMinIbDelete, yMinIbDelete, xMaxIbDelete, yMaxIbDelete;
    private float xMinIbShare, yMinIbShare, xMaxIbShare, yMaxIbShare;
    private float xMinIbSave, yMinIbSave, xMaxIbSave, yMaxIbSave;
    private WifiGpsManager wifiGpsManager;
    private RecyclerView.OnItemTouchListener rcvOnItemTouchListioner;
    private CustomStaggeredGridLayoutManager customStaggeredGridLayoutManager;//Custom view cho phép dừng hoặc tiếp tục scroll recycleView
    private boolean isLongClick;//Khônng cho phép ACTION_FOCUS và ACTION_UP thực thi khi chưa LONG_CLICK
    private RecycleNoteAdapter recycleNoteAdapter;
    private int heightFloatOption;
    private int widthFloatOption;
    private Animation animation;
    private AsyncTask animRunOnce;
    private Spinner spinner;
    //    private EventBus eventBus;
    private String saveTvSpinner = "";
    private SharedPreferences sharedPreferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int[] into = new int[2];
    private TextView tvCountNote;
    private Cursor allCursor;
    private TextView tvFuntion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inits();
    }

    private void inits() {
//        eventBus = EventBus.getDefault();
//        eventBus.register(this);

        activity = (MainActivity) getActivity();
        sharedPreferences = activity.getSharedPreferences(SHARE_PREFERENCE, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(KEY_TYPE_SAVE, null) == null) {
            sharedPreferences.edit().putString(KEY_TYPE_SAVE, "All").apply();
        }
        this.db = ((MainActivity) getActivity()).getDb();

        this.typeSavePara = ((MainActivity) getActivity()).getTypeSave();
        this.cursor = db.readAllData(typeSavePara);
        allCursor = db.readAllData("All");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frg_layout_main, container, false);
        recycleNoteAdapter = new RecycleNoteAdapter(getActivity(), cursor);
        initViewChild();
        return rootView;
    }


    public RecycleNoteAdapter getRecycleNoteAdapter() {
        return recycleNoteAdapter;
    }

    private void initViewChild() {
        ((ImageButton) rootView.findViewById(R.id.bt_add_note)).setOnClickListener(this);
        ((ImageButton) rootView.findViewById(R.id.ib_menu)).setOnClickListener(this);

        tvCountNote = (TextView) rootView.findViewById(R.id.tv_count_note);
        tvFuntion = (TextView) rootView.findViewById(R.id.tv_funtion);

        // TODO: 8/31/2016 ---Done---Chưa đặt snipper
        initSnipper();
        initRcv();

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.float_option);
        isLongClick = false;

        //init Float Option
        rlFloatOption = (RelativeLayout) rootView.findViewById(R.id.rl_float_option);
//        rlFloatOption.setVisibility(View.GONE);
        ibEdit = (ImageButton) rootView.findViewById(R.id.ib_pencil);
        ibAlarm = (ImageButton) rootView.findViewById(R.id.ib_alarm);
        ibDelete = (ImageButton) rootView.findViewById(R.id.ib_recycling);
        ibShare = (ImageButton) rootView.findViewById(R.id.ib_share);
        ibSave = (ImageButton) rootView.findViewById(R.id.ib_save);
    }

    private void initRcv() {
        //init Recycle View
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperf);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Log.d(TAG, "onRefresh: cursor count: " + cursor.getCount());
                cursor = db.readAllData(activity.getTypeSave());
                recycleNoteAdapter.swapDataUseCursor(cursor);
                AsyncTask<Integer, Integer, Integer> task = new AsyncTask<Integer, Integer, Integer>() {
                    @Override
                    protected Integer doInBackground(Integer... integers) {
                        SystemClock.sleep(2000);
                        return null;
                    }
                };
                task.execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        rcv = (RecyclerView) rootView.findViewById(R.id.rcv);
        rcv.setHasFixedSize(true);
        customStaggeredGridLayoutManager = new CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rcv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                into[0] = 0;
                into[1] = 0;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (customStaggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(into)[0] == 0) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });
        rcv.setLayoutManager(customStaggeredGridLayoutManager);
        customStaggeredGridLayoutManager.setCanScroll(true);
        rcv.setAdapter(recycleNoteAdapter);

        rcvOnItemTouchListioner = new RecycleViewOnItemTouch(getActivity(), rcv, new RecycleViewOnItemTouch.onItemClick() {
            // TODO: 8/25/2016 ---Done--- Chưa xử lý code long press khi action up thì option float sẽ biến mất
            @Override
            public void onClick(View view, int position) {
                ((MainActivity) getActivity()).startNoteActivity(((RecycleNoteAdapter) rcv.getAdapter()).getArrData(), position);
            }

            /*Hiện float option*/
            @Override
            public void onLongClick(View view, int position, final float rawX, final float rawY) {
                // TODO: 8/26/2016 ---Done--- Lần đầu tiên load float option sai vị trí
                isLongClick = true;
                swipeRefreshLayout.setEnabled(false);
                tvFuntion.setVisibility(View.VISIBLE);

                rlFloatOption.setVisibility(View.VISIBLE);
                rlFloatOption.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        widthFloatOption = rlFloatOption.getWidth();
                        heightFloatOption = rlFloatOption.getHeight();
                        rlFloatOption.setTranslationX(rawX - widthFloatOption / 2);
                        rlFloatOption.setTranslationY(rawY - 100 - heightFloatOption / 2);
                    }
                });
                rlFloatOption.startAnimation(animation);
                positionCurrentItem = cursor.getCount() - 1 - position;
                customStaggeredGridLayoutManager.setCanScroll(false);

                setDefaultDrawableImageButton();
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
                        tvFuntion.setText("Edit");
//                        runAnim=true;
                        break;
                    case BUTTON_ALARM:
                        startAnimRotate(BUTTON_ALARM);
                        ibAlarm.setImageResource(R.drawable.ic_alarm_clock_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        tvFuntion.setText("Alarm");
                        break;
                    case BUTTON_SHARE:
                        startAnimRotate(BUTTON_SHARE);
                        ibShare.setImageResource(R.drawable.ic_share_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        tvFuntion.setText("Share");
                        break;
                    case BUTTON_DELETE:
                        startAnimRotate(BUTTON_DELETE);
                        tvFuntion.setText("Delete");
                        ibDelete.setImageResource(R.drawable.ic_recycling_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    case BUTTON_SAVE:
                        startAnimRotate(BUTTON_SAVE);
                        tvFuntion.setText("Save");
                        ibSave.setImageResource(R.drawable.ic_save_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    default:
                        setDefaultDrawableImageButton();
                        tvFuntion.setText("Menu option");
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
                        cursor.moveToPosition(positionCurrentItem);
                        String title = cursor.getString(DatabaseManagerCopyDb.COLUMN_TITLE_NOTE);
                        String content = cursor.getString(DatabaseManagerCopyDb.COLUMN_CONTENT_NOTE);
                        String pathImg = cursor.getString(DatabaseManagerCopyDb.COLUMN_PATH_IMAGE_NOTE);
                        ((MainActivity) getActivity()).showFrgEdit(title, content, pathImg);
                        rlFloatOption.setVisibility(View.GONE);
                        tvFuntion.setVisibility(View.GONE);
                        break;
                    case BUTTON_ALARM:
                        // TODO: 8/25/2016 Chưa xử lý code báo thức cho note
                        rlFloatOption.setVisibility(View.GONE);
                        tvFuntion.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "This function can't use in this version", Toast.LENGTH_SHORT).show();
                        break;
                    case BUTTON_SHARE:
                        // TODO: 8/31/2016 sử dụng Share Action Provider - TL Lập trình ANDROID P38
                        Toast.makeText(getActivity(), "This function can't use in this version", Toast.LENGTH_SHORT).show();
                        rlFloatOption.setVisibility(View.GONE);
                        tvFuntion.setVisibility(View.GONE);
                        break;
                    case BUTTON_DELETE:
                        cursor.moveToPosition(positionCurrentItem);
                        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setMessage("Do you want to delete this note?");
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String noteTitleDelete = cursor.getString(DatabaseManagerCopyDb.COLUMN_TITLE_NOTE);
                                String noteContentDelete = cursor.getString(DatabaseManagerCopyDb.COLUMN_CONTENT_NOTE);
                                String noteImg = cursor.getString(DatabaseManagerCopyDb.COLUMN_PATH_IMAGE_NOTE);
                                String noteThumbnail = cursor.getString(DatabaseManagerCopyDb.COLUMN_PATH_THUMBNAIL_IMAGE_NOTE);
                                String typeSave = cursor.getString(DatabaseManagerCopyDb.COLUMN_TYPE_SAVE);
                                String latlong = cursor.getString(DatabaseManagerCopyDb.COLUMN_LATLONG);
                                String alarm = cursor.getString(DatabaseManagerCopyDb.COLUMN_ALARM);
                                String wifiName = cursor.getString(DatabaseManagerCopyDb.COLUMN_WIFI_NAME);
                                ((MainActivity) getActivity()).deleteDb(noteTitleDelete, noteContentDelete, noteImg, noteThumbnail, typeSave, latlong, alarm, wifiName);
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
                        tvFuntion.setVisibility(View.GONE);
                        break;
                    case BUTTON_SAVE:
                        cursor.moveToPosition(positionCurrentItem);
                        String pathImage = cursor.getString(DatabaseManagerCopyDb.COLUMN_PATH_IMAGE_NOTE);
                        db.exportImageToExternal(pathImage);
                        rlFloatOption.setVisibility(View.GONE);
                        tvFuntion.setVisibility(View.GONE);
                        break;
                    default:
                        tvFuntion.setVisibility(View.GONE);
                        rlFloatOption.setVisibility(View.GONE);
                        break;
                }
                isLongClick = false;
                swipeRefreshLayout.setEnabled(true);
//                buttonFloatOption=BUTTON_UNSELECTED;//Đặt lại default button tránh action focus nhảy lấy dữ button cũ để chạy
                customStaggeredGridLayoutManager.setCanScroll(true);
            }
        });
        rcv.addOnItemTouchListener(rcvOnItemTouchListioner);
    }

    /**
     * Lọc ra các loại type tồn tại
     */
    private void initSnipper() {
        ArrayList<String> arrNoteType = new ArrayList<>();
        arrNoteType.add("All");
        tvCountNote.setText("" + allCursor.getCount());
        int countTypeScreenShot = 0, countTypeCapture = 0, countTypeClipboard = 0, countTypeGallery = 0, countTypeTextOnly = 0;
        for (allCursor.moveToFirst(); !allCursor.isAfterLast(); allCursor.moveToNext()) {
            String typeSave = allCursor.getString(DatabaseManagerCopyDb.COLUMN_TYPE_SAVE);
            switch (typeSave) {
                case DatabaseManagerCopyDb.TYPE_SCREEN_SHOT:
                    countTypeScreenShot += 1;
                    break;
                case DatabaseManagerCopyDb.TYPE_CAPTURE:
                    countTypeCapture += 1;
                    break;
                case DatabaseManagerCopyDb.TYPE_CLIP_BOARD:
                    countTypeClipboard += 1;
                    break;
                case DatabaseManagerCopyDb.TYPE_GALLERY:
                    countTypeGallery += 1;
                    break;
                case DatabaseManagerCopyDb.TYPE_TEXT_ONLY:
                    countTypeTextOnly += 1;
                    break;
                default:
                    break;
            }
            if (!arrNoteType.contains(typeSave)) {
                arrNoteType.add(typeSave);
            }
        }

        String[] arrTemp = new String[arrNoteType.size()];
        for (int i = 0; i < arrNoteType.size(); i++) {
            switch (arrNoteType.get(i)) {
                case DatabaseManagerCopyDb.TYPE_SCREEN_SHOT:
                    arrTemp[i] = arrNoteType.get(i) + "(" + countTypeScreenShot + ")";
                    break;
                case DatabaseManagerCopyDb.TYPE_CAPTURE:
                    arrTemp[i] = arrNoteType.get(i) + "(" + countTypeCapture + ")";
                    break;
                case DatabaseManagerCopyDb.TYPE_CLIP_BOARD:
                    arrTemp[i] = arrNoteType.get(i) + "(" + countTypeClipboard + ")";
                    break;
                case DatabaseManagerCopyDb.TYPE_GALLERY:
                    arrTemp[i] = arrNoteType.get(i) + "(" + countTypeGallery + ")";
                    break;
                case DatabaseManagerCopyDb.TYPE_TEXT_ONLY:
                    arrTemp[i] = arrNoteType.get(i) + "(" + countTypeTextOnly + ")";
                    break;
                default:
                    break;
            }
        }

        SpinnerAdapterTypeSave spinnerAdapterTypeSave = new SpinnerAdapterTypeSave(getActivity(), android.R.layout.simple_spinner_dropdown_item, arrNoteType);
        spinner = (Spinner) rootView.findViewById(R.id.sp_title_action_bar);

        if (!activity.showArrayWifiDetect()) {
            Log.d(TAG, "initSnipper: Vào");
            spinner.setAdapter(spinnerAdapterTypeSave);
            spinner.setSelection(arrNoteType.indexOf(typeSavePara));
        } else {
            arrNoteType.add(DatabaseManagerCopyDb.TYPE_WIFI_AVAILABLE);
            spinnerAdapterTypeSave = new SpinnerAdapterTypeSave(getActivity(), android.R.layout.simple_spinner_dropdown_item, arrNoteType);
            spinner.setAdapter(spinnerAdapterTypeSave);
            spinner.setSelection(arrNoteType.indexOf(DatabaseManagerCopyDb.TYPE_WIFI_AVAILABLE));
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = adapterView.getItemAtPosition(i).toString();
                switch (type) {
                    case DatabaseManagerCopyDb.TYPE_CAPTURE:
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).apply();//Khi Chụp hoặc lưu ảnh mới sẽ mở lại type cũ lên
                        cursor = activity.swapDb(type);
                        recycleNoteAdapter.swapDataUseCursor(cursor);
                        tvCountNote.setText("" + cursor.getCount());
//                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    case DatabaseManagerCopyDb.TYPE_CLIP_BOARD:
                        // TODO: 9/20/2016 ---Done---Hiển thị thiếu clipboard
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).apply();
                        cursor = activity.swapDb(type);
                        recycleNoteAdapter.swapDataUseCursor(cursor);
                        tvCountNote.setText("" + cursor.getCount());
//                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    case DatabaseManagerCopyDb.TYPE_GALLERY:
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).apply();
                        cursor = activity.swapDb(type);
                        recycleNoteAdapter.swapDataUseCursor(cursor);
                        tvCountNote.setText("" + cursor.getCount());
//                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    case DatabaseManagerCopyDb.TYPE_SCREEN_SHOT:
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).apply();
                        cursor = activity.swapDb(type);
                        recycleNoteAdapter.swapDataUseCursor(cursor);
                        tvCountNote.setText("" + cursor.getCount());
//                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    case DatabaseManagerCopyDb.TYPE_TEXT_ONLY:
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, type).apply();
                        cursor = activity.swapDb(type);
                        recycleNoteAdapter.swapDataUseCursor(cursor);
                        tvCountNote.setText("" + cursor.getCount());
//                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    case "All":
                        sharedPreferences.edit().putString(KEY_TYPE_SAVE, "All").apply();
                        cursor = activity.swapDb("All");
                        recycleNoteAdapter.swapDataUseCursor(cursor);
                        tvCountNote.setText("" + cursor.getCount());
//                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;

                    case DatabaseManagerCopyDb.TYPE_WIFI_AVAILABLE:
                        tvCountNote.setText("" + rcv.getAdapter().getItemCount());
//                        ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(type + " ( " + cursor.getCount() + " ) ");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        allCursor.close();
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
                        case BUTTON_SAVE:
                            ibSave.setRotationX(values[0]);
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
        ibSave.setImageResource(R.drawable.ic_save_50dp);
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

        }

        /*Button Delete*/
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

        /*Button Save*/
        xMinIbSave = rlFloatOption.getX() + ibSave.getX();
        xMaxIbSave = rlFloatOption.getX() + ibSave.getX() + ibSave.getWidth();
        yMinIbSave = rlFloatOption.getY() + ibSave.getY();
        yMaxIbSave = rlFloatOption.getY() + ibSave.getY() + ibSave.getHeight();

        if ((rawX > xMinIbSave && rawX < xMaxIbSave)
                && (rawY > yMinIbSave && rawY < yMaxIbSave)) {
            buttonFloatOption = BUTTON_SAVE;
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
//                tvWifiInfo.setText(wifiGpsManager.getSSIDWifi());
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
                DialogMethodCreateNewNote dlgPick = new DialogMethodCreateNewNote(getActivity());
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

//    @Subscribe
//    public void onEvent(Integer[] data) {
//        switch (data[0]) {
//            case DatabaseManagerCopyDb.MSG_UPDATE_PERCENT_BACKUP:
//                try {
//                    // TODO: 9/16/2016 ---Done--- percent progress not set
//                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText("Backup : " + data[1] + " %");
//                } catch (java.lang.NullPointerException e) {
//                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
//                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình backup dữ liệu", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case DatabaseManagerCopyDb.MSG_BACKUP_COMPLETE:
//                try {
//                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(saveTvSpinner);
//                } catch (java.lang.NullPointerException e) {
//                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
//                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình backup dữ liệu", Toast.LENGTH_SHORT).show();
//                }
//                break;
//
//            case DatabaseManagerCopyDb.MSG_UPDATE_PERCENT_RESTORE:
//                try {
//                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText("Restore : " + data[1] + " %");
//                } catch (java.lang.NullPointerException e) {
//                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
//                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình restore dữ liệu", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case DatabaseManagerCopyDb.MSG_RESTORE_COMPLETE:
//                try {
//                    Log.d(TAG, "onEvent: saveTvSpinner: "+saveTvSpinner);
//                    ((TextView) spinner.getRootView().findViewById(R.id.tv_type_save)).setText(saveTvSpinner);
////                    ((TextView) findViewById(R.id.tv_title_action_bar)).setText("ALL NOTE (" + data[1] + ")");
//                    if(!sharedPreferences.getString(KEY_TYPE_SAVE,null).equals(null)){
//                        recycleNoteAdapter.swapDataUseCursor(activity.swapDb(sharedPreferences.getString(KEY_TYPE_SAVE,null)));
//                    }
//                } catch (java.lang.NullPointerException e) {
//                    Log.d(TAG, "onEvent: Null tý thôi k sao cứ chạy tiếp đi");
//                    Toast.makeText(getActivity(), "Dừng đột ngột trong quá trình restore dữ liệu", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            default:
//                break;
//        }
//    }
}