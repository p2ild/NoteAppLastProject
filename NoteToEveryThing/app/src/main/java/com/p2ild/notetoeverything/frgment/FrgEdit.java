package com.p2ild.notetoeverything.frgment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;

/**
 * Created by duypi on 8/25/2016.
 */
public class FrgEdit extends Fragment implements View.OnTouchListener {
    private static final String TAG = FrgEdit.class.getSimpleName();
    private static final int BUTTON_CAPTURE = 2;
    private static final int BUTTON_BACK = 0;
    private final String titleNote, contentNote;
    private View rootView;
    private Button btSave;
    private EditText edContentNote, edTitleNote;
    private String oldTitle;
    private InputMethodManager im;
    private Drawable[] buttonDrawable;

    public FrgEdit(String titleNote, String contentNote) {
        this.titleNote = titleNote;
        this.contentNote = contentNote;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frg_add_note, container, false);

        (edTitleNote = (EditText) rootView.findViewById(R.id.ed_note_title)).setText(titleNote);
        oldTitle = titleNote;

        (edContentNote = (EditText) rootView.findViewById(R.id.ed_note_content)).setText(contentNote);

        Drawable buttonBack= getResources().getDrawable(R.drawable.ic_left_arrow_angle);
        Drawable buttonSave= getResources().getDrawable(R.drawable.ic_save);
        buttonBack.setBounds(new Rect(0,0,buttonBack.getIntrinsicWidth(),buttonBack.getIntrinsicHeight()));
        buttonSave.setBounds(new Rect(0,0,buttonSave.getIntrinsicWidth(),buttonSave.getIntrinsicHeight()));
        edTitleNote.setCompoundDrawables(buttonBack,null,buttonSave,null);
        buttonDrawable = edTitleNote.getCompoundDrawables();
        edTitleNote.setOnTouchListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getRawX() > (edTitleNote.getRight() - buttonDrawable[BUTTON_CAPTURE].getBounds().width())) {
                    Log.d(TAG, "onTouch: SAVE ICON");
                    String newTitle = edTitleNote.getText().toString();
                    String newContent = edContentNote.getText().toString();

                    edTitleNote.setFocusable(false);
                    edContentNote.setFocusable(false);
                    im.hideSoftInputFromWindow(edContentNote.getWindowToken(), 0);
                    im.hideSoftInputFromWindow(edTitleNote.getWindowToken(), 0);

                    ((MainActivity) getActivity()).updateDataBase(oldTitle, newTitle, newContent);
                    break;
                }
                if (motionEvent.getRawX() < (edTitleNote.getLeft() + buttonDrawable[BUTTON_BACK].getBounds().width())) {
                    Log.d(TAG, "onTouch: BACK ICON");
                    edTitleNote.setFocusable(false);
                    edContentNote.setFocusable(false);
                    im.hideSoftInputFromWindow(edContentNote.getWindowToken(), 0);
                    im.hideSoftInputFromWindow(edTitleNote.getWindowToken(), 0);
                    ((MainActivity) getActivity()).onBackPressed();
                    break;
                }
                break;
            default:
                break;
        }
        return false;
    }

    // TODO: 8/25/2016(-----Done-----) Chưa xử lý khi ấn back
}
