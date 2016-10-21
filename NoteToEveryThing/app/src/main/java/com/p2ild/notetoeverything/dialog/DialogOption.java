package com.p2ild.notetoeverything.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.NotePagerActivity;

/**
 * Created by duypi on 2016-10-21.
 */
public class DialogOption extends Dialog implements View.OnClickListener {
    private final Context context;
    private ImageButton ibSave, ibShowOnMap;
    private ImageButton ibShare;

    public DialogOption(Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.dialog_option);
        Window window = getWindow();
        window.setGravity(Gravity.TOP | Gravity.RIGHT);

        initView();
    }

    private void initView() {
        (ibSave = (ImageButton) findViewById(R.id.ib_save_activity_content)).setOnClickListener(this);
        (ibShowOnMap = (ImageButton) findViewById(R.id.ib_show_on_map)).setOnClickListener(this);
        (ibShare = (ImageButton) findViewById(R.id.ib_share_image)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_save_activity_content:
                ((NotePagerActivity)context).saveImage();
                dismiss();
                break;
            case R.id.ib_show_on_map:
                ((NotePagerActivity)context).showOnMap();
                dismiss();
                break;
            case R.id.ib_share_image:
                ((NotePagerActivity)context).shareNote();
                dismiss();
            default:
                break;
        }
    }
}
