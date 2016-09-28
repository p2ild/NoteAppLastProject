package com.p2ild.notetoeverything;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.p2ild.notetoeverything.service.AppService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by duypi on 8/20/2016.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    // TODO: 9/28/2016 Chuyen sang dung realm
    //SQLiteOpenHelper, ormlite, is sqlite
    //tuy ban
    //cai nao tien thi dung
    //du lieu minh nho, thi toc do may cai minh kho phan biet
    //lon thi moi de phan biet.Ok a e hieu r :D
    //The thang realm kia k phai sqlite  gra
    public static final String NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE = "path_thumbnail_image";
    public static final String NAME_COLUMN_TITLE_NOTE = "title_note";
    public static final String NAME_COLUMN_CONTENT_NOTE = "content_note";
    public static final String NAME_COLUMN_PATH_IMAGE_NOTE = "path_image_note";
    public static final String NAME_COLUMN_TYPE_SAVE = "type_save";
    public static final String NAME_COLUMN_LATLONG = "lat_long";
    public static final int COLUMN_TITLE_NOTE = 0;
    public static final int COLUMN_CONTENT_NOTE = 1;
    public static final int COLUMN_PATH_IMAGE_NOTE = 2;
    public static final int COLUMN_PATH_THUMBNAIL_IMAGE_NOTE = 3;
    public static final int COLUMN_TYPE_SAVE = 4;
    public static final int COLUMN_LATLONG = 5;

    public static final String TYPE_SCREEN_SHOT = "Screenshot";
    public static final String TYPE_GALLERY = "Gallery";
    public static final String TYPE_CLIP_BOARD = "Clipboard";
    public static final String TYPE_CAPTURE = "Capture";

    public static final String PATH_APP_INTERNAL = Environment.getDataDirectory() + "/data/com.p2ild.notetoeverything";
    public static final int MSG_BACKUP_COMPLETE = 111;
    public static final int MSG_RESTORE_COMPLETE = 222;
    public static final int MSG_DELETE_COMPLETE = 333;
    public static final int MSG_UPDATE_PERCENT_BACKUP = 444;
    public static final int MSG_UPDATE_PERCENT_RESTORE = 555;
    public static final int MSG_UPDATE_PERCENT_DELETE = 666;
    private static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    private static final String TAG = DatabaseManager.class.getSimpleName();
    private static final String DATABASE_NAME = "DB_NOTE";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "TB_NOTE";
    private static final String FOLDER_BACKUP_NAME = "BackupNoteToEveryThing";
    private static final String SWITCH_SCREENSHOT = "SWITCH_SCREENSHOT";

    private final Context context;
    private final EventBus eventBus;

    private SharedPreferences sharedPreferences;

    private SQLiteDatabase db;

    public DatabaseManager(Context context, EventBus eventBus) {
        super(context, DATABASE_NAME, null, VERSION);
        db = getWritableDatabase();
        this.eventBus = eventBus;
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARE_PREFERENCE, Context.MODE_PRIVATE);

        createDefaultFolderInternal();
        createTableIfNotExists();
    }


    public static void copyFile(File fileIn, File fileOut,boolean isNewFile) {
        try {
            Log.d(TAG, "copyFile: path file in :::: "+fileIn.getPath());
            if(!fileOut.exists() && !isNewFile){
                FileInputStream inputDb = new FileInputStream(fileIn);
                FileOutputStream outputDb = new FileOutputStream(fileOut);
                byte[] b = new byte[1024];
                int length;
                Log.d(TAG, "copyFile: fileOut" + fileOut.getName());
                while ((length = inputDb.read(b)) != -1) {
                    outputDb.write(b, 0, length);
                }
                inputDb.close();
                outputDb.close();
                return;
            }else {
                Log.d(TAG, "copyFile: Bỏ qua. File tồn tại");
            }

            if (isNewFile){
                FileInputStream inputDb = new FileInputStream(fileIn);
                FileOutputStream outputDb = new FileOutputStream(fileOut);
                byte[] b = new byte[1024];
                int length;
                while ((length = inputDb.read(b)) != -1) {
                    outputDb.write(b, 0, length);
                }
                inputDb.close();
                outputDb.close();
                return;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
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
                + NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE + " VARCHAR,"
                + NAME_COLUMN_TYPE_SAVE + " VARCHAR,"
                + NAME_COLUMN_LATLONG + " VARCHAR"
                + ")";
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
    public Cursor readAllData(String type) {
        openDb();
        // TODO: 9/1/2016 ---DONE--- Xóa table nên select từ table bị lỗi
        String sql = "";
        if (type.equals("All")) {
            sql = "SELECT " + "*" + " FROM " + TABLE_NAME;
        } else {
            sql = "SELECT " + "*" + " FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN_TYPE_SAVE + "='" + type + "'";
        }

        Cursor cursor = db.rawQuery(sql, null);
        Log.d(TAG, "readAllData: cursor count : "+cursor.getCount());
        closeDb();
        return cursor;
    }

    /*Thêm note vào cơ sở dữ liệu*/
    public void insert(String titleNote, String contentNote, String imgPath, String imgThumbnailPath, String typeSave,String latLong) {
        openDb();
        String sql = "INSERT INTO " + TABLE_NAME + " VALUES("
                + "'" + replaceCharApostrophe(titleNote) + "'" + ","
                + "'" + replaceCharApostrophe(contentNote) + "'" + ","
                + "'" + imgPath + "'" + ","
                + "'" + imgThumbnailPath + "'" + ","
                + "'" + typeSave + "'"+ ","
                + "'" + latLong + "'"
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
    public void deleteNote(String noteTitleDelete, String noteContentDelete, String noteImg, String noteThumbnail, String typeSave) {
        openDb();

        //del row
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE "
                + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(noteTitleDelete) + "' AND "
                + NAME_COLUMN_CONTENT_NOTE + "='" + replaceCharApostrophe(noteContentDelete) + "' AND "
                + NAME_COLUMN_PATH_IMAGE_NOTE + "='" + replaceCharApostrophe(noteImg) + "' AND "
                + NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE + "='" + replaceCharApostrophe(noteThumbnail) + "' AND "
                + NAME_COLUMN_TYPE_SAVE + "='" + replaceCharApostrophe(typeSave) + "'";
        db.execSQL(sql);

        //del img
        File img = new File(noteImg);
        if (img.exists()) {
            Log.d(TAG, "deleteNote: del IMG thành công: "+noteImg);
            img.delete();
        } else {
            Log.d(TAG, "IMG fail : File không tồn tại");
        }

        File thumbnail = new File(noteThumbnail);
        if (thumbnail.exists()) {
            thumbnail.delete();
            Log.d(TAG, "deleteNote: del THUMBNAIL thành công: "+noteThumbnail);
        } else {
            Log.d(TAG, "THUMBNAIL fail : file không tồn tại");
        }
        closeDb();
    }

    /*Sửa tất cả các ký tự đặc biệt để khi thêm vào cơ sở dữ liệu k gây lỗi*/
    public String replaceCharApostrophe(String str) {
        String result = str;
        if (str != null && str.contains("'")) {
            str = str.replace("'", "''");
            return str;
        } else {
            return result;
        }
    }

    /*Backup tất cả note ra External Storage*/
    public void backupAllNote(final Service service) {
        Toast.makeText(context, "Bắt đầu quá trình sao lưu database. Sẽ có thông báo khi quá trình hoàn tất", Toast.LENGTH_LONG).show();
        AsyncTask<Void, Integer, Void> asyncTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                /* Nếu file ở External chưa tồn tại thì tạo
                * */
                createDefaultFolderExternal();

                File folderBackup = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_BACKUP_NAME);
                File folderImageSave = new File(folderBackup.getPath() + "/imageSave");

                /*Backup file database*/
                File fileDbIn = new File(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME);
                File fileDbOut = new File((folderBackup.getPath() + "/" + DATABASE_NAME));
                copyFile(fileDbIn, fileDbOut,true);

                /*Back up tất cả các ảnh và thumbnail của nó
                * */
                Cursor cursor = readAllData("All");//Load cursor chỉ copy những ảnh nào note còn tồn tại trong database
                cursor.moveToFirst();
                int percent = 0;
                while (!cursor.isAfterLast()) {
                    /*Gửi message update textview trạng thái % */
                    publishProgress(MSG_UPDATE_PERCENT_BACKUP, percent, cursor.getCount());
                    //Copy file ảnh gốc
                    File fileImgIn = new File(cursor.getString(COLUMN_PATH_IMAGE_NOTE));//Đầu vào là file lấy từ cursor đang trỏ tới
                    File fileImgOut = new File(folderImageSave.getPath() + "/" + fileImgIn.getName());///Đầu ra là file trong thư mục ở External
                    copyFile(fileImgIn, fileImgOut,false);

                    //Copy file thumbnail
                    File fileThumbnailIn = new File(cursor.getString(COLUMN_PATH_THUMBNAIL_IMAGE_NOTE));//Đầu vào là file lấy từ cursor đang trỏ tới
                    File fileThumbnailOut = new File(folderImageSave.getPath() + "/" + fileThumbnailIn.getName());//Đầu ra là file trong thư mục ở External
                    copyFile(fileThumbnailIn, fileThumbnailOut,false);
//                MediaScannerConnection.scanFile(context, new String[]{file1.getPath()}, null, null);
                    if (cursor.getPosition() == (cursor.getCount() - 1)) {
                        /*Gửi message update textview trạng thái % */
                        publishProgress(MSG_BACKUP_COMPLETE, cursor.getCount());
                    }
                    cursor.moveToNext();
                    percent++;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                int state = values[0];
                switch (state) {
                    case MSG_UPDATE_PERCENT_BACKUP:
                        int percentBackup = (values[1] * 100) / values[2];
                        Integer[] msgBackup = {MSG_UPDATE_PERCENT_BACKUP, percentBackup};
                        eventBus.post(msgBackup);
                        break;
                    case MSG_BACKUP_COMPLETE:
                        Integer[] msgBackupComplete = {MSG_BACKUP_COMPLETE, values[1]};
                        eventBus.post(msgBackupComplete);
                        Toast.makeText(context, "Sao lưu dữ liệu hoàn tất", Toast.LENGTH_LONG).show();
                        if (sharedPreferences.getString(SWITCH_SCREENSHOT, "null").equals("off")) {
                            service.stopSelf();
                        } else {

                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        };
        asyncTask.execute();
    }

    /*Restore toàn bộ note*/
    public void restoreAllNote(final Service appService) {
        //Trỏ tới folder backup ở External Storage
        final File folderBackup = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_BACKUP_NAME);
        //Nếu folder backup không tồn tại đưa ra thông báo
        if (!folderBackup.exists()) {
            Toast.makeText(context, "Không có bản backup không tồn tại", Toast.LENGTH_LONG).show();
        }
        //Tồn tại thì tiếp tục đọc dữ liệu để ghi vào Internal Storage
        else {
            Toast.makeText(context, "Bắt đầu quá trình phục hồi database. Sẽ có thông báo khi quá trình hoàn tất", Toast.LENGTH_LONG).show();
            AsyncTask<Void, Integer, Void> asyncTask = new AsyncTask<Void, Integer, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    File folderImageSave = new File(folderBackup.getPath() + "/imageSave");
                    int percent = 0;

                    //Phục hồi file database vào bộ nhớ internal
                    File fileDbIn = new File(folderBackup.getPath() + "/" + DATABASE_NAME);
                    File fileDbOut = new File(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME);
                    syncDatabaseFile(fileDbIn, fileDbOut);

                    //Phục hồi file ảnh vào bộ nhớ
                    for (File pointer : folderImageSave.listFiles()) {

                        publishProgress(MSG_UPDATE_PERCENT_RESTORE, percent, folderImageSave.listFiles().length);
                        File imgIn = new File(folderImageSave.getPath() + "/" + pointer.getName());
                        File imgOut = new File(PATH_APP_INTERNAL + "/imageSave/" + pointer.getName());
                        copyFile(imgIn, imgOut,false);
                        if (pointer.getName().equals(folderImageSave.listFiles()[folderImageSave.listFiles().length - 1].getName())) {
                            publishProgress(MSG_RESTORE_COMPLETE, readAllData("All").getCount());
                        }
                        percent++;
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    super.onProgressUpdate(values);
                    int state = values[0];
                    switch (state) {
                        case MSG_UPDATE_PERCENT_RESTORE:
                            int percentRestore = (values[1] * 100) / values[2];
                            Integer[] msgRestorePercent = {MSG_UPDATE_PERCENT_RESTORE, percentRestore};
                            eventBus.post(msgRestorePercent);
                            break;
                        case MSG_RESTORE_COMPLETE:
                            Integer[] msgRestoreComplete = {MSG_RESTORE_COMPLETE, values[1]};
                            eventBus.post(msgRestoreComplete);
//                            ((TextView) ((Activity) context).findViewById(R.id.tv_title_action_bar)).setText("ALL NOTE (" + readAllData().getCount() + ")");
                            Toast.makeText(context, "Phục hồi dữ liệu hoàn tất", Toast.LENGTH_LONG).show();
                            if (sharedPreferences.getString(SWITCH_SCREENSHOT, "").equals("false")) {
                                appService.stopSelf();
                            }
                            break;
                        default:
                            break;
                    }
                }
            };
            asyncTask.execute();
        }
    }

    private void syncDatabaseFile(File fileDbIn, File fileDbOut) {
        if (!fileDbOut.exists()) {
            copyFile(fileDbIn, fileDbOut,true);
        } else {
            SQLiteDatabase inputFileDb = null;
            try{
                inputFileDb = getReadableDatabase();
                inputFileDb = SQLiteDatabase.openDatabase(fileDbIn.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            }catch (SQLiteCantOpenDatabaseException e){
                // TODO: 9/20/2016 ---Done--- Hỏi quyền từ người sử dụng
                AsyncTask asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        publishProgress();
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Object[] values) {
                        Toast.makeText(context, "Vui lòng cấp phép permision trong cài đặt app cho ứng dụng", Toast.LENGTH_SHORT).show();
                        super.onProgressUpdate(values);
                    }
                };
                asyncTask.execute();
                Intent i = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(i);
            }
            String readAll = "SELECT * FROM " + TABLE_NAME;
            Cursor inputCursor = inputFileDb.rawQuery(readAll, null);
            int i = 0;
            Cursor cursorCheckExists = null;
            for (inputCursor.moveToFirst(); !inputCursor.isAfterLast(); inputCursor.moveToNext()) {
                String noteTitleReplace = inputCursor.getString(DatabaseManager.COLUMN_TITLE_NOTE);
                String noteContentReplace = inputCursor.getString(DatabaseManager.COLUMN_CONTENT_NOTE);
                String noteImgReplace = inputCursor.getString(DatabaseManager.COLUMN_PATH_IMAGE_NOTE);
                String noteThumbnailReplace = inputCursor.getString(DatabaseManager.COLUMN_PATH_THUMBNAIL_IMAGE_NOTE);
                String typeSaveReplace = inputCursor.getString(DatabaseManager.COLUMN_TYPE_SAVE);
                String typeSaveLatlong = inputCursor.getString(DatabaseManager.COLUMN_LATLONG);
                String sqlCheckExit =
                        "SELECT 1 FROM " + TABLE_NAME + " WHERE "
                                + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(noteTitleReplace) + "' AND "
                                + NAME_COLUMN_CONTENT_NOTE + "='" + replaceCharApostrophe(noteContentReplace) + "' AND "
                                + NAME_COLUMN_PATH_IMAGE_NOTE + "='" + replaceCharApostrophe(noteImgReplace) + "' AND "
                                + NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE + "='" + replaceCharApostrophe(noteThumbnailReplace) + "' AND "
                                + NAME_COLUMN_TYPE_SAVE + "='" + replaceCharApostrophe(typeSaveReplace) + "'";
                i++;
                openDb();
                cursorCheckExists = db.rawQuery(sqlCheckExit, null);
                if (cursorCheckExists.getCount() == 0) {
                    Log.d(TAG, "syncDatabaseFile: CHƯA TỒN TẠI");
                    // TODO: 9/28/2016 Trùng quá nhiều '
                    insert(replaceCharApostrophe(noteTitleReplace), replaceCharApostrophe(noteContentReplace), replaceCharApostrophe(noteImgReplace), replaceCharApostrophe(noteThumbnailReplace), replaceCharApostrophe(typeSaveReplace),replaceCharApostrophe(typeSaveLatlong));
                } else {
                    Log.d(TAG, "syncDatabaseFile: " + i + " tồn tại rồi");
                }
            }
            if (inputFileDb.isOpen()) {
                inputFileDb.close();
            }
        }
    }

    public void delAllDataTable(AppService appService) {
        for (File file : new File(PATH_APP_INTERNAL + "/imageSave").listFiles()) {
            Log.d(TAG, "delAllDataTable: FILE: " + file.getPath());
        }
        openDb();
        Cursor cursor = readAllData("All");
        cursor.moveToFirst();
        int percent = 0;
        Message messagePercentBackup = new Message();
        messagePercentBackup.what = MSG_UPDATE_PERCENT_DELETE;
        messagePercentBackup.arg1 = percent;
        messagePercentBackup.arg2 = cursor.getColumnCount() - 1;

        while (!cursor.isAfterLast()) {
            String noteTitleDel = cursor.getString(DatabaseManager.COLUMN_TITLE_NOTE);
            String noteContentDel = cursor.getString(DatabaseManager.COLUMN_CONTENT_NOTE);
            String noteImg = cursor.getString(DatabaseManager.COLUMN_PATH_IMAGE_NOTE);
            String noteThumbnail = cursor.getString(DatabaseManager.COLUMN_PATH_THUMBNAIL_IMAGE_NOTE);
            String typeSave = cursor.getString(DatabaseManager.COLUMN_TYPE_SAVE);

            deleteNote(noteTitleDel, noteContentDel, noteImg, noteThumbnail, typeSave);

            cursor.moveToNext();
            percent++;
        }
        closeDb();
    }

    public void createDefaultFolderInternal() {
        //Create folder imgSave
        File dirImage = new File(PATH_APP_INTERNAL + "/imageSave");
        if (!dirImage.exists()) {
            dirImage.mkdir();
        }
    }

    public void createDefaultFolderExternal() {
        File folderBackup = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_BACKUP_NAME);
        if (!folderBackup.exists()) {
            folderBackup.mkdir();
        }

        File folderImageSave = new File(folderBackup.getPath() + "/imageSave");
        if (!folderImageSave.exists()) {
            folderImageSave.mkdir();
        }
    }
}