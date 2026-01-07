package com.example.forgetmenot.habits;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

public class Habit {

    public enum Day {
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    }

    public String Id;
    public String Name;
    public String Description;
    public Instant StartDate;
    public EnumSet<Day> ScheduledDays;

    public int Goal;

    public Habit(String name, String description, Instant startDate, EnumSet<Day> scheduledDays, int goal) {
        Id = UUID.randomUUID().toString();
        Name = name;
        Description = description;
        StartDate = startDate;
        ScheduledDays = scheduledDays;
        Goal = Math.max(1, goal);
    }
}
