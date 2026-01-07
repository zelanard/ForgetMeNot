package com.example.forgetmenot.workflow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationChannels {
    private NotificationChannels() {}

    public static final String CHANNEL_WORKDAY_OVER = "workday_over";

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_WORKDAY_OVER,
                "Workday reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        ch.setDescription("Alerts when intended work time is finished");
        // Sound/vibration are controlled by channel settings (user can modify).
        // You can enable vibration:
        ch.enableVibration(true);

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(ch);
    }
}
