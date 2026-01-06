package com.example.forgetmenot.habits;

import java.time.Instant;
import java.util.EnumSet;

public class Habit {
    public enum Days{
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday,
        Sunday
    }

    public String Name;
    public String Description;
    public Instant StartDate;
    public EnumSet<Days> Days;

     public Habit(String name, String description, Instant startDate, EnumSet<Days> days){
         Name = name;
         Description = description;
         StartDate = startDate;
         Days = days;
     }
}
