package com.example.forgetmenot.todo;

import androidx.annotation.NonNull;

import java.util.List;

public final class TaskSection {
    @NonNull public final String Title;
    @NonNull public final List<TaskItem> Items;

    public TaskSection(@NonNull final String title, @NonNull final List<TaskItem> items) {
        Title = title;
        Items = items;
    }
}
