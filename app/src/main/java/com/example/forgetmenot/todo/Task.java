package com.example.forgetmenot.todo;

import java.time.Instant;

public final class Task {
    public String Description;
    public Instant CreationDate;
    public Boolean Completed;

    public Task(String description, Instant epoch, boolean completed) {
        Description = description;
        CreationDate = epoch;
        Completed = completed;
    }
}
