package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.other.DataSerializable;
import com.p2ild.notetoeverything.adapter.MyPagerAdapterActNoteContent;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.DatabaseManager;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by duypi on 8/24/2016.
 */
public class NoteContentActivity extends Activity implements View.OnClickListener {
    private static final String TAG = NoteContentActivity.class.getSimpleName();
    private ImageButton ibBack, ibAddNote, ibOption;
    private ViewPager vp;
    private DatabaseManager db;
    private MyPagerAdapterActNoteContent myPagerAdapterActNoteContent;
    Random rd = new Random();
    private ViewPager.OnPageChangeListener mPageChangeListioner = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            ((TextView) findViewById(R.id.tv_title_in_content_note)).setText(myPagerAdapterActNoteContent.getPageTitle(position));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    // TODO: 8/31/2016 Chưa xử lý sự kiện cho các button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_content);
        db = new DatabaseManager(this,null);
        ArrayList arrData = (ArrayList) ((DataSerializable) getIntent().getSerializableExtra(MainActivity.KEY_ARR_DATA)).getData();
        int positionClick = getIntent().getIntExtra(MainActivity.KEY_POSTION_CLICK, 0);

        (ibBack = (ImageButton) findViewById(R.id.ib_back)).setOnClickListener(this);
        (ibBack = (ImageButton) findViewById(R.id.ib_add_note)).setOnClickListener(this);
        (ibBack = (ImageButton) findViewById(R.id.ib_options)).setOnClickListener(this);

        initViewPager(arrData, positionClick);
    }

    private void initViewPager(ArrayList<NoteItem> arrData, int positionClick) {
        myPagerAdapterActNoteContent = new MyPagerAdapterActNoteContent(arrData);
        vp = (ViewPager) findViewById(R.id.view_pager);
        int itemMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50,getResources().getDisplayMetrics());
        int pagerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10,getResources().getDisplayMetrics());
        vp.setPageMargin(itemMargin);
        vp.setClipToPadding(false);
        vp.setPadding(pagerPadding, pagerPadding, pagerPadding, pagerPadding);
        vp.addOnPageChangeListener(mPageChangeListioner);
        vp.setAdapter(myPagerAdapterActNoteContent);
        vp.setCurrentItem(positionClick);
        // TODO: 8/31/2016 ---Done--- PageTitle load sai
        ((TextView) findViewById(R.id.tv_title_in_content_note)).setText(myPagerAdapterActNoteContent.getPageTitle(positionClick));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.ib_add_note:
                Toast.makeText(NoteContentActivity.this, "Chưa hợp lý", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ib_options:
                Toast.makeText(NoteContentActivity.this, "Chưa xử lý", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}