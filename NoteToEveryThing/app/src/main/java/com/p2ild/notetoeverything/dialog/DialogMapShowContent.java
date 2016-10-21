package com.p2ild.notetoeverything.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.p2ild.notetoeverything.R;

/**
 * Created by duypi on 2016-10-21.
 */
public class DialogMapShowContent extends Dialog{
    private static final String TAG = DialogMapShowContent.class.getSimpleName();

    public DialogMapShowContent(Context context, String title, String content,String pathImg) {
        super(context);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.dialog_img_content_map);
        Log.d(TAG, "DialogMapShowContent: "+pathImg);
        ((TextView)findViewById(R.id.tv_title_dialog_map)).setText(title);
        if(pathImg!=null && !pathImg.equals("")){
        Glide.with(context)
                .load(pathImg)
                .crossFade()
                .into(((ImageView)findViewById(R.id.iv_img_on_map)));}
        else {
            ((TextView)findViewById(R.id.tv_content_dialog_map)).setText(content);
        }

//        ((ImageView)findViewById(R.id.iv_img_on_map)).setImageBitmap(BitmapFactory.decodeFile(pathImg));
    }
}
