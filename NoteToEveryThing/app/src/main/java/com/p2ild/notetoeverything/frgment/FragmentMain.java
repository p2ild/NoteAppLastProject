package com.p2ild.notetoeverything.frgment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.Adapter.NoteAdapter;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.RecycleViewOnItemTouch;
import com.p2ild.notetoeverything.WifiGpsManager;
import com.p2ild.notetoeverything.CustomStaggeredGridLayoutManager;
import com.p2ild.notetoeverything.activity.MainActivity;


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
    private final Cursor cursor;
    private View rootView;
    private RecyclerView rcv;
    private LinearLayout llActionBar;
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
    private WifiGpsManager wifiGpsManager;
    private Handler handler;
    private RecyclerView.OnItemTouchListener rcvOnItemTouchListioner;
    private CustomStaggeredGridLayoutManager customStaggeredGridLayoutManager;//Custom view cho phép dừng hoặc tiếp tục scroll recycleView
    private boolean isLongClick;//Khônng cho phép ACTION_FOCUS và ACTION_UP thực thi khi chưa LONG_CLICK

    public FragmentMain(Cursor cursor) {
        this.cursor = cursor;
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
        final NoteAdapter noteAdapter = new NoteAdapter(getActivity(), cursor);
        initViewChild(noteAdapter);
        return rootView;
    }

    private void initViewChild(final NoteAdapter noteAdapter) {
        (btAddNote = (ImageButton) rootView.findViewById(R.id.bt_add_note)).setOnClickListener(this);

        // TODO: 8/31/2016 Chưa đặt snipper
        ((TextView) rootView.findViewById(R.id.tv_title_action_bar)).setText("All note( " + cursor.getCount() + " )");

        isLongClick=false;

        wifiGpsManager = new WifiGpsManager(getActivity());

        //init Recycle View
        rcv = (RecyclerView) rootView.findViewById(R.id.rcv);
        rcv.setHasFixedSize(true);
        customStaggeredGridLayoutManager = new CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rcv.setLayoutManager(customStaggeredGridLayoutManager);
        customStaggeredGridLayoutManager.setCanScroll(true);


        rcv.setAdapter(noteAdapter);
        rcvOnItemTouchListioner = new RecycleViewOnItemTouch(getActivity(), rcv, new RecycleViewOnItemTouch.onItemClick() {
            // TODO: 8/25/2016  (-----Done-----) Chưa xử lý code long press khi action up thì option float sẽ biến mất
            @Override
            public void onClick(View view, int position) {
                ((MainActivity) getActivity()).startNoteActivity(position);
            }

            /*Hiện float option*/
            @Override
            public void onLongClick(View view, int position, float rawX, float rawY) {
                // TODO: 8/26/2016 Lần đầu tiên load float option sai vị trí
                isLongClick = true;
                rlFloatOption.setX(rawX - rlFloatOption.getWidth() / 2);
                rlFloatOption.setY(rawY - 200 - rlFloatOption.getHeight() / 2);

                widthScreen = getResources().getDisplayMetrics().widthPixels;
                heightScreen = getResources().getDisplayMetrics().widthPixels;
                rlFloatOption.setVisibility(View.VISIBLE);
                positionCurrentItem = cursor.getCount() - 1 - position;
                customStaggeredGridLayoutManager.setCanScroll(false);

                setDefaultDrawableImageButton();

            }

            @Override
            public void onActionFocus(float rawX, float rawY) {
                if(!isLongClick){
                    return;
                }
                switchButton(rawX, rawY);
                switch (buttonFloatOption) {
                    case BUTTON_EDIT:
//                        if (!runAnim) {
//                            YoYo.with(Techniques.RotateInUpLeft).duration(500).playOn(ibEdit);
//                        }
                        ibEdit.setImageResource(R.drawable.ic_pencil_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
//                        runAnim=true;
                        break;
                    case BUTTON_ALARM:
//                        YoYo.with(Techniques.RotateIn).duration(500).playOn(ibAlarm);
                        ibAlarm.setImageResource(R.drawable.ic_alarm_clock_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    case BUTTON_SHARE:
//                        YoYo.with(Techniques.RotateIn).duration(500).playOn(ibDelete);
                        ibShare.setImageResource(R.drawable.ic_share_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    case BUTTON_DELETE:
//                        YoYo.with(Techniques.RotateIn).duration(500).playOn(ibDelete);
                        ibDelete.setImageResource(R.drawable.ic_recycling_focus);
                        buttonFloatOption = BUTTON_UNSELECTED;
                        break;
                    default:
                        setDefaultDrawableImageButton();
                        break;
                }
            }

            @Override
            public void onActionUp(float rawX, float rawY) {
                if(!isLongClick){
                    return;
                }
                switchButton(rawX, rawY);
                switch (buttonFloatOption) {
                    case BUTTON_EDIT:
                        rlFloatOption.setVisibility(View.GONE);
                        cursor.moveToPosition(positionCurrentItem);
                        String title = cursor.getString(DatabaseManager.COLUMN_TITLE_NOTE);
                        String content = cursor.getString(DatabaseManager.COLUMN_CONTENT_NOTE);

                        ((MainActivity) getActivity()).showFrgEdit(title, content);
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
                                ((MainActivity) getActivity()).deleteDb(noteTitleDelete);
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
                isLongClick=false;
//                buttonFloatOption=BUTTON_UNSELECTED;//Đặt lại default button tránh action focus nhảy lấy dữ button cũ để chạy
                customStaggeredGridLayoutManager.setCanScroll(true);
            }
        });
        rcv.addOnItemTouchListener(rcvOnItemTouchListioner);

        //init Float Option
        rlFloatOption = (RelativeLayout) rootView.findViewById(R.id.rl_float_option);
        rlFloatOption.setVisibility(View.GONE);
        ibEdit = (ImageButton) rootView.findViewById(R.id.ib_pencil);
        ibAlarm = (ImageButton) rootView.findViewById(R.id.ib_alarm);
        ibDelete = (ImageButton) rootView.findViewById(R.id.ib_recycling);
        ibShare = (ImageButton) rootView.findViewById(R.id.ib_share);

        llActionBar = (LinearLayout) rootView.findViewById(R.id.action_bar);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_add_note:
                ((MainActivity) getActivity()).showFrgAddNote();
                break;
        }
    }
}