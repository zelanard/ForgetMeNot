package com.example.forgetmenot.work_flow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public final class WorkEndReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "work_end_channel";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        final NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Work End Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        nm.createNotificationChannel(channel);

        final NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Work time finished")
                .setContentText("Your intended work duration has ended.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify(3001, b.build());
    }
}
