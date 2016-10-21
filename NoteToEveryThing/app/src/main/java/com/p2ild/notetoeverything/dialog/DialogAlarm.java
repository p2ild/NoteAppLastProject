package com.p2ild.notetoeverything.dialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.tv.TvView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;

/**
 * Created by duypi on 9/10/2016.
 */
public class DialogAlarm extends Dialog implements View.OnClickListener {
    private static final String TAG = DialogAlarm.class.getSimpleName();
    //    private final FrgAddNote frgAddNote;
    private final Activity activity;
    private final DatePicker datePicker;
    private final TimePicker timePicker;
    private TextView tvPickTime,tvPickDate;
    private ImageButton imageButton;

    public DialogAlarm(Context context) {
        super(context);
        this.activity = (Activity) context;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setCancelable(false);
        setContentView(R.layout.dialog_alarm_pick);
        YoYo.with(Techniques.DropOut).duration(1500).playOn((RelativeLayout)findViewById(R.id.root_view_alarm));

        tvPickTime = (TextView)findViewById(R.id.tv_pick_time);
        tvPickTime.setOnClickListener(this);

        tvPickDate = (TextView)findViewById(R.id.tv_pick_date);
        tvPickDate.setOnClickListener(this);

        imageButton = (ImageButton)findViewById(R.id.ib_set_alarm);
        imageButton.setOnClickListener(this);

        datePicker =(DatePicker)findViewById(R.id.d_pick);
        timePicker = (TimePicker)findViewById(R.id.t_pick);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_pick_time:
                YoYo.with(Techniques.SlideOutRight).duration(1500).playOn(datePicker);
                timePicker.setVisibility(View.VISIBLE);
                YoYo
                        .with(Techniques.SlideInLeft)
                        .duration(1500)
                        .withListener(new Animator.AnimatorListener () {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                datePicker.setVisibility(View.GONE);
                                tvPickTime.setVisibility(View.GONE);
                                tvPickDate.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .playOn(timePicker);
                break;
            case R.id.tv_pick_date:
                YoYo.with(Techniques.SlideOutLeft).duration(1500).playOn(timePicker);
                datePicker.setVisibility(View.VISIBLE);
                YoYo
                        .with(Techniques.SlideInRight)
                        .duration(1500)
                        .withListener(new Animator.AnimatorListener () {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                datePicker.setVisibility(View.VISIBLE);
                                tvPickTime.setVisibility(View.VISIBLE);
                                tvPickDate.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .playOn(datePicker);
                break;
            case R.id.ib_set_alarm:
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();

                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                Log.d(TAG, "onClick: day,month,year :: hour,minute "+day+","+month+","+year+"|||"+hour+","+minute);
                dismiss();
                break;
            default:
                break;
        }
    }
}
