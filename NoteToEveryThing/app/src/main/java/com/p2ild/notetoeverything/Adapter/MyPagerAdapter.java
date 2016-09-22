package com.p2ild.notetoeverything.adapter;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.other.DatabaseManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by duypi on 8/24/2016.
 */
public class MyPagerAdapter extends PagerAdapter {

    private static final String TAG = MyPagerAdapter.class.getSimpleName();
    private final DatabaseManager db;
    private ImageView img;
    private ArrayList<NoteItem> data;
    private EditText edNoteContent;

    public MyPagerAdapter(ArrayList<NoteItem> arr, DatabaseManager db) {
        super();
        data = arr;
        this.db = db;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return data.get(position).getNoteTitle();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View rootView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_view_pager, container, false);

        img = (ImageView) rootView.findViewById(R.id.iv_show_image);
        Glide
                .with(container.getContext())
                .load(new File(data.get(position).getPathImg()))
//                .fitCenter()
                .priority(Priority.IMMEDIATE)
                .into(img);
        ((EditText)rootView.findViewById(R.id.ed_content_note)).setText(data.get(position).getNoteContent());
        container.addView(rootView);
        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void swapData(ArrayList arrayList) {
        data.clear();
        data.addAll(arrayList);
        notifyDataSetChanged();
    }
}
