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
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;

import java.io.File;


/**
 * Created by duypi on 8/21/2016.
 */
public class FrgAddNote extends Fragment implements View.OnTouchListener {
    public static final int BUTTON_CAPTURE = 2;
    private static final String TAG = FrgAddNote.class.getSimpleName();
    private static final int BUTTON_BACK = 0;
    private final String imgPath;
    private final String imgThumbnailPath;
    private final String typeSave;
    private EditText edNoteTitle, edNoteContent;
    private InputMethodManager im;
    private Drawable[] buttonDrawable;
    private ImageView iv_img_frg_add_note;

    public FrgAddNote(String imgPath, String imgThumbnailPath, String typeSave) {
        this.imgPath = imgPath;
        this.imgThumbnailPath = imgThumbnailPath;
        this.typeSave = typeSave;
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
        iv_img_frg_add_note = (ImageView)rootView.findViewById(R.id.iv_img_frg_add_note);
        buttonDrawable = edNoteTitle.getCompoundDrawables();
        Glide
                .with(getActivity())
                .load(new File(imgPath))
                .into(iv_img_frg_add_note);
        // TODO: 8/26/2016 ---Done--- keyboard không biến mất khi click capture
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

                    ((MainActivity)getActivity()).insertToDataBase(getNoteTitle(),getNoteContent(),imgPath,imgThumbnailPath,typeSave);
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
