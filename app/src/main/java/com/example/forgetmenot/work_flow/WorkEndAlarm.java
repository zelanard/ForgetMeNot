package com.example.forgetmenot.work_flow;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class WorkEndAlarm {
    private static final int REQUEST_CODE = 2001;

    private WorkEndAlarm() {}

    public static void schedule(final Context context, final long triggerAtMillis) {
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        final PendingIntent pi = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                new Intent(context, WorkEndReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
    }

    public static void cancel(final Context context) {
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        final PendingIntent pi = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                new Intent(context, WorkEndReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pi);
    }
}
