package com.example.forgetmenot.workflow;

import java.time.Instant;
import java.util.UUID;

public final class WorkSession {

    public String Id;
    public Instant CheckIn;
    public Instant CheckOut;       // null while active
    public int IntendedMinutes;    // >= 0

    public WorkSession(final Instant checkIn, final int intendedMinutes) {
        Id = UUID.randomUUID().toString();
        CheckIn = checkIn;
        CheckOut = null;
        IntendedMinutes = Math.max(0, intendedMinutes);
    }

    public boolean isOpen() {
        return CheckOut == null;
    }
}
