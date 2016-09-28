package com.p2ild.notetoeverything.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by duypi on 8/20/2016.
 */
public class RecycleNoteAdapter extends RecyclerView.Adapter<RecycleNoteAdapter.Holder> {
    private static final String TAG = RecycleNoteAdapter.class.getSimpleName();
    private static final int THUMBSIZE_X = 512;
    private final Context context;
    private Cursor cursor;
    private Bitmap bitmap;
    private ArrayList<NoteItem> data;

    public RecycleNoteAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        data = cursorToArrayList(cursor);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_recycler, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.tvTitleNote.setText(data.get(position).getNoteTitle());
        holder.tvContentNote.setText(data.get(position).getNoteContent());
        if(data.get(position).getPathThumbnail().equals("")){
            holder.tvTitleNote.setText("[ClipBoard]"+holder.tvTitleNote.getText());
            holder.imgPreview.setVisibility(View.GONE);
        }
        else {
            String pathThumbnail =(data.get(position).getPathThumbnail());
            Glide
                    .with(context)
                    .load(new File(pathThumbnail))
                    .placeholder(R.drawable.placeholder)
                    .crossFade()
                    .thumbnail(0.5f)
                    .into(holder.imgPreview);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
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

    public ArrayList<NoteItem> getArrData() {
        return data;
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView tvTitleNote, tvContentNote;
        public ImageView imgPreview;
        public CardView card;
        public ImageButton cbCheck;

        public Holder(View itemView) {
            super(itemView);
            tvTitleNote = (TextView) itemView.findViewById(R.id.tv_title_note);
            tvContentNote = (TextView) itemView.findViewById(R.id.tv_content_note);
            imgPreview = (ImageView) itemView.findViewById(R.id.img_preview);
            cbCheck = (ImageButton)itemView.findViewById(R.id.ib_check) ;
            card = (CardView) itemView.findViewById(R.id.card_view);
        }
    }
    public void swapData(Cursor cursor){
        data.clear();
        data.addAll(cursorToArrayList(cursor));
        notifyDataSetChanged();
    }

    public static ArrayList<NoteItem> cursorToArrayList(Cursor cursor){
        ArrayList<NoteItem> temp = new ArrayList<>();
        for(cursor.moveToLast();!cursor.isBeforeFirst();cursor.moveToPrevious()){
            String noteTitle = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_TITLE_NOTE));
            String noteContent = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_CONTENT_NOTE));
            String notePathImg = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_PATH_IMAGE_NOTE));
            String notePathThumbnail = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE));
            String noteTypeSave = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_TYPE_SAVE));
            String latlong = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_LATLONG));
            temp.add(new NoteItem(noteTitle,noteContent,notePathImg,notePathThumbnail,noteTypeSave,latlong));
        }
        return temp;
    }
}
