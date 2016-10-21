package com.p2ild.notetoeverything.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

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
    private Bitmap bitmapOri;
//    public static final int[] placeHolderColor = {R.drawable.placeholder_primary, R.drawable.placeholder_primary, R.drawable.placeholder_primary, R.drawable.placeholder_primary};
//    public static final int[] placeHolderColor = {R.drawable.placeholder_greensea, R.drawable.placeholder_pomegranate, R.drawable.placeholder_wisteria, R.drawable.placeholder_nephretis};
    private Random random = new Random();

    public RecycleNoteAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;

        data = cursorToArrayList(cursor);
        Log.d(TAG, "RecycleNoteAdapter: Here i'm here: "+data.size());
    }

    public static ArrayList<NoteItem> cursorToArrayList(Cursor cursor) {
        ArrayList<NoteItem> temp = new ArrayList<>();
        if (cursor.getCount() > 0) {
            for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
                String noteTitle = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_TITLE_NOTE));
                String noteContent = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_CONTENT_NOTE));
                String notePathImg = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_PATH_IMAGE_NOTE));
                String notePathThumbnail = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE));
                String noteTypeSave = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_TYPE_SAVE));
                String latlong = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_LATLONG));
                String wifiName = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_WIFI_NAME));
                temp.add(new NoteItem(noteTitle, noteContent, notePathImg, notePathThumbnail, noteTypeSave, latlong, wifiName));
            }
        }
        return temp;
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
            String pathImg = (data.get(position).getPathImg());
            switch (data.get(position).getTypeSave()) {
                case DatabaseManager.TYPE_CLIP_BOARD:
                    holder.tvTitleNote.setText(holder.tvTitleNote.getText());
                    holder.tvTypeSaveItemRecycleView.setText("ClipBoard");
                    holder.imgPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    Glide
                            .with(context)
                            .load(R.drawable.placeholder_primary)
                            .fitCenter()
                            .crossFade()
                            .into(holder.imgPreview);
                    break;
                case DatabaseManager.TYPE_TEXT_ONLY:
                    holder.tvTitleNote.setText(holder.tvTitleNote.getText());
                    holder.tvTypeSaveItemRecycleView.setText("TextOnly");
                    holder.imgPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    Glide
                            .with(context)
                            .load(R.drawable.placeholder_primary)
                            .crossFade()
                            .fitCenter()
                            .into(holder.imgPreview);
                    break;
                case DatabaseManager.TYPE_CAPTURE:
                    holder.tvTitleNote.setText(holder.tvTitleNote.getText());
                    holder.tvTypeSaveItemRecycleView.setText("");
                    holder.imgPreview.setScaleType(ImageView.ScaleType.CENTER);
                    Glide
                            .with(context)
                            .load(new File(pathImg))
                            .placeholder(R.drawable.placeholder)
                            .crossFade()
//                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
//                            .thumbnail(0.1f)
                            .override(200,300)
                            .into(holder.imgPreview);
                    break;
                case DatabaseManager.TYPE_GALLERY:
                    holder.tvTitleNote.setText(holder.tvTitleNote.getText());
                    holder.tvTypeSaveItemRecycleView.setText("");
                    holder.imgPreview.setScaleType(ImageView.ScaleType.CENTER);
                    Glide
                            .with(context)
                            .load(new File(pathImg))
                            .placeholder(R.drawable.placeholder)
                            .crossFade()
//                            .fitCenter()
                            .override(400,600)
                            .into(holder.imgPreview);
                    break;
                case DatabaseManager.TYPE_SCREEN_SHOT:
                    holder.tvTitleNote.setText(holder.tvTitleNote.getText());
                    holder.tvTypeSaveItemRecycleView.setText("");
                    holder.imgPreview.setScaleType(ImageView.ScaleType.CENTER);
                    Log.d(TAG, "onBindViewHolder: "+pathImg);
                    Glide
                            .with(context)
                            .load(new File(pathImg))
                            .placeholder(R.drawable.placeholder)
                            .crossFade()
//                            .fitCenter()
                            .override(400,600)
                            .into(holder.imgPreview);
                default:
                    break;
            }

            String gpsInfo = data.get(position).getLatlong();
            String wifiInfo = data.get(position).getWifiName();
            holder.tvGps.setText(gpsInfo);
            holder.tvWifi.setText(wifiInfo);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //Load ảnh sử dụng BitmapFacetory--Tốc độ chậm
    public Bitmap loadImage(String pathImageThumbnail) {
        try {
            InputStream inputStream = new FileInputStream(pathImageThumbnail);
            bitmapOri = BitmapFactory.decodeStream(inputStream);
//            Log.d(TAG,"getWidth: "+bitmapOri.getWidth());
//            Log.d(TAG,"getHeight: "+bitmapOri.getHeight());
//            float ratio = (THUMBSIZE_X*1F / bitmapOri.getWidth());
//            Log.d(TAG,"ratio: "+ratio);
//            bitmap = ThumbnailUtils.extractThumbnail(bitmapOri, THUMBSIZE_X, THUMBSIZE_X);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmapOri;
    }

    public ArrayList<NoteItem> getArrData() {
        return data;
    }

    public void swapDataUseCursor(Cursor cursor) {
        data.clear();
        data.addAll(cursorToArrayList(cursor));
        notifyDataSetChanged();
    }

    public void swapDataUseArray(ArrayList arrayList){
            data.clear();
            data.addAll(arrayList);
            notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView tvTitleNote, tvContentNote, tvTypeSaveItemRecycleView;
        public ImageView imgPreview;
        public TextView tvPlace, tvGps, tvWifi;
        public ImageButton cbCheck;

        public Holder(View itemView) {
            super(itemView);
            tvTypeSaveItemRecycleView = (TextView) itemView.findViewById(R.id.tv_type_save_item_view_recycle);
            tvTitleNote = (TextView) itemView.findViewById(R.id.tv_title_note);
            tvContentNote = (TextView) itemView.findViewById(R.id.tv_content_note);
            imgPreview = (ImageView) itemView.findViewById(R.id.img_preview);
            cbCheck = (ImageButton) itemView.findViewById(R.id.ib_check);
            tvPlace = (TextView) itemView.findViewById(R.id.tv_place_info);
            tvGps = (TextView) itemView.findViewById(R.id.tv_gps_info);
            tvWifi = (TextView) itemView.findViewById(R.id.tv_wifi_info);
        }
    }
}
