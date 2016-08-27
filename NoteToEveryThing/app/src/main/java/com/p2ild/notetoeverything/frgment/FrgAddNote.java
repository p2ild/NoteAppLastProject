package com.p2ild.notetoeverything.frgment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.p2ild.notetoeverything.activity.MainActivity;
import com.p2ild.notetoeverything.R;


/**
 * Created by duypi on 8/21/2016.
 */
public class FrgAddNote extends Fragment implements View.OnClickListener {
    private static final String TAG = FrgAddNote.class.getSimpleName();
    private EditText edNoteTitle, edNoteContent;
    private Button btCapture;
    private String titleNote,contentNote;
    private InputMethodManager im;

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
        // TODO: 8/26/2016 keyboard không biến mất khi click capture
        (btCapture = (Button) rootView.findViewById(R.id.bt_capture_image)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: ");
        replaceCharApostrophe();
        ((MainActivity) getActivity()).showFrgCapture();

        im.hideSoftInputFromWindow(edNoteTitle.getWindowToken(),0);
        im.hideSoftInputFromWindow(edNoteContent.getWindowToken(),0);
        super.onDetach();
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
        return titleNote;
    }

    public String getNoteContent() {
        return contentNote;
    }

    private void replaceCharApostrophe(){
        titleNote = edNoteTitle.getText().toString();
        contentNote = edNoteContent.getText().toString();

        if(!titleNote.contains("'") && !contentNote.contains("'")){
            return;
        }

        titleNote = titleNote.replace("'","''");
        contentNote = contentNote.replace("'","''");

    }
}
