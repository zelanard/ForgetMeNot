package com.example.forgetmenot.work_flow;

import java.util.concurrent.TimeUnit;

public final class TimeRounding {
    private TimeRounding() {}

    public static long roundToNearest15MinutesMillis(final long timeMillis) {
        final long q = TimeUnit.MINUTES.toMillis(15);
        return ((timeMillis + (q / 2)) / q) * q;
    }
}