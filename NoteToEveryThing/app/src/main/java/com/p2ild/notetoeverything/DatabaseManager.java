package com.p2ild.notetoeverything;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String FOLDER_BACKUP_NAME = "BackupNoteToEveryThing";
    private static final int MSG_BACKUP = 111;
    private static final int MSG_RESTORE = 222;
    private static final int MSG_UPDATE_PERCENT_BACKUP = 333;
    private static final int MSG_UPDATE_PERCENT_RESTORE = 444;
    private static final String PATH_APP_INTERNAL = Environment.getDataDirectory() + "/data/com.p2ild.notetoeverything";
    private final Context context;
    private SQLiteDatabase db;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_PERCENT_BACKUP:
                    float percentBackup = (msg.arg1 * 100) / msg.arg2;
                    ((TextView) ((Activity) context).findViewById(R.id.tv_title_action_bar)).setText("Backup : " + (int) percentBackup + " %");
                    break;
                case MSG_UPDATE_PERCENT_RESTORE:
                    float percentRestore = (msg.arg1 * 100) / msg.arg2;
                    ((TextView) ((Activity) context).findViewById(R.id.tv_title_action_bar)).setText("Restore : " + (int) percentRestore + " %");
                    break;
                // TODO: 9/1/2016 Load sai tổng số note
                case MSG_BACKUP:
                    ((TextView) ((Activity) context).findViewById(R.id.tv_title_action_bar)).setText("ALL NOTE (" + (msg.arg1 / 2) + ")");
                    Toast.makeText(context, "Sao lưu dữ liệu hoàn tất", Toast.LENGTH_LONG).show();
                    break;
                case MSG_RESTORE:
                    ((TextView) ((Activity) context).findViewById(R.id.tv_title_action_bar)).setText("ALL NOTE (" + readAllData().getCount() + ")");
                    Toast.makeText(context, "Phục hồi dữ liệu hoàn tất", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        db = this.getWritableDatabase();
        this.context = context;
        createTableIfNotExists();
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

    /*Open-Close Database*/
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

    /*Đọc tất cả các dữ liệu. Gọi ở chỗ setAdapter của RecycleView trong Fragment Main*/
    public Cursor readAllData() {
        openDb();
        // TODO: 9/1/2016 Xóa table nên select từ table bị lỗi
        String sql = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);
        Log.d(TAG, "cursor.getCount= " + cursor.getCount());
        closeDb();
        return cursor;
    }

    /*Thêm note vào cơ sở dữ liệu*/
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

    /*Sửa note ờ cơ sở dữ liệu*/
    public void update(String oldTitle, String titleNote, String contentNote) {
        openDb();
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + "" + NAME_COLUMN_TITLE_NOTE + " ='" + replaceCharApostrophe(titleNote) + "',"
                + "" + NAME_COLUMN_CONTENT_NOTE + " ='" + replaceCharApostrophe(contentNote) + "'"
                + " WHERE " + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(oldTitle) + "'";
        db.execSQL(sql);
        closeDb();
    }

    /*Xóa note ờ cơ sở dữ liệu*/
    public void deleteNote(String noteTitleDelete) {
        openDb();
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(noteTitleDelete) + "'";
        db.execSQL(sql);
        closeDb();
    }

    /*Sửa tất cả các ký tự đặc biệt để khi thêm vào cơ sở dữ liệu k gây lỗi*/
    private String replaceCharApostrophe(String str) {
        String result = str;
        Log.d(TAG, "replaceCharApostrophe: result:" + result);
        if (str != null && str.contains("'")) {
            str = str.replace("'", "''");
            Log.d(TAG, "replaceCharApostrophe: str" + str);
            return str;
        } else {
            return result;
        }
    }

    /*Backup tất cả note ra External Storage*/
    public void backupAllNote() {
        Toast.makeText(context, "Bắt đầu quá trình sao lưu database. Sẽ có thông báo khi quá trình hoàn tất", Toast.LENGTH_LONG).show();
        //Trỏ tới folder Backup ở External Storage được tạo mới nếu chưa tồn tại
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                File folderBackup = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_BACKUP_NAME);
                if (!folderBackup.exists()) {
                    folderBackup.mkdir();
//            MediaScannerConnection.scanFile(context, new String[]{folderBackup.getPath()}, null, null);
                }
                backupDataBase(folderBackup.getPath());
                backupImageNote(folderBackup.getPath());
            }
        });
        thread.start();
    }

    /*Back up file database*/
    public void backupDataBase(String path) {
        //Trỏ tới file db ở Internal Storage
        File fileIn = new File(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME);
        try {
            FileInputStream input = new FileInputStream(fileIn);
            FileOutputStream output = new FileOutputStream((path + "/" + DATABASE_NAME));
            byte[] b = new byte[1024];
            int length;
            while ((length = input.read(b)) != -1) {
                output.write(b, 0, length);
            }
            output.close();
            input.close();
//            MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Back up tất cả các ảnh và thumbnail của nó*/
    public void backupImageNote(String path) {
        File fileSource = new File(Environment.getDataDirectory() + "/data/com.p2ild.notetoeverything/imageSave");
        File[] listFile = fileSource.listFiles();
        int percent = 0;
        for (File pointer : fileSource.listFiles()) {
            try {
                Message messagePercentBackup = new Message();
                messagePercentBackup.what = MSG_UPDATE_PERCENT_BACKUP;
                messagePercentBackup.arg1 = percent;
                messagePercentBackup.arg2 = fileSource.listFiles().length;
                handler.sendMessage(messagePercentBackup);
                percent++;
                if (pointer.getName().equals(listFile[listFile.length - 1].getName())) {
                    Message messageBackup = new Message();
                    messageBackup.what = MSG_BACKUP;
                    messageBackup.arg1 = listFile.length;
                    handler.sendMessage(messageBackup);
                }
                FileInputStream input = new FileInputStream(pointer.getPath());
                File fileOut = new File(path + "/" + pointer.getName());
                FileOutputStream output = new FileOutputStream(fileOut);
                byte[] b = new byte[1024];
                int length;
                while ((length = input.read(b)) != -1) {
                    output.write(b, 0, length);
                }
                input.close();
                output.close();
//                MediaScannerConnection.scanFile(context, new String[]{file1.getPath()}, null, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*Restore toàn bộ note*/
    public void restoreAllNote() {
        //Trỏ tới folder backup ở External Storage
        final File folderBackup = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_BACKUP_NAME);
        //Nếu folder backup không tồn tại đưa ra thông báo
        if (!folderBackup.exists()) {
            Toast.makeText(context, "Folder backup không tồn tại", Toast.LENGTH_LONG).show();
        }
        //Tồn tại thì tiếp tục đọc dữ liệu để ghi vào Internal Storage
        else {
            Toast.makeText(context, "Bắt đầu quá trình phục hồi database. Sẽ có thông báo khi quá trình hoàn tất", Toast.LENGTH_LONG).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    restore(folderBackup.getPath());
                }
            }).start();
        }
    }

    private void restore(final String path) {
        try {
            File fileSource = new File(path);
            int percent = 0;

            //Sao lưu file database vào bộ nhớ
            FileInputStream fileIn = new FileInputStream(path + "/" + DATABASE_NAME);
            FileOutputStream fileOut = new FileOutputStream(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME);
            byte[] b = new byte[1024];
            int length;
            while ((length = fileIn.read(b)) != -1) {
                fileOut.write(b, 0, length);
            }
            percent++;
            fileIn.close();
            fileOut.close();

            //Sao lưu file ảnh vào bộ nhớ
            for (File pointer : fileSource.listFiles()) {
                if (pointer.getName().equals(fileSource.listFiles()[fileSource.listFiles().length - 1].getName())) {
                    handler.sendEmptyMessage(MSG_RESTORE);
                }
                Message message = new Message();
                message.what = MSG_UPDATE_PERCENT_RESTORE;
                message.arg1 = percent;
                message.arg2 = fileSource.listFiles().length - 1;
                handler.sendMessage(message);
                percent++;
                FileInputStream fileInputStream = new FileInputStream(pointer.getPath());
                if (!pointer.getName().equals(DATABASE_NAME)) {
                    File fileImgOut = new File(PATH_APP_INTERNAL + "/imageSave/" + pointer.getName());
                    FileOutputStream fops = new FileOutputStream(fileImgOut);
                    byte[] bImg = new byte[1024];
                    int lengthImg;
                    while ((lengthImg = fileInputStream.read(bImg)) != -1) {
                        fops.write(bImg, 0, lengthImg);
                    }
                    fileInputStream.close();
                    fops.close();
                }
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "restore: lỗi: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "restore: lỗi: " + e);
            e.printStackTrace();
        }
    }

    public void delTable() {
        openDb();
        dropTableIfExists();
        closeDb();
    }
}
