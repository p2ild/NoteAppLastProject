package com.p2ild.notetoeverything.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.p2ild.notetoeverything.R;
import com.p2ild.notetoeverything.activity.MainActivity;
import com.p2ild.notetoeverything.adapter.NoteItem;
import com.p2ild.notetoeverything.frgment.FragmentMain;
import com.p2ild.notetoeverything.other.DataSerializable;

import java.util.ArrayList;

/**
 * Created by duypi on 10/3/2016.
 */
public class NotificationService extends NotificationCompat.Builder {

    public static final String NOTI_SHOW_TYPE = "NOTI_SHOW_TYPE";
    public static final String NOTI_CONTENT = "NOTI_CONTENT";
    private static final String TAG = NotificationService.class.getSimpleName();
    private ArrayList<NoteItem> arrayListNote;
    private Context context;
    private String titleNoti;
    private String noteDetect, noteTitleNewest, noteContentNewest, path;
    private String showFolderBackup = "";

    /**
     * Đối với backup/restore/delete
     */
    public NotificationService(Context context, int idOrType) {
        super(context);
        this.context = context;
        initNoti(idOrType);
    }

    /**
     * Đối với Screenshot và Clipboard
     */
    public NotificationService(Context context, ArrayList<NoteItem> o, int type, String noteDetect, String noteTitleNewest, String noteContentNewest, String path) {
        super(context);
        this.context = context;
        this.noteDetect = noteDetect;
        this.noteTitleNewest = noteTitleNewest;
        this.noteContentNewest = noteContentNewest;
        this.path = path;
        arrayListNote = o;
        initNoti(type);
    }

    public static void upToNotify(int id, Notification notification, Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, notification);
    }

    private void initNoti(int idOrType) {
        setAutoCancel(true);
        getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        setContentInfo("dev.p2ild");
        switch (idOrType) {
            //Noti allway show
            case AppService.ID_NOTI_SERVICE:
                setSmallIcon(R.drawable.ic_noti_app_note_128);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_app_note_orange));
                setContentTitle("Note Everything is already");
                setContentText("How are you today?");

                Intent itNotiService = new Intent(context, AppService.class);
                itNotiService.putExtra(NOTI_SHOW_TYPE, AppService.NOTI_SWITCH_ALLWAY_SHOW);
                PendingIntent piNotiService = PendingIntent.getService(context, AppService.NOTI_SWITCH_ALLWAY_SHOW, itNotiService, PendingIntent.FLAG_UPDATE_CURRENT);
                setContentIntent(piNotiService);
                break;
            case AppService.SERVICE_BACKUP:
                setSmallIcon(R.drawable.ic_noti_backup_running);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_backup_running_orange));
                setContentTitle("Backup note ..");
                break;
            case AppService.SERVICE_RESTORE:
                setSmallIcon(R.drawable.ic_noti_backup_running);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_backup_running_orange));
                setContentTitle("Restore note ..");
                break;
            case AppService.SERVICE_DELETE:
                setSmallIcon(R.drawable.ic_noti_backup_running);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_backup_running_orange));
                setContentTitle("Delete note ..");
                break;
            case AppService.NOTI_CLIPBOAD:
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                setSmallIcon(R.drawable.ic_noti_clipboard);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_clipboard_orange));
                setContentTitle("Bạn vừa Copy, bạn có muốn save lại không ?");
                setStyle(new NotificationCompat.BigTextStyle(this)
                        .bigText(noteContentNewest));
                Intent itClipboard = new Intent(context, AppService.class);
                itClipboard.putExtra(NOTI_SHOW_TYPE, AppService.NOTI_CLIPBOAD);
                PendingIntent piClipBoard = PendingIntent.getService(context, AppService.NOTI_CLIPBOAD, itClipboard, PendingIntent.FLAG_UPDATE_CURRENT);
                setContentIntent(piClipBoard);
                break;
            case AppService.NOTI_SCREENSHOT:
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                setSmallIcon(R.drawable.ic_noti_screen_shot);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_screen_shot_orange));
                setContentTitle("Bạn vừa Chụp màn hình, bạn có muốn save lại không ?");
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                setStyle(new NotificationCompat.BigPictureStyle(this)
                        .setSummaryText(noteContentNewest)
                        .bigPicture(bitmap));
                Intent itScreenshot = new Intent(context, AppService.class);
                itScreenshot.putExtra(NOTI_SHOW_TYPE, AppService.NOTI_SCREENSHOT);
                PendingIntent piScreenshot = PendingIntent.getService(context, AppService.NOTI_SCREENSHOT, itScreenshot, PendingIntent.FLAG_UPDATE_CURRENT);
                setContentIntent(piScreenshot);
                break;
            case AppService.WIFI_DETECT:
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                setSmallIcon(R.drawable.ic_noti_wifi_detect);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_wifi_detect_orange));
                setContentTitle("Có " + noteDetect + " trong wifi này");
                //Dành cho clipboard
                if (path == null || path.isEmpty()) {
                    setStyle(new NotificationCompat.BigTextStyle(this)
                            .bigText(noteContentNewest));
                }
                //Dành cho screenShot
                else {
                    setContentTitle(noteTitleNewest);
                    Bitmap bitmap1 = BitmapFactory.decodeFile(path);
                    setStyle(new NotificationCompat.BigPictureStyle(this)
                            .setSummaryText(noteContentNewest)
                            .bigPicture(bitmap1));
                }
                setContentIntentForWifiDetect(AppService.WIFI_DETECT);
                break;
            case AppService.GPS_DETECT:
                setSmallIcon(R.drawable.ic_noti_gps_detect);
                setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti_gps_detect_orange));
                setContentTitle("Có " + noteDetect + " trong xung quanh khu vực này");
                //Dành cho clipboard
                if (path == null || path.isEmpty()) {
                    setStyle(new NotificationCompat.BigTextStyle(this)
                            .bigText(noteContentNewest));
                }
                //Dành cho screenShot
                else {
                    setContentTitle(noteTitleNewest);
                    Bitmap bitmap1 = BitmapFactory.decodeFile(path);
                    setStyle(new NotificationCompat.BigPictureStyle(this)
                            .setSummaryText(noteContentNewest)
                            .bigPicture(bitmap1));
                }
                setContentIntentForWifiDetect(AppService.GPS_DETECT);
                break;
            default:
                break;
        }
    }

    private void setContentIntentForWifiDetect(int type) {
        if (arrayListNote != null) {
            switch (type) {
                case AppService.WIFI_DETECT:
                    Intent itNoteDetect = new Intent(context, MainActivity.class);
                    itNoteDetect.putExtra(FragmentMain.KEY_TYPE_SAVE, AppService.WIFI_DETECT+"");
                    itNoteDetect.putExtra(AppService.WIFI_DETECT + "", new DataSerializable(arrayListNote));
                    PendingIntent piWifiDetect = PendingIntent.getActivity(context, AppService.WIFI_DETECT, itNoteDetect, PendingIntent.FLAG_ONE_SHOT);
                    setContentIntent(piWifiDetect);
                    break;
                case AppService.GPS_DETECT:
                    Intent itGpsDetect = new Intent(context, MainActivity.class);
                    itGpsDetect.putExtra(AppService.GPS_DETECT + "", new DataSerializable(arrayListNote));
                    itGpsDetect.putExtra(FragmentMain.KEY_TYPE_SAVE, AppService.GPS_DETECT+"");
                    PendingIntent piGpsDetect = PendingIntent.getActivity(context, AppService.GPS_DETECT, itGpsDetect, PendingIntent.FLAG_ONE_SHOT);
                    setContentIntent(piGpsDetect);
                    break;
                default:
                    break;
            }
        }
    }

    public void updateProgressBar(int percent) {
        setProgress(100, percent, false);
        if (percent < 100) {
            setSmallIcon(R.drawable.ic_noti_backup_running);
            setContentText(percent + "%");
        } else {
            setSmallIcon(R.drawable.ic_noti_backup_complete);
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            setProgress(0, 0, false);
            setContentText("Complete!!");
        }
    }

    public void setTitleNoti(String titleNoti) {
        this.titleNoti = titleNoti;
        setStyle(new NotificationCompat.BigTextStyle()
                .bigText(titleNoti)
        );
    }
}
