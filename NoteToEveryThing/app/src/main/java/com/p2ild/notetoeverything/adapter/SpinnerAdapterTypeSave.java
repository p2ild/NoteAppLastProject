package com.p2ild.notetoeverything.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.p2ild.notetoeverything.R;

import java.util.ArrayList;

/**
 * Created by duypi on 9/16/2016.
 */
public class SpinnerAdapterTypeSave extends ArrayAdapter {

    private final Context context;
    private final ArrayList<String> arrType;

    public SpinnerAdapterTypeSave(Context context,int layoutDrop, ArrayList<String> arrType) {
        super(context, layoutDrop);
        this.context = context;
        this.arrType = arrType;
    }

    @Override
    public int getCount() {
        return arrType.size();
    }

    @Override
    public String getItem(int i) {
        return arrType.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spinner_type_save, parent, false);
        ((TextView) convertView.findViewById(R.id.tv_type_save)).setText(arrType.get(position));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView) getView(position, convertView, parent).findViewById(R.id.tv_type_save);
        tv.setText(arrType.get(position));
        return super.getDropDownView(position, convertView, parent);
    }

}
