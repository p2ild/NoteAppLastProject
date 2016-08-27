package com.p2ild.notetoeverything.frgment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;


/**
 * Created by duypi on 8/21/2016.
 */
public class FrgAddNote extends Fragment implements View.OnTouchListener {
    public static final int BUTTON_CAPTURE = 2;
    private static final String TAG = FrgAddNote.class.getSimpleName();
    private static final int BUTTON_BACK = 0;
    private EditText edNoteTitle, edNoteContent;
    private InputMethodManager im;
    private Drawable[] buttonDrawable;

    public FrgAddNote() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frg_add_note, container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initView(View rootView) {
        edNoteTitle = (EditText) rootView.findViewById(R.id.ed_note_title);
        edNoteContent = (EditText) rootView.findViewById(R.id.ed_note_content);
        edNoteTitle.setOnTouchListener(this);
        buttonDrawable = edNoteTitle.getCompoundDrawables();

        // TODO: 8/26/2016 (----Done----)keyboard không biến mất khi click capture
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public String getNoteTitle() {
        return edNoteTitle.getText().toString();
    }

    public String getNoteContent() {
        return edNoteContent.getText().toString();
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getRawX() > (edNoteTitle.getRight() - buttonDrawable[BUTTON_CAPTURE].getBounds().width())) {

                    edNoteTitle.setFocusable(false);
                    edNoteContent.setFocusable(false);
                    im.hideSoftInputFromWindow(edNoteTitle.getWindowToken(), 0);
                    im.hideSoftInputFromWindow(edNoteContent.getWindowToken(), 0);

                    ((MainActivity) getActivity()).showFrgCapture();
                    break;
                }
                if (motionEvent.getRawX() < (edNoteTitle.getLeft() + buttonDrawable[BUTTON_BACK].getBounds().width())) {
                    edNoteTitle.setFocusable(false);
                    edNoteContent.setFocusable(false);
                    im.hideSoftInputFromWindow(edNoteTitle.getWindowToken(), 0);
                    im.hideSoftInputFromWindow(edNoteContent.getWindowToken(), 0);
                    ((MainActivity) getActivity()).onBackPressed();
                    break;
                }
                break;
            default:
                break;
        }
        return false;
    }
}
