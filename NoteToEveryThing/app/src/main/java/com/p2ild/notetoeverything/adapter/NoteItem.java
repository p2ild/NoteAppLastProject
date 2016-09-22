package com.p2ild.notetoeverything.adapter;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by duypi on 9/16/2016.
 */
public class NoteItem implements Serializable{
    private String noteTitle,noteContent,pathImg,PathThumbnail,typeSave;

    public NoteItem(String noteTitle, String noteContent, String pathImg, String pathThumbnail, String typeSave) {
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        this.pathImg = pathImg;
        PathThumbnail = pathThumbnail;
        this.typeSave = typeSave;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getPathImg() {
        return pathImg;
    }

    public void setPathImg(String pathImg) {
        this.pathImg = pathImg;
    }

    public String getPathThumbnail() {
        return PathThumbnail;
    }

    public void setPathThumbnail(String pathThumbnail) {
        PathThumbnail = pathThumbnail;
    }

    public String getTypeSave() {
        return typeSave;
    }

    public void setTypeSave(String typeSave) {
        this.typeSave = typeSave;
    }
}
