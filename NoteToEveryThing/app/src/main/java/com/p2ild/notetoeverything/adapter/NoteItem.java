package com.p2ild.notetoeverything.adapter;

import android.database.Cursor;

import com.p2ild.notetoeverything.DatabaseManager;

import java.io.Serializable;

/**
 * Created by duypi on 9/16/2016.
 */
public class NoteItem implements Serializable{

    private String noteTitle,noteContent,pathImg, pathThumbnail,typeSave,wifiName,latlong;

    public NoteItem(String noteTitle, String noteContent, String pathImg, String pathThumbnail, String typeSave,String latlong,String wifiName) {
        this.noteTitle       = noteTitle;
        this.noteContent     = noteContent;
        this.pathImg         = pathImg;
        this.pathThumbnail      = pathThumbnail;
        this.typeSave       = typeSave;
        this.latlong        = latlong;
        this.wifiName        = wifiName;
    }

    public NoteItem(Cursor cursor){
        this.noteTitle      = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_TITLE_NOTE));
        this.noteContent    = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_CONTENT_NOTE));
        this.pathImg        = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_PATH_IMAGE_NOTE));
        this.pathThumbnail  = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE));
        this.typeSave       = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_TYPE_SAVE));
        this.latlong        = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_LATLONG));
        this.wifiName          = cursor.getString(cursor.getColumnIndex(DatabaseManager.NAME_COLUMN_WIFI_NAME));
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public String getPathImg() {
        return pathImg;
    }

    public String getPathThumbnail() {
        return pathThumbnail;
    }

    public String getTypeSave() {
        return typeSave;
    }

    public String getLatlong() {
        return latlong;
    }

    public String getWifiName() {
        return wifiName;
    }
}
