package com.p2ild.notetoeverything;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.adapter.RecycleNoteAdapter;
import com.p2ild.notetoeverything.locationmanager.WifiGpsManager;
import com.p2ild.notetoeverything.service.AppService;
import com.p2ild.notetoeverything.service.NotificationService;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by duypi on 8/20/2016.
 */
public class DatabaseManagerCopyDb implements Serializable {
    // TODO: 9/28/2016 Chuyen sang dung realm
    //SQLiteOpenHelper, ormlite, is sqlite
    //tuy ban
    //cai nao tien thi dung
    //du lieu minh nho, thi toc do may cai minh kho phan biet
    //lon thi moi de phan biet.Ok a e hieu r :D
    //The thang realm kia k phai sqlite  gra
    public static final String UPDATE_DATABASE = "UPDATE_DATABASE";
    public static final String NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE = "path_thumbnail_image";
    public static final String NAME_COLUMN_TITLE_NOTE = "title_note";
    public static final String NAME_COLUMN_CONTENT_NOTE = "content_note";
    public static final String NAME_COLUMN_PATH_IMAGE_NOTE = "path_image_note";
    public static final String NAME_COLUMN_TYPE_SAVE = "type_save";
    public static final String NAME_COLUMN_LATLONG = "lat_long";
    public static final String NAME_COLUMN_ALARM = "alarm";
    public static final String NAME_COLUMN_WIFI_NAME = "wifi_name";

    public static final int COLUMN_TITLE_NOTE = 0;
    public static final int COLUMN_CONTENT_NOTE = 1;
    public static final int COLUMN_PATH_IMAGE_NOTE = 2;
    public static final int COLUMN_PATH_THUMBNAIL_IMAGE_NOTE = 3;
    public static final int COLUMN_TYPE_SAVE = 4;
    public static final int COLUMN_LATLONG = 5;
    public static final int COLUMN_ALARM = 6;
    public static final int COLUMN_WIFI_NAME = 7;

    public static final String TYPE_SCREEN_SHOT = "Screenshot";
    public static final String TYPE_GALLERY = "Gallery";
    public static final String TYPE_CLIP_BOARD = "Clipboard";
    public static final String TYPE_CAPTURE = "Capture";
    public static final String TYPE_TEXT_ONLY = "TextOnly";

    public static final String PATH_APP_INTERNAL = Environment.getDataDirectory() + "/data/com.p2ild.notetoeverything";
    public static final int MSG_BACKUP_COMPLETE = 111;
    public static final int MSG_RESTORE_COMPLETE = 222;
    public static final int MSG_DELETE_COMPLETE = 333;
    public static final int MSG_UPDATE_PERCENT_BACKUP = 444;
    public static final int MSG_UPDATE_PERCENT_RESTORE = 555;
    public static final int MSG_UPDATE_PERCENT_DELETE = 666;
    private static final String SHARE_PREFERENCE = "SHARE_PREFERENCE";
    private static final String TAG = DatabaseManagerCopyDb.class.getSimpleName();
    private static final String DATABASE_NAME = "DB_NOTE";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "TB_NOTE";
    private static final String FOLDER_BACKUP_NAME = "BackupNoteToEveryThing";
    private static final String FOLDER_EXPORT_IMAGE = "ImageExport";
    private static final String SWITCH_SCREENSHOT = "SWITCH_SCREENSHOT";
    public static final String TYPE_WIFI_AVAILABLE = "note in wifi  available";

    private final Context context;
    private SharedPreferences sharedPreferences;

    private SQLiteDatabase db;
    private NotificationManager notificationManager;

    public DatabaseManagerCopyDb(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARE_PREFERENCE, Context.MODE_PRIVATE);

        createDefaultFolderInternal();

//        createTableIfNotExists();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void copyFile(File fileIn, File fileOut, boolean isNewFile) {
        try {
            Log.d(TAG, "copyFile: path file in :::: " + fileIn.getPath());
            if (!fileOut.exists() && !isNewFile) {
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
            } else {
                Log.d(TAG, "copyFile: Bỏ qua. File tồn tại");
            }

            if (isNewFile) {
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

    public void copyAssetDbToInternal() {
        try {
            DataInputStream input = new DataInputStream(
                    context.getAssets().open(DATABASE_NAME));

            DataOutputStream output = new DataOutputStream(
                    new FileOutputStream(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME));

            byte[] b = new byte[1024];
            int length;
            while ((length = input.read(b)) != -1) {
                Log.d(TAG, "copyDatabases: WRITING");
                output.write(b, 0, length);
            }
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (db == null || !db.isOpen()) {
            db = SQLiteDatabase.openDatabase(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        } else {
        }
    }

    public void closeDb() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }


    public void updateWifiName(String sql) {
        sql = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null, null);
        cursor.moveToLast();
        Random random = new Random();
        String[] wifi = {"\"W32.Spybot.Worm.Test.Router\"", "\"TP-LINK_2FE960\""};
        for (int i = cursor.getCount() - 1; i > 0; i--) {
            String noteTitleDelete = cursor.getString(cursor.getColumnIndex(NAME_COLUMN_TITLE_NOTE));
            Log.d(TAG, "updateWifiName: noteTitle: " + noteTitleDelete);
            String noteContentDelete = cursor.getString(cursor.getColumnIndex(NAME_COLUMN_CONTENT_NOTE));
            String noteImg = cursor.getString(cursor.getColumnIndex(NAME_COLUMN_PATH_IMAGE_NOTE));
            String noteThumbnail = cursor.getString(cursor.getColumnIndex(NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE));
            String typeSave = cursor.getString(cursor.getColumnIndex(NAME_COLUMN_TYPE_SAVE));
            String latlong = cursor.getString(cursor.getColumnIndex(NAME_COLUMN_LATLONG));
            String alarm = cursor.getString(cursor.getColumnIndex(NAME_COLUMN_ALARM));
            sql = "UPDATE " + TABLE_NAME + " SET "
                    + "" + NAME_COLUMN_WIFI_NAME + " ='" + (wifi[random.nextInt(2)]) + "'" +
                    " WHERE "
                    + (NAME_COLUMN_TITLE_NOTE) + "='" + replaceCharApostrophe(noteTitleDelete) + "' AND "
                    + (NAME_COLUMN_CONTENT_NOTE) + "='" + replaceCharApostrophe(noteContentDelete) + "' AND "
                    + (NAME_COLUMN_PATH_IMAGE_NOTE) + "='" + (noteImg) + "' AND "
                    + (NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE) + "='" + (noteThumbnail) + "' AND "
                    + (NAME_COLUMN_TYPE_SAVE) + "='" + (typeSave) + "' AND "
                    + (NAME_COLUMN_LATLONG) + "='" + (latlong) + "' AND "
                    + (NAME_COLUMN_ALARM) + "='" + (alarm) + "'";
            db.execSQL(sql);
            cursor.moveToPrevious();
        }
    }

    /*Đọc tất cả các dữ liệu. Gọi ở chỗ setAdapter của RecycleView trong Fragment Main*/
    public Cursor readAllDataWithColumnTypeSave(String type) {
        openDb();
        // TODO: 9/1/2016 ---DONE--- Xóa table nên select từ table bị lỗi
        String sql = "";
//        updateWifiName(sql);
        if (type.equals("All")) {
            sql = "SELECT " + "*" + " FROM " + TABLE_NAME;
        } else {
            sql = "SELECT " + "*" + " FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN_TYPE_SAVE + "='" + type + "'";
        }

        Cursor cursor = db.rawQuery(sql, null);
        /** Dòng log này cấm xóa
         * Không hiểu tại sao nhưng nếu bỏ dòng log này đi chương trình sẽ lỗi
         **/
        Log.d(TAG, "readAllDataWithColumnTypeSave: cursorCheck"+ cursor.getCount());
        closeDb();
        return cursor;
    }

    public Cursor readAllDataWithColumnWifiName(String wifiName) {
        openDb();
        // TODO: 9/1/2016 ---DONE--- Xóa table nên select từ table bị lỗi
        String sql = "";
//        updateWifiName(sql);
            sql = "SELECT " + "*" + " FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN_WIFI_NAME + "='" + wifiName + "'";

        Cursor cursor = db.rawQuery(sql, null);
        /** Dòng log này cấm xóa
         * Không hiểu tại sao nhưng nếu bỏ dòng log này đi chương trình sẽ lỗi
         **/
        Log.d(TAG, "readAllDataWithColumnTypeSave: cursorCheck"+ cursor.getCount());
        closeDb();
        return cursor;
    }

    /*Thêm note vào cơ sở dữ liệu*/
    public void insert(String titleNote, String contentNote, String imgPath, String imgThumbnailPath, String typeSave, String latLong, String alarm, String wifiName, Boolean detectWifi) {
        openDb();
        String sql;
        String wifi_detected = "";
        if (detectWifi) {
            wifi_detected = WifiGpsManager.getSSIDWifi(context);
        } else {
            wifi_detected = wifiName;
        }
        ContentValues values = new ContentValues();
        values.put(NAME_COLUMN_TITLE_NOTE, titleNote);
        values.put(NAME_COLUMN_CONTENT_NOTE, contentNote);
        values.put(NAME_COLUMN_PATH_IMAGE_NOTE, imgPath);
        values.put(NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE, imgThumbnailPath);
        values.put(NAME_COLUMN_TYPE_SAVE, typeSave);
        values.put(NAME_COLUMN_LATLONG, latLong);
        values.put(NAME_COLUMN_ALARM, alarm);
        values.put(NAME_COLUMN_WIFI_NAME, wifi_detected);
        db.insert(TABLE_NAME, null, values);

        sharedPreferences.edit().putString(UPDATE_DATABASE, UPDATE_DATABASE).apply();
//        db.execSQL(sql);
        closeDb();
    }

    /*Sửa 1 note ờ cơ sở dữ liệu*/
    public void update(String oldTitle, String titleNote, String contentNote) {
        openDb();
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + "" + NAME_COLUMN_TITLE_NOTE + " ='" + replaceCharApostrophe(titleNote) + "',"
                + "" + NAME_COLUMN_CONTENT_NOTE + " ='" + replaceCharApostrophe(contentNote) + "'"
                + " WHERE " + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(oldTitle) + "'";
        db.execSQL(sql);
        sharedPreferences.edit().putString(UPDATE_DATABASE, UPDATE_DATABASE).apply();
        closeDb();
    }

    /**
     * Xóa 1 note ờ cơ sở dữ liệu
     */
    public void deleteNote(String noteTitleDelete, String noteContentDelete, String noteImg, String noteThumbnail, String typeSave, String latlong, String alarm, String wifiName) {
        openDb();
        String[] whereClause = new String[]{noteTitleDelete, noteContentDelete, noteImg, noteThumbnail, typeSave, latlong, alarm, wifiName};
        Log.d(TAG, "deleteNote: " + Arrays.toString(whereClause));
        for (int i = 0 ; i<whereClause.length;i++) {
            if(whereClause[i]==null){
                whereClause[i]="";
            }
        }
        //        del row
        db.delete(TABLE_NAME
                , NAME_COLUMN_TITLE_NOTE + "=? AND "
                        + NAME_COLUMN_CONTENT_NOTE + "=? AND "
                        + NAME_COLUMN_PATH_IMAGE_NOTE + "=? AND "
                        + NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE + "=? AND "
                        + NAME_COLUMN_TYPE_SAVE + "=? AND "
                        + NAME_COLUMN_LATLONG + "=? AND "
                        + NAME_COLUMN_ALARM + "=? AND "
                        + NAME_COLUMN_WIFI_NAME + "=?"
                , whereClause);
        Log.d(TAG, "deleteNote: " + Arrays.toString(whereClause));

        //del img
        if (noteImg != null && !noteImg.equals("")) {
            Log.d(TAG, "deleteNote: noteImg" + noteImg);
            File img = new File(noteImg);
            if (img.exists()) {
                Log.d(TAG, "deleteNote: del IMG thành công: " + noteImg);
                img.delete();
            } else {
                Log.d(TAG, "IMG fail : File không tồn tại");
            }
        }
        if (noteThumbnail != null && !noteThumbnail.equals("")) {
            File thumbnail = new File(noteThumbnail);
            if (thumbnail.exists()) {
                thumbnail.delete();
                Log.d(TAG, "deleteNote: del THUMBNAIL thành công: " + noteThumbnail);
            } else {
                Log.d(TAG, "THUMBNAIL fail : file không tồn tại");
            }
        }
        closeDb();
    }

    /*Sửa tất cả các ký tự đặc biệt để khi thêm vào cơ sở dữ liệu k gây lỗi sqlite*/
    public String replaceCharApostrophe(String str) {
        String result = str;
        if (str != null && str.contains("'")) {
            str = str.replace("'", "''");
            Log.d(TAG, "replaceCharApostrophe: str after replace: " + str);
            return str;
        } else {
            return result;
        }
    }

    /*Backup tất cả note ra External Storage*/
    public void backupAllNote(final Service service, final NotificationService notificationService, final EventBus eventBus) {
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
                copyFile(fileDbIn, fileDbOut, true);

                /*Back up tất cả các ảnh và thumbnail của nó
                * */
                Cursor cursor = readAllDataWithColumnTypeSave("All");//Load cursor chỉ copy những ảnh nào note còn tồn tại trong database
                cursor.moveToFirst();
                int percent = 0;
                while (!cursor.isAfterLast()) {
                    /*Gửi message update textview trạng thái % */
                    publishProgress(MSG_UPDATE_PERCENT_BACKUP, percent, cursor.getCount());
                    //Copy file ảnh gốc
                    Log.d(TAG, "doInBackground: img path: " + cursor.getString(COLUMN_PATH_IMAGE_NOTE));
                    String pathImg = cursor.getString(COLUMN_PATH_IMAGE_NOTE);
                    if (pathImg != null && !pathImg.equals("")) {
                        File fileImgIn = new File(cursor.getString(COLUMN_PATH_IMAGE_NOTE));//Đầu vào là file lấy từ cursor đang trỏ tới
                        File fileImgOut = new File(folderImageSave.getPath() + "/" + fileImgIn.getName());///Đầu ra là file trong thư mục ở External
                        copyFile(fileImgIn, fileImgOut, false);
                    }

                    //Copy file thumbnail
                    String pathThumbnail = cursor.getString(COLUMN_PATH_THUMBNAIL_IMAGE_NOTE);
                    if (pathThumbnail != null && pathThumbnail.equals("")) {
                        File fileThumbnailIn = new File(cursor.getString(COLUMN_PATH_THUMBNAIL_IMAGE_NOTE));//Đầu vào là file lấy từ cursor đang trỏ tới
                        File fileThumbnailOut = new File(folderImageSave.getPath() + "/" + fileThumbnailIn.getName());//Đầu ra là file trong thư mục ở External
                        copyFile(fileThumbnailIn, fileThumbnailOut, false);
                    }

//                MediaScannerConnection.scanFile(context, new String[]{file1.getPath()}, null, null);
                    if (cursor.getPosition() == (cursor.getCount() - 1)) {
                        /*Gửi message update textview trạng thái % */
                        publishProgress(MSG_BACKUP_COMPLETE);
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
                        notificationService.updateProgressBar(percentBackup);
                        NotificationService.upToNotify(AppService.ID_NOTI_BACKUP, notificationService.build(), context);
                        //                        Integer[] msgBackup = {MSG_UPDATE_PERCENT_BACKUP, percentBackup};
//                        eventBus.post(msgBackup);
                        break;
                    case MSG_BACKUP_COMPLETE:
                        eventBus.postSticky(new MessageEventBus(AppService.SERVICE_BACKUP));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                notificationService.updateProgressBar(100);
                                notificationService.setProgress(100, 100, false);
                                NotificationService.upToNotify(AppService.ID_NOTI_BACKUP, notificationService.build(), context);
                                Toast.makeText(context, "Sao lưu dữ liệu hoàn tất", Toast.LENGTH_LONG).show();
                                if (sharedPreferences.getString(SWITCH_SCREENSHOT, "null").equals("off")) {
                                    service.stopSelf();
                                } else {
                                }
                            }
                        }, 1000);
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
    public void restoreAllNote(final Service appService, final NotificationService notificationService, final EventBus eventBus) {
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
                        copyFile(imgIn, imgOut, false);
                        if (pointer.getName().equals(folderImageSave.listFiles()[folderImageSave.listFiles().length - 1].getName())) {
                            publishProgress(MSG_RESTORE_COMPLETE);
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
                            notificationService.updateProgressBar(percentRestore);
                            NotificationService.upToNotify(AppService.ID_NOTI_RESTORE, notificationService.build(), context);

//                            Integer[] msgRestorePercent = {MSG_UPDATE_PERCENT_RESTORE, percentRestore};
//                            eventBus.post(msgRestorePercent);
                            break;
                        case MSG_RESTORE_COMPLETE:
                            eventBus.postSticky(new MessageEventBus(AppService.SERVICE_RESTORE));
//                            Integer[] msgRestoreComplete = {MSG_RESTORE_COMPLETE, values[1]};
//                            eventBus.post(msgRestoreComplete);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    notificationService.updateProgressBar(100);
                                    notificationService.setProgress(100, 100, false);
                                    NotificationService.upToNotify(AppService.ID_NOTI_RESTORE, notificationService.build(), context);
                                    Toast.makeText(context, "Phục hồi dữ liệu hoàn tất", Toast.LENGTH_LONG).show();
                                    if (sharedPreferences.getString(SWITCH_SCREENSHOT, "").equals("false")) {
                                        appService.stopSelf();
                                    }
                                }
                            }, 1000);
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
    }

    /*Ghi tiếp dữ liệu ở database chứ không ghi mới từ đầu*/
    private void syncDatabaseFile(File fileDbIn, File fileDbOut) {
        if (!fileDbOut.exists()) {
            copyFile(fileDbIn, fileDbOut, true);
        } else {
            SQLiteDatabase inputFileDb = null;
            try {
                inputFileDb = SQLiteDatabase.openDatabase(fileDbIn.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            } catch (SQLiteCantOpenDatabaseException e) {
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

//                Đoạn code này để làm cái gì vậy ???????
//                Intent i = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                context.startActivity(i);

            }
            String readAll = "SELECT * FROM " + TABLE_NAME;
            Cursor inputCursor = inputFileDb.rawQuery(readAll, null);
            int i = 0;
            Cursor cursorCheckExists = null;
            for (inputCursor.moveToFirst(); !inputCursor.isAfterLast(); inputCursor.moveToNext()) {
                String noteTitleReplace = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_TITLE_NOTE);
                String noteContentReplace = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_CONTENT_NOTE);
                String noteImgReplace = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_PATH_IMAGE_NOTE);
                String noteThumbnailReplace = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_PATH_THUMBNAIL_IMAGE_NOTE);
                String noteTypeSaveReplace = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_TYPE_SAVE);
                String noteLatlongReplace = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_LATLONG);
                String noteAlarm = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_ALARM);
                String wifiName = inputCursor.getString(DatabaseManagerCopyDb.COLUMN_WIFI_NAME);
                String sqlCheckExit =
                        "SELECT 1 FROM " + TABLE_NAME + " WHERE "
                                + NAME_COLUMN_TITLE_NOTE + "='" + replaceCharApostrophe(noteTitleReplace) + "' AND "
                                + NAME_COLUMN_CONTENT_NOTE + "='" + replaceCharApostrophe(noteContentReplace) + "' AND "
                                + NAME_COLUMN_PATH_IMAGE_NOTE + "='" + replaceCharApostrophe(noteImgReplace) + "' AND "
                                + NAME_COLUMN_PATH_THUMBNAIL_IMAGE_NOTE + "='" + replaceCharApostrophe(noteThumbnailReplace) + "' AND "
                                + NAME_COLUMN_TYPE_SAVE + "='" + replaceCharApostrophe(noteTypeSaveReplace) + "' AND "
                                + NAME_COLUMN_WIFI_NAME + "='" + replaceCharApostrophe(wifiName) + "'";
                // TODO: 2016-10-09 add thêm điều kiện
                i++;
                openDb();
                cursorCheckExists = db.rawQuery(sqlCheckExit, null);
                if (cursorCheckExists.getCount() == 0) {
                    Log.d(TAG, "syncDatabaseFile: CHƯA TỒN TẠI");
                    // TODO: 9/28/2016 Trùng quá nhiều ' và chưa add wifi
                    insert(noteTitleReplace, noteContentReplace, noteImgReplace, noteThumbnailReplace, noteTypeSaveReplace, noteLatlongReplace, noteAlarm, wifiName, false);
                } else {
                    Log.d(TAG, "syncDatabaseFile: " + i + " tồn tại rồi");
                }
            }
            if (inputFileDb.isOpen()) {
                inputFileDb.close();
            }
        }
    }

    /*Xóa hết các dòng trong bảng*/
    public void delAllDataTable(final NotificationService notificationService, final EventBus eventBus) {
        sharedPreferences.edit().putString(UPDATE_DATABASE, UPDATE_DATABASE).apply();

        new File(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME).delete();
        copyAssetDbToInternal();
        AsyncTask<Integer, Integer, Void> asyncTask = new AsyncTask<Integer, Integer, Void>() {
            @Override
            protected Void doInBackground(Integer... values) {
                openDb();

                int percent = 0;
                File listFile = new File(PATH_APP_INTERNAL + "/imageSave");
                int length =  listFile.listFiles().length;
                for (File f : listFile.listFiles()) {
                    f.delete();
                    percent++;

                    if (percent == (listFile.listFiles().length)) {
                        publishProgress(MSG_DELETE_COMPLETE);
                    }
                    publishProgress(MSG_UPDATE_PERCENT_DELETE, percent, length);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int state = values[0];
                switch (state) {
                    case MSG_UPDATE_PERCENT_DELETE:
                        int percentRestore = (values[1] * 100) / values[2];
                        notificationService.updateProgressBar(percentRestore);
                        NotificationService.upToNotify(AppService.ID_NOTI_DELETE, notificationService.build(), context);
                        break;
                    case MSG_DELETE_COMPLETE:
                        eventBus.postSticky(new MessageEventBus(AppService.SERVICE_DELETE));
                        notificationService.updateProgressBar(100);
                        notificationService.setProgress(100, 100, false);
                        NotificationService.upToNotify(AppService.ID_NOTI_DELETE, notificationService.build(), context);
                        sharedPreferences.edit().putString(UPDATE_DATABASE, UPDATE_DATABASE).apply();
                        break;
                    default:
                        break;
                }
            }
        };
        asyncTask.execute();
    }

    /**
     * Tạo folder ở bộ nhớ trong nếu nó chưa tồn tại
     */
    public void createDefaultFolderInternal() {
        //Create folder dataBase
        File dirDataBase = new File(PATH_APP_INTERNAL + "/databases");
        if (!dirDataBase.exists()) {
            dirDataBase.mkdir();
        }

        File dbInInternal = new File(PATH_APP_INTERNAL + "/databases/" + DATABASE_NAME);
        if (!dbInInternal.exists()) {
            Log.d(TAG, "DatabaseManagerCopyDb: VÀO");
            copyAssetDbToInternal();
        } else {
            Log.d(TAG, "DatabaseManagerCopyDb: TRƯỢT");
        }

        //Create folder imgSave
        File dirImage = new File(PATH_APP_INTERNAL + "/imageSave");
        if (!dirImage.exists()) {
            dirImage.mkdir();
        }
    }

    /**
     * Tạo folder ở bộ nhớ ngoài nếu nó chưa tồn tại
     */
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

    /**
     * Trả về số note tìm thấy với wifi bắt được và toàn bộ thông tin của note
     */
    public ArrayList<NoteItem> readDataWithWifi(String ssid) {
        openDb();
        String sql = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + NAME_COLUMN_WIFI_NAME + "='" + ssid + "'";
        Cursor cursor = db.rawQuery(sql, null);
        RecycleNoteAdapter.cursorToArrayList(cursor);
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            ArrayList<NoteItem> arrayList = RecycleNoteAdapter.cursorToArrayList(cursor);
            closeDb();
            return arrayList;
        } else {
            return null;
        }

    }

    public void exportImageToExternal(String pathImage) {
        createDefaultFolderExternal();
        if (pathImage == null || pathImage.equals("")) {
            Toast.makeText(context, "Image doesn't exists", Toast.LENGTH_SHORT).show();
            return;
        }
        File folderExportImage = new File(Environment.getExternalStorageDirectory().getPath() + "/" + FOLDER_BACKUP_NAME + "/" + FOLDER_EXPORT_IMAGE);
        if (!folderExportImage.exists()) {
            folderExportImage.mkdir();
        }
        File fileImgIn = new File(pathImage);
        Log.d(TAG, "exportImageToExternal: file name: " + fileImgIn.getName());
        File fileImgOut = new File(folderExportImage + "/" + fileImgIn.getName().replace("Screenshot", "Note"));
        copyFile(fileImgIn, fileImgOut, true);
        Toast.makeText(context, "Export complete", Toast.LENGTH_SHORT).show();
    }
}