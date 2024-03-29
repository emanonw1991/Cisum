package com.vivo.emanon.cisum.views;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.vivo.emanon.cisum.activity.SplashActivity;
import com.vivo.emanon.cisum.constant.Extras;
import com.vivo.emanon.cisum.model.Music;
import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.receiver.NotificationReceiver;
import com.vivo.emanon.cisum.service.AudioService;
import com.vivo.emanon.cisum.utils.CoverLoader;

/**
 * 通知栏控制
 * Created by emanon on 2017/10/12.
 */

public class NotificationController {

    //通知ID
    private static final int NOTIFICATION_ID = 0x19;
    private static AudioService sAudioService;
    private static NotificationManager notificationManager;

    public static void init(AudioService audioService) {
        NotificationController.sAudioService = audioService;
        notificationManager = (NotificationManager) audioService.
                getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void showPlay(Music music) {
        sAudioService.startForeground(NOTIFICATION_ID, buildNotification(sAudioService, music,
                true));
    }

    public static void showPause(Music music) {
        sAudioService.stopForeground(false);
        notificationManager.notify(NOTIFICATION_ID, buildNotification(sAudioService, music, false));
    }

    public static void cancelAll() {
        notificationManager.cancelAll();
    }

    private static Notification buildNotification(Context context, Music music, boolean isPlaying) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra(Extras.EXTRA_NOTIFICATION, true);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setCustomContentView(getRemoteViews(context, music, isPlaying));
        return builder.build();
    }

    private static RemoteViews getRemoteViews(Context context, Music music, boolean isPlaying) {
        String title = music.getTitle();
        String subtitle = music.getArtist() + " - " + music.getAlbum();
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        if (cover != null) {
            remoteViews.setImageViewBitmap(R.id.iv_icon, cover);
        } else {
            remoteViews.setImageViewResource(R.id.iv_icon, R.mipmap.ic_launcher_round);
        }
        remoteViews.setTextViewText(R.id.tv_title, title);
        remoteViews.setTextViewText(R.id.tv_subtitle, subtitle);

        Intent playIntent = new Intent(NotificationReceiver.ACTION_NOTIFICATION);
        playIntent.putExtra(NotificationReceiver.EXTRA, NotificationReceiver.EXTRA_PLAY_PAUSE);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setImageViewResource(R.id.iv_play_pause, getPlayIconRes(isPlaying));
        remoteViews.setOnClickPendingIntent(R.id.iv_play_pause, playPendingIntent);

        Intent nextIntent = new Intent(NotificationReceiver.ACTION_NOTIFICATION);
        nextIntent.putExtra(NotificationReceiver.EXTRA, NotificationReceiver.EXTRA_NEXT);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setImageViewResource(R.id.iv_next, getNextIconRes());
        remoteViews.setOnClickPendingIntent(R.id.iv_next, nextPendingIntent);

        return remoteViews;
    }

    private static int getPlayIconRes(boolean isPlaying) {
        if (isPlaying) {
            return R.drawable.ic_status_bar_pause_light_selector;
        } else {
            return R.drawable.ic_status_bar_play_light_selector;
        }
    }

    private static int getNextIconRes() {
        return R.drawable.ic_status_bar_next_light_selector;
    }
}
