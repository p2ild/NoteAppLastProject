package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.Adapter.MyPagerAdapter;
import com.p2ild.notetoeverything.R;

/**
 * Created by duypi on 8/24/2016.
 */
public class NoteContentActivity extends Activity implements View.OnClickListener {
    private ImageButton ibBack,ibAddNote,ibOption;
    private static final String TAG = NoteContentActivity.class.getSimpleName();
    private ViewPager vp;

    // TODO: 8/31/2016 Chưa xử lý sự kiện cho các button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_content);
        DatabaseManager db = new DatabaseManager(this);
        int positionClick = getIntent().getIntExtra(MainActivity.KEY_POSITION,0);

        (ibBack=(ImageButton) findViewById(R.id.ib_back)).setOnClickListener(this);
        (ibBack=(ImageButton) findViewById(R.id.ib_add_note)).setOnClickListener(this);
        (ibBack=(ImageButton) findViewById(R.id.ib_options)).setOnClickListener(this);
        Cursor c = db.readAllData();

        c.moveToLast();

        initViewPager(positionClick, c);
    }

    private void initViewPager(int positionClick, Cursor c) {
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(c);
        vp = (ViewPager) findViewById(R.id.view_pager);
        vp.setAdapter(myPagerAdapter);
        vp.setCurrentItem(positionClick);
        // TODO: 8/31/2016 PageTitle load sai
        ((TextView)findViewById(R.id.tv_title_note)).setText(myPagerAdapter.getPageTitle(positionClick));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
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