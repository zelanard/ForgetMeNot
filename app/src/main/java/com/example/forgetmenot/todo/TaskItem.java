package com.example.forgetmenot.todo;

import androidx.annotation.NonNull;

public final class TaskItem {
    public final int IndexInAllTasks;
    @NonNull public final Task Task;

    public TaskItem(final int indexInAllTasks, @NonNull final Task task) {
        this.IndexInAllTasks = indexInAllTasks;
        this.Task = task;
    }
}
