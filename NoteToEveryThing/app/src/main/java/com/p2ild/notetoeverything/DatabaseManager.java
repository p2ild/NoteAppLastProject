package com.p2ild.notetoeverything;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by duypi on 8/20/2016.
 */
public class DatabaseManager extends SQLiteOpenHelper {

    public static final String NAME_COLUMN_TITLE_NOTE = "title_note";
    public static final String NAME_COLUMN_CONTENT_NOTE = "content_note";
    public static final String NAME_COLUMN_PATH_IMAGE_NOTE = "path_image_note";
    public static final String NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE = "path_thumbnail_image";

    public static final int COLUMN_TITLE_NOTE = 0;
    public static final int COLUMN_CONTENT_NOTE = 1;
    public static final int COLUMN_PATH_IMAGE_NOTE = 2;
    public static final int COLUMN_PATH_THUMBNAIL_IMAGE_NOTE = 3;

    private static final String DATABASE_NAME = "DB_NOTE";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "TB_NOTE";
    private static final String TAG = DatabaseManager.class.getSimpleName();
    private SQLiteDatabase db;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        db = this.getWritableDatabase();
//        dropTableIfExists();
        createTableIfNotExists();
    }


    public void backupAllNote() {
        File folderBackup = new File(Environment.getExternalStorageDirectory() + "/BackupNoteToEveryThing");
        if (!folderBackup.exists()) {
            folderBackup.mkdir();
        }

        backupDataBase(folderBackup.getPath());
        backupImageNote(folderBackup.getPath());
    }

    public void backupDataBase(String path) {
        File file = new File(Environment.getDataDirectory() + "/data/com.p2ild.notetoeverything/databases/DB_NOTE");

        try {
            FileInputStream input = new FileInputStream(file);
            FileOutputStream output = new FileOutputStream(path + "/" + DATABASE_NAME);

            byte[] b = new byte[1024];
            int length;
            while ((length = input.read(b)) != -1) {
                output.write(b, 0, length);
            }
            output.close();
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backupImageNote(String path) {
        File file = new File(Environment.getDataDirectory() + "/data/com.p2ild.notetoeverything/imageSave");

//        try {
        FileOutputStream output = null;
        FileInputStream input = null;
        for (File file1 : file.listFiles()) {
            Log.d(TAG, "backupImageNote: File:" + file1.getPath());
            try {
                input = new FileInputStream(file1);
                output = new FileOutputStream(path + "/" + file1.getName());
                byte[] b = new byte[1024];
                int length;
                while ((length = input.read(b)) != -1) {
                    output.write(b, 0, length);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        createTableIfNotExists();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    //Tạo và xóa table
    public void createTableIfNotExists() {
        String scripCreateTable = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + "("
                + NAME_COLUMN_TITLE_NOTE + " VARCHAR,"
                + NAME_COLUMN_CONTENT_NOTE + " VARCHAR,"
                + NAME_COLUMN_PATH_IMAGE_NOTE + " VARCHAR,"
                + NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE + " VARCHAR"
                + ")";
        db.execSQL(scripCreateTable);
    }

    private void dropTableIfExists() {
        String scripCreateTable = "DROP TABLE IF EXISTS "
                + TABLE_NAME;
        db.execSQL(scripCreateTable);
    }

    //Open-Close Database
    public void openDb() {
        if (db != null && !db.isOpen()) {
            db = SQLiteDatabase.openDatabase(db.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        }
    }

    public void closeDb() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public Cursor readAllData() {
        openDb();
        String sql = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);
        Log.d(TAG, "cursor.getCount= " + cursor.getCount());
        closeDb();
        return cursor;
    }

    public void insert(String titleNote, String contentNote, String imgPath, String imgThumbnailPath) {
        openDb();
        String sql = "INSERT INTO " + TABLE_NAME + " VALUES("
                + "'" + replaceCharApostrophe(titleNote) + "'" + ","
                + "'" + replaceCharApostrophe(contentNote) + "'" + ","
                + "'" + imgPath + "'" + ","
                + "'" + imgThumbnailPath + "'"
                + ")";
        db.execSQL(sql);
        closeDb();
    }

    public void update(String oldTitle, String titleNote, String contentNote) {
        openDb();
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + "" + NAME_COLUMN_TITLE_NOTE + " ='" + replaceCharApostrophe(titleNote) + "',"
                + "" + NAME_COLUMN_CONTENT_NOTE + " ='" + replaceCharApostrophe(contentNote) + "'"
                + " WHERE " + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(oldTitle) + "'";
        db.execSQL(sql);
        closeDb();
    }

    public void deleteNote(String noteTitleDelete) {
        openDb();
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(noteTitleDelete) + "'";
        db.execSQL(sql);
        closeDb();
    }

    private String replaceCharApostrophe(String str) {
        String result = str;
        Log.d(TAG, "replaceCharApostrophe: result:"+ result);
        if (str!=null && str.contains("'")) {
            str = str.replace("'", "''");
            Log.d(TAG, "replaceCharApostrophe: str"+str);
            return str;
        }else {
            return result;
        }
    }
}
