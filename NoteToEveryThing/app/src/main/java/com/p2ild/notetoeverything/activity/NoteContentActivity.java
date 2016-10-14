package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.p2ild.notetoeverything.DatabaseManagerCopyDb;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.MyPagerAdapterActNoteContent;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.other.DataSerializable;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by duypi on 8/24/2016.
 */
public class NoteContentActivity extends Activity implements View.OnClickListener {
    private static final String TAG = NoteContentActivity.class.getSimpleName();
    Random rd = new Random();
    private ImageButton ibBack, ibSave, ibShowOnMap;
    private ViewPager vp;
    private DatabaseManagerCopyDb db;
    private MyPagerAdapterActNoteContent myPagerAdapterActNoteContent;
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
    private ArrayList<NoteItem> arrData;
    private int positionClick;
    private int currentPage = -1;
    private ArrayList<NoteItem> showNoteInMap;

    // TODO: 8/31/2016 Chưa xử lý sự kiện cho các button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_content);
        db = new DatabaseManagerCopyDb(this);
        arrData = (ArrayList) ((DataSerializable) getIntent().getSerializableExtra(MainActivity.KEY_ARR_DATA)).getData();
        positionClick = getIntent().getIntExtra(MainActivity.KEY_POSTION_CLICK, 0);


        (ibBack = (ImageButton) findViewById(R.id.ib_back)).setOnClickListener(this);
        (ibSave = (ImageButton) findViewById(R.id.ib_save_activity_content)).setOnClickListener(this);
        checkCanSaveImage(positionClick);

        (ibShowOnMap = (ImageButton) findViewById(R.id.ib_show_on_map)).setOnClickListener(this);
        checkCanMapLocation(positionClick);

        initViewPager(arrData, positionClick);
    }

    private void checkCanMapLocation(int position) {
        if (arrData.get(position).getLatlong() == null
                || arrData.get(position).getLatlong().equals("")) {
            ibShowOnMap.setVisibility(View.INVISIBLE);
        } else {
            ibShowOnMap.setVisibility(View.VISIBLE);
        }
    }

    private void checkCanSaveImage(int postion) {
        if (arrData.get(postion).getTypeSave().equals(DatabaseManagerCopyDb.TYPE_CLIP_BOARD)
                || arrData.get(postion).getTypeSave().equals(DatabaseManagerCopyDb.TYPE_TEXT_ONLY)) {
            ibSave.setVisibility(View.INVISIBLE);
        } else {
            ibSave.setVisibility(View.VISIBLE);
        }
    }

    private void initViewPager(final ArrayList<NoteItem> arrData, final int positionClick) {
        myPagerAdapterActNoteContent = new MyPagerAdapterActNoteContent(arrData);
        vp = (ViewPager) findViewById(R.id.view_pager);
        int itemMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        int pagerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        vp.setPageMargin(itemMargin);
        vp.setClipToPadding(false);
        vp.setPadding(pagerPadding, pagerPadding, pagerPadding, pagerPadding);
        vp.addOnPageChangeListener(mPageChangeListioner);
        vp.setAdapter(myPagerAdapterActNoteContent);
        vp.setCurrentItem(positionClick);
        // TODO: 8/31/2016 ---Done--- PageTitle load sai
        ((TextView) findViewById(R.id.tv_title_in_content_note)).setText(myPagerAdapterActNoteContent.getPageTitle(positionClick));
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                checkCanSaveImage(position);
                checkCanMapLocation(position);
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.ib_save_activity_content:
                if (currentPage == -1) {
                    db.exportImageToExternal(arrData.get(positionClick).getPathImg());
                } else {
                    db.exportImageToExternal(arrData.get(currentPage).getPathImg());
                }
                break;
            case R.id.ib_show_on_map:
                showNoteInMap = new ArrayList<>();
                if (currentPage == -1) {
                    showNoteInMap.add(arrData.get(positionClick));
                } else {
                    showNoteInMap.add(arrData.get(currentPage));
                }
                startActivity(
                        new Intent(NoteContentActivity.this, MapActivity.class)
                                .putExtra(MainActivity.KEY_OBJECT_DB, new DataSerializable(showNoteInMap))
                );
                break;
            default:
                break;
        }
    }
}