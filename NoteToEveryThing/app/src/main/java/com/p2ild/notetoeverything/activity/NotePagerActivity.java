package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.adapter.MyPagerAdapterActNoteContent;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.dialog.DialogOption;
import com.p2ild.notetoeverything.other.DataSerializable;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by duypi on 8/24/2016.
 */
public class NotePagerActivity extends Activity implements View.OnClickListener {
    private static final String TAG = NotePagerActivity.class.getSimpleName();
    Random rd = new Random();
    private ImageButton ibBack,ibMenuOption;
    private ViewPager vp;
    private DatabaseManager db;
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
        db = new DatabaseManager(this, null);
        arrData = (ArrayList) ((DataSerializable) getIntent().getSerializableExtra(MainActivity.KEY_ARR_DATA)).getData();
        positionClick = getIntent().getIntExtra(MainActivity.KEY_POSTION_CLICK, 0);


        (ibBack = (ImageButton) findViewById(R.id.ib_back)).setOnClickListener(this);
        (ibMenuOption = (ImageButton) findViewById(R.id.ib_menu_content_note)).setOnClickListener(this);

//        checkCanSaveImage(positionClick);
//        checkCanMapLocation(positionClick);

        initViewPager(arrData, positionClick);
    }

    private boolean checkCanMapLocation(int position) {
        if (arrData.get(position).getLatlong().equals("null")
                || arrData.get(position).getLatlong().equals("")) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkCanSaveImage(int postion) {
        if (arrData.get(postion).getTypeSave().equals(DatabaseManager.TYPE_CLIP_BOARD)
                || arrData.get(postion).getTypeSave().equals(DatabaseManager.TYPE_TEXT_ONLY)) {
            return false;
        } else {
            return true;
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
//                checkCanSaveImage(position);
//                checkCanMapLocation(position);
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
            case R.id.ib_menu_content_note:
                DialogOption dialogOption = new DialogOption(this);
                dialogOption.show();
                break;
            default:
                break;
        }
    }

    public void showOnMap() {
        showNoteInMap = new ArrayList<>();
        if (currentPage == -1) {
            if(checkCanMapLocation(positionClick)){
                showNoteInMap.add(arrData.get(positionClick));
            }else {
                Toast.makeText(this, "Ghi chú không có tọa độ trên bản đồ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if(checkCanMapLocation(currentPage)){
                showNoteInMap.add(arrData.get(currentPage));
            }else {
                Toast.makeText(this, "Ghi chú không có tọa độ trên bản đồ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        startActivity(
                new Intent(NotePagerActivity.this, MapActivity.class)
                        .putExtra(MainActivity.KEY_OBJECT_DB, new DataSerializable(showNoteInMap))
        );
    }

    public void saveImage() {
        if (currentPage == -1) {
            if(checkCanSaveImage(positionClick)){
                db.exportImageToExternal(arrData.get(positionClick).getPathImg());
            }else {
                Toast.makeText(this, "Ghi chú không tồn tại nội dung để trích xuất", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if(checkCanSaveImage(currentPage)){
                db.exportImageToExternal(arrData.get(currentPage).getPathImg());
            }else {
                Toast.makeText(this, "Ghi chú không tồn tại nội dung để trích xuất", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    public void shareNote() {
        String pathImgShare ="";

        if (currentPage == -1) {
            if(checkCanSaveImage(positionClick)){
                pathImgShare = arrData.get(positionClick).getPathImg();
            }else {
                Toast.makeText(this, "Ghi chú không tồn tại nội dung để chia sẻ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if(checkCanSaveImage(currentPage)){
                pathImgShare = arrData.get(currentPage).getPathImg();
            }else {
                Toast.makeText(this, "Ghi chú không tồn tại nội dung để chia sẻ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ArrayList<Uri> imageUris = new ArrayList<Uri>();
            //Copy ảnh ra thư mục external thì các app khác mới có thể truy cập vào ảnh
            File imgPathExternal = new File(db.copyImgShare(pathImgShare));
            imageUris.add(Uri.fromFile(imgPathExternal));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM,imageUris.get(0));
//                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share images to.."));
    }
}