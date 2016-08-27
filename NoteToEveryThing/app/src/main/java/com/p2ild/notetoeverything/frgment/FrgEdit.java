package com.p2ild.notetoeverything.frgment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
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
public class FrgEdit extends Fragment implements View.OnClickListener {
    private static final String TAG = FrgEdit.class.getSimpleName();
    private final String titleNote, contentNote;
    private View rootView;
    private Button btSave;
    private EditText edContentNote,edTitleNote;
    private String oldTitle;
    private InputMethodManager im;

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

        (btSave = (Button) rootView.findViewById(R.id.bt_capture_image)).setText("Save");
        btSave.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_capture_image:
                String newTitle = edTitleNote.getText().toString();
                String newContent = edContentNote.getText().toString();
                ((MainActivity)getActivity()).updateDataBase(oldTitle,newTitle,newContent);
                im.hideSoftInputFromWindow(edTitleNote.getWindowToken(),0);
                im.hideSoftInputFromWindow(edContentNote.getWindowToken(),0);
                super.onDetach();
                break;
            default:
                break;
        }
    }

    // TODO: 8/25/2016(-----Done-----) Chưa xử lý khi ấn back
}
