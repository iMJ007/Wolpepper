package com.eclectik.wolpepper.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.eclectik.wolpepper.R;

public class NotificationUtils {
    private NotificationUtils(){}

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager.getNotificationChannel(context.getString(R.string.channel_id)) != null){
            return;
        }

        // The id of the channel.
        String id = context.getString(R.string.channel_id);
        // The user-visible name of the channel.
        CharSequence name = context.getString(R.string.channel_name);
        // The user-visible description of the channel.
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;
        mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(context.getColor(R.color.colorPrimary));
        mChannel.enableVibration(false);
        mChannel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public static Notification showDownloadingAndSettingWallpaperNotification(Context context, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return showNonCompatDownloadingAndSettingWallpaperNotification(context, pendingIntent);
        }

//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.reminder_notification_layout);
//        remoteViews.setTextViewText(R.id.notification_desc, "Fetching pronunciation & playing : " + wordToSpeak);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_format_paint_white_24dp)
                .setContentTitle("Wolpepper")
                .setContentText("Downloading and setting wallpaper!")
//                .setCustomContentView(remoteViews)
                .setAutoCancel(true)
                .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
                .setTicker("Downloading and setting wallpaper!")
                .setVibrate(new long[]{100})
                .setContentIntent(pendingIntent).build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification showNonCompatDownloadingAndSettingWallpaperNotification(Context context, PendingIntent pendingIntent) {
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.reminder_notification_layout);
//        remoteViews.setTextViewText(R.id.notification_desc, "Fetching pronunciation & playing : " + wordToSpeak);

        return new Notification.Builder(context, context.getString(R.string.channel_id))
                .setSmallIcon(R.drawable.ic_format_paint_white_24dp)
                .setContentTitle("Wolpepper")
                .setContentText("Downloading and setting wallpaper!")
//                .setCustomContentView(remoteViews)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setTicker("Downloading and setting wallpaper!")
                .setContentIntent(pendingIntent).build();
    }

}
