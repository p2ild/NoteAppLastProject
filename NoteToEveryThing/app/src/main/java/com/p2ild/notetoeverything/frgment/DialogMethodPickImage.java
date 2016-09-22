package com.p2ild.notetoeverything.frgment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;

/**
 * Created by duypi on 9/10/2016.
 */
public class DialogMethodPickImage extends Dialog implements View.OnClickListener {
//    private final FrgAddNote frgAddNote;
    private final Activity activity;

    public DialogMethodPickImage(Context context) {
        super(context);
        this.activity = (Activity) context;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_method_pick);
        (findViewById(R.id.iv_gallery)).setOnClickListener(this);
        (findViewById(R.id.iv_cature)).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_cature:
                ((MainActivity)activity).showFrgCapture();
                dismiss();
                break;
            case R.id.iv_gallery:
                ((MainActivity)activity).showGallery();
                dismiss();
                break;
            default:
                break;
        }
    }
}
