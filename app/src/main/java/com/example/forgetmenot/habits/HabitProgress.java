package com.example.forgetmenot.habits;

import java.time.LocalDate;

public final class HabitProgress {
    public String HabitId;
    public LocalDate Date;
    public int Count;

    public HabitProgress(final String habitId, final LocalDate date, final int count) {
        HabitId = habitId;
        Date = date;
        Count = Math.max(0, count);
    }
}
