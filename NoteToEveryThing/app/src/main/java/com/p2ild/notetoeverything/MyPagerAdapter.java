package com.p2ild.notetoeverything;

import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by duypi on 8/24/2016.
 */
public class MyPagerAdapter extends PagerAdapter {

    private static final String TAG = MyPagerAdapter.class.getSimpleName();
    private Cursor cursor;
    private ImageView img;

    public MyPagerAdapter(Cursor cursor) {
        super();
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
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
    public Object instantiateItem(ViewGroup container, int position) {
        View rootView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_view_pager, container, false);
        cursor.moveToPosition(cursor.getCount() - 1 - position);

        ((TextView) rootView.findViewById(R.id.tv_title_note)).setText(cursor.getString(DatabaseManager.COLUMN_TITLE_NOTE));
        ((TextView) rootView.findViewById(R.id.tv_content_note)).setText(cursor.getString(DatabaseManager.COLUMN_CONTENT_NOTE));

        img = (ImageView) rootView.findViewById(R.id.iv_show_image);
        Glide
                .with(container.getContext())
                .load(new File(cursor.getString(DatabaseManager.COLUMN_PATH_IMAGE_NOTE)))
//                .fitCenter()
                .thumbnail(2f)
                .placeholder(R.drawable.placeholder)
                .into(img);
        container.addView(rootView);
        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
