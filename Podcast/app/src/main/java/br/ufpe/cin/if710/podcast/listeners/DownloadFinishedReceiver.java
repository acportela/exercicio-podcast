package br.ufpe.cin.if710.podcast.listeners;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

/**
 * Created by acpr on 12/10/17.
 */

public class DownloadFinishedReceiver extends BroadcastReceiver {

    private static final int MY_NOTIFICATION_ID=2;
    NotificationManager notificationManager;
    Notification myNotification;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        myNotification = new NotificationCompat.Builder(context)
                .setContentTitle("Podcast")
                .setContentText("Podcast Baixado!")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setVibrate(new long[] {0, 1000, 1000, 1000, 1000 })
                .setSmallIcon(R.drawable.ic_headset_black_24dp)
                .build();

        notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
    }
}
