package com.p2ild.notetoeverything.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.p2ild.notetoeverything.DatabaseManager;
import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.SurfaceView;
import com.p2ild.notetoeverything.frgment.FragmentMain;
import com.p2ild.notetoeverything.frgment.FrgAddNote;
import com.p2ild.notetoeverything.frgment.FrgCapture;
import com.p2ild.notetoeverything.frgment.FrgEdit;

import java.io.File;

public class MainActivity extends Activity {

    public static final String KEY_POSITION = "KEY_POSITION";
    public static final String KEY_OBJECT_DB = "KEY_OBJECT_DB";
    private static final String TAG = MainActivity.class.getSimpleName();
    private FrgAddNote frgAdd;
    private FrgCapture frgCapture;
    private FragmentMain frgMain;
    private DatabaseManager db;
    private boolean isExit;
    private ImageButton btAddNote;
    private boolean isFileExists;
    private FrgEdit frgEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Khởi tạo cơ sở dữ liệu load ảnh thêm sửa xóa
        db = new DatabaseManager(this);

        /*Backup Database và ảnh
        *backupAllDatabase();
        * */

        //Khởi tạo view
        showFrgMain();
        isExit = false;

        initOpenCV();
    }

    private void backupAllDatabase() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.backupAllNote();
            }
        });
        thread.start();
    }


    private void initOpenCV() {
        // TODO: 8/25/2016 Khởi tạo OpenCv xử lý ảnh
    }

    private void removeAllFragment() {
        if (frgAdd != null) {
            getFragmentManager().beginTransaction()
                    .remove(frgAdd).commit();
        }
        if (frgCapture != null) {
            getFragmentManager().beginTransaction()
                    .remove(frgCapture).commit();
        }
        if (frgEdit != null) {
            getFragmentManager().beginTransaction()
                    .remove(frgEdit).commit();
        }
    }

    private void showFrgMain() {
        removeAllFragment();
        frgMain = new FragmentMain(db.readAllData());
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main, frgMain).commit();
    }

    public void showFrgAddNote() {
        frgAdd = new FrgAddNote();
        getFragmentManager()
                .beginTransaction().add(R.id.activity_main, frgAdd)
                .hide(frgMain).commit();
    }

    public void showFrgCapture() {
        frgCapture = new FrgCapture();
        getFragmentManager()
                .beginTransaction()
                .hide(frgAdd)
                .add(R.id.activity_main, frgCapture)
                .commit();
    }

    public void showFrgEdit(String title, String content) {
        frgEdit = new FrgEdit(title, content);
        getFragmentManager().beginTransaction()
                .add(R.id.activity_main, frgEdit)
                .commit();
    }

    public void insertToDataBase(final String imgPath, final String imgThumbnailPath) {
        isFileExists = false;
        db.insert(frgAdd.getNoteTitle(), frgAdd.getNoteContent(), imgPath, imgThumbnailPath);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(imgThumbnailPath);
                while (!isFileExists) {
                    Log.d(TAG, "while true");
                    if (file.exists()) {
                        showFrgMain();
                        getFragmentManager().beginTransaction()
                                .remove(frgCapture)
                                .remove(frgAdd)
                                .commit();
                        Log.d(TAG, "show complete");
                        SurfaceView.camera.stopPreview();
                        SurfaceView.camera.release();

                        isFileExists = true;
                        break;
                    } else {
                        Log.d(TAG, "Chưa tồn tại");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
    }

    public void updateDataBase(String oldTitle, String newTitle, String newContent) {
        db.update(oldTitle, newTitle, newContent);
        showFrgMain();
    }

    public void deleteDb(String noteTitleDelete) {
        db.deleteNote(noteTitleDelete);
        showFrgMain();
    }

    @Override
    public void onBackPressed() {
        if (frgAdd != null && frgAdd.isVisible()) {
            getFragmentManager().beginTransaction()
                    .remove(frgAdd)
                    .show(frgMain)
                    .commit();
            return;
        }

        if (frgEdit != null && frgEdit.isVisible()) {
            getFragmentManager().beginTransaction()
                    .remove(frgEdit)
                    .show(frgMain)
                    .commit();
            return;
        }

        if (frgCapture != null && frgCapture.isVisible()) {
            SurfaceView.camera.stopPreview();
            SurfaceView.camera.release();
            getFragmentManager().beginTransaction()
                    .remove(frgCapture)
                    .show(frgAdd)
                    .commit();
            return;
        }

        if (isExit) {
            super.onBackPressed();
        } else
            Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        isExit = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isExit = false;
            }
        }, 500);
    }

    public void startNoteActivity(int position) {
        Intent intent = new Intent(MainActivity.this, NoteContentActivity.class);
        intent.putExtra(KEY_POSITION, position);
        startActivity(intent);
    }
}
