package com.p2ild.notetoeverything.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by duypi on 9/23/2016.
 */
public class RecycleViewAdapterMap extends RecyclerView.Adapter<RecycleViewAdapterMap.MyViewHolder> {
    private static final String TAG = RecycleViewAdapterMap.class.getSimpleName();
    private final ArrayList<NoteItem> arrNote;
    private final Context context;
    private Random random = new Random();


    public RecycleViewAdapterMap(Context context, ArrayList<NoteItem> arrNote) {
        this.arrNote = arrNote;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_recycler_map, parent, false);
        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String pathImg = arrNote.get(position).getPathImg();

        switch (arrNote.get(position).getTypeSave()) {
            case DatabaseManager.TYPE_CLIP_BOARD:
                holder.tvTitle.setText(arrNote.get(position).getNoteTitle());
                holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                holder.imageView.setImageResource(R.drawable.placeholder_primary);
                break;
            case DatabaseManager.TYPE_TEXT_ONLY:
                holder.tvTitle.setText(arrNote.get(position).getNoteTitle());
                holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                holder.imageView.setImageResource(R.drawable.placeholder_primary);
                break;
            case DatabaseManager.TYPE_CAPTURE:
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide
                        .with(context)
                        .load(pathImg)
                        .placeholder(R.drawable.placeholder)
                        .crossFade()
                        .override(200,500)
                        .thumbnail(0.5f)
                        .into(holder.imageView);
                break;
            case DatabaseManager.TYPE_GALLERY:
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide
                        .with(context)
                        .load(pathImg)
                        .placeholder(R.drawable.placeholder)
                        .crossFade()
                        .override(200,500)
                        .thumbnail(0.5f)
                        .into(holder.imageView);
                break;
            case DatabaseManager.TYPE_SCREEN_SHOT:
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide
                        .with(context)
                        .load(pathImg)
                        .placeholder(R.drawable.placeholder)
                        .crossFade()
                        .override(200,500)
                        .into(holder.imageView);
                break;
            default:
                break;
        }
//        holder.imageView.setImageResource(R.drawable.placeholder);
    }

    @Override
    public int getItemCount() {
        return arrNote.size();
    }

    public ArrayList<NoteItem> getArrayNote() {
        return arrNote;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView tvTitle;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_preview_map);
            tvTitle = (TextView)itemView.findViewById(R.id.tv_title_map);
        }
    }

    public void itemRemove(int position){
        arrNote.remove(position);
        notifyItemRemoved(position);
        Log.d(TAG, "itemRemove: afterSwap: "+arrNote.size());
    }
}
