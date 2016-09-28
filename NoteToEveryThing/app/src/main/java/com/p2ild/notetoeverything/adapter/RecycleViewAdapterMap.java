package com.p2ild.notetoeverything.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.p2ild.notetoeverything.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by duypi on 9/23/2016.
 */
public class RecycleViewAdapterMap extends RecyclerView.Adapter<RecycleViewAdapterMap.MyViewHolder> {
    private static final String TAG = RecycleViewAdapterMap.class.getSimpleName();
    private final ArrayList<NoteItem> arrNote;
    private final Context context;


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
        Log.d(TAG, "onBindViewHolder: "+arrNote.get(position).getPathImg());
        Glide.with(context)
                .load(new File(arrNote.get(position).getPathImg()))
                .placeholder(R.drawable.placeholder)
                .thumbnail(0.1f)
                .priority(Priority.IMMEDIATE)
                .into(holder.imageView);
//        holder.imageView.setImageResource(R.drawable.placeholder);
    }

    @Override
    public int getItemCount() {
        return arrNote.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_preview_map);
        }
    }
}
