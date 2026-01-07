package com.example.forgetmenot.workflow;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.forgetmenot.R;

public final class WorkdayOverReceiver extends BroadcastReceiver {

    public static final String EXTRA_MESSAGE = "extra_message";
    private static final int NOTIF_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WorkdayOverReceiver", "onReceive fired");

        // Android 13+ requires POST_NOTIFICATIONS permission granted at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("WorkdayOverReceiver", "POST_NOTIFICATIONS not granted; dropping notification");
                return;
            }
        }

        NotificationChannels.ensureChannels(context);

        String msg = intent.getStringExtra(EXTRA_MESSAGE);
        if (msg == null || msg.trim().isEmpty()) {
            msg = "Work day is over";
        }

        Intent openIntent = new Intent(context, WorkFlowActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentPi = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(
                context, NotificationChannels.CHANNEL_WORKDAY_OVER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("ForgetMeNot")
                .setContentText(msg)
                .setAutoCancel(true)
                .setContentIntent(contentPi)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(context).notify(NOTIF_ID, b.build());
    }
}
