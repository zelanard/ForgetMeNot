package com.example.forgetmenot.todo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TodoViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();

    public TodoViewModel(@NonNull final Application application) {
        super(application);
        repository = new TaskRepository(application.getApplicationContext());
        reload();
    }

    public LiveData<List<Task>> getTasks() {
        return tasksLiveData;
    }

    public void setActiveAt(final int index, final boolean completed) {
        final ArrayList<Task> tasks = repository.loadTasks();
        if (index < 0 || index >= tasks.size()) {
            return;
        }

        final Task t = tasks.get(index);
        t.Completed = completed;

        repository.updateTaskAt(index, t);
        reload();
    }

    public void addTask(@NonNull final String description) {
        final Task task = new Task(description, Instant.now(), false);
        repository.addTask(task);
        reload();
    }

    public void removeTaskAt(final int index) {
        repository.removeTaskAt(index);
        reload();
    }

    public void toggleCompletedAt(final int index) {
        final ArrayList<Task> tasks = repository.loadTasks();
        if (index < 0 || index >= tasks.size()) {
            return;
        }

        final Task t = tasks.get(index);
        final boolean current = t.Completed != null && t.Completed;
        t.Completed = !current;

        repository.updateTaskAt(index, t);
        reload();
    }

    private void reload() {
        final ArrayList<Task> tasks = repository.loadTasks();
        tasksLiveData.setValue(Collections.unmodifiableList(tasks));
    }

    public void setCompletedAt(final int index, final boolean completed) {
        final ArrayList<Task> tasks = repository.loadTasks();
        if (index < 0 || index >= tasks.size()) {
            return;
        }

        final Task t = tasks.get(index);
        t.Completed = completed;

        repository.updateTaskAt(index, t);
        reload();
    }

}
