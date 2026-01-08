package com.example.forgetmenot.habits;

public final class HabitStatItem {
    public final String Name;
    public final int TotalCompletions7;
    public final int DaysMetGoal7;
    public final int ScheduledDays7;
    public final int CurrentStreak;
    public final int Goal;

    public HabitStatItem(
            final String name,
            final int totalCompletions7,
            final int daysMetGoal7,
            final int scheduledDays7,
            final int currentStreak,
            final int goal
    ) {
        Name = name;
        TotalCompletions7 = totalCompletions7;
        DaysMetGoal7 = daysMetGoal7;
        ScheduledDays7 = scheduledDays7;
        CurrentStreak = currentStreak;
        Goal = goal;
    }
}
