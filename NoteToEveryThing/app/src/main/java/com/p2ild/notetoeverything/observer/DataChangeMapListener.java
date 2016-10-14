package com.p2ild.notetoeverything.observer;

import com.p2ild.notetoeverything.adapter.NoteItem;

import java.util.ArrayList;

/**
 * Created by duypi on 2016-10-12.
 */
public interface DataChangeMapListener {
    void initMarkerComplete(ArrayList<NoteItem> newData);
}
