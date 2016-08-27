package com.p2ild.notetoeverything;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by duypi on 8/20/2016.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.Holder> implements View.OnTouchListener {
    private static final String TAG = NoteAdapter.class.getSimpleName();
    private static final int THUMBSIZE_X = 512;
    private final Context context;
    private Cursor cursor;
    private Bitmap bitmap;

    public NoteAdapter(Context context,Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_recycler, parent, false);

        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        cursor.moveToPosition(cursor.getCount()-1-position);
        holder.tvTitleNote.setText(cursor.getString(DatabaseManager.COLUMN_TITLE_NOTE));
        holder.tvContentNote.setText(cursor.getString(DatabaseManager.COLUMN_CONTENT_NOTE));
        Glide
                .with(context)
                .load(new File(cursor.getString(DatabaseManager.COLUMN_PATH_IMAGE_NOTE)))
                .placeholder(R.drawable.placeholder)
                .crossFade()
                .thumbnail(0.5f)
                .into(holder.imgPreview);
        holder.card.setOnTouchListener(this);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            default:
                break;
        }
        return false;
    }

    //Load ảnh sử dụng BitmapFacetory--Tốc độ chậm
    public Bitmap loadImage(String pathImageThumbnail) {
        try {
            InputStream inputStream = new FileInputStream(pathImageThumbnail);
            Bitmap bitmapOri = BitmapFactory.decodeStream(inputStream);
            Log.d(TAG,"getWidth: "+bitmapOri.getWidth());
            Log.d(TAG,"getHeight: "+bitmapOri.getHeight());
            float ratio = (THUMBSIZE_X*1F / bitmapOri.getWidth());
            Log.d(TAG,"ratio: "+ratio);
            bitmap = ThumbnailUtils.extractThumbnail(bitmapOri, THUMBSIZE_X, THUMBSIZE_X);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView tvTitleNote, tvContentNote;
        public ImageView imgPreview;
        public CardView card;

        public Holder(View itemView) {
            super(itemView);
            tvTitleNote = (TextView) itemView.findViewById(R.id.tv_title_note);
            tvContentNote = (TextView) itemView.findViewById(R.id.tv_content_note);
            imgPreview = (ImageView) itemView.findViewById(R.id.img_preview);

            card = (CardView) itemView.findViewById(R.id.card_view);
        }
    }
}
