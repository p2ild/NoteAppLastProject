package com.p2ild.notetoeverything;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

/**
 * Created by duypi on 9/8/2016.
 */
public class ScreenShotDialog extends Dialog {
    public ScreenShotDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_screen_shots);
    }
}
