package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.MyPagerAdapter;
import com.p2ild.notetoeverything.R;

/**
 * Created by duypi on 8/24/2016.
 */
public class NoteContentActivity extends Activity{

    private static final String TAG = NoteContentActivity.class.getSimpleName();
    private ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_content);
        DatabaseManager db = new DatabaseManager(this);
        int positionClick = getIntent().getIntExtra(MainActivity.KEY_POSITION,0);

        Cursor c = db.readAllData();

        c.moveToLast();

        initViewPager(positionClick, c);
    }

    private void initViewPager(int positionClick, Cursor c) {
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(c);
        vp = (ViewPager) findViewById(R.id.view_pager);
        vp.setAdapter(myPagerAdapter);
        vp.setCurrentItem(positionClick);
    }
}
