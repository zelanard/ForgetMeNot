package com.example.forgetmenot.todo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;

public final class TaskRepository {

    private static final String PREFS_NAME = "todo_prefs";
    private static final String KEY_TASKS_JSON = "tasks_json";

    // JSON keys (keep stable)
    private static final String K_DESCRIPTION = "description";
    private static final String K_CREATION_DATE = "creationDate";
    private static final String K_COMPLETED = "completed";
    private final SharedPreferences prefs;

    public TaskRepository(@NonNull final Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public ArrayList<Task> loadTasks() {
        final String json = prefs.getString(KEY_TASKS_JSON, "[]");
        final ArrayList<Task> result = new ArrayList<>();

        try {
            final JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                final JSONObject obj = array.optJSONObject(i);
                if (obj == null) {
                    continue;
                }

                final Task task = fromJson(obj);
                if (task != null) {
                    result.add(task);
                }
            }
        } catch (final JSONException ignored) {
            // If corrupted, treat as empty
        }

        return result;
    }

    public void addTask(@NonNull final Task task) {
        final ArrayList<Task> tasks = loadTasks();
        tasks.add(task);
        saveTasks(tasks);
    }

    public void removeTaskAt(final int index) {
        final ArrayList<Task> tasks = loadTasks();
        if (index < 0 || index >= tasks.size()) {
            return;
        }
        tasks.remove(index);
        saveTasks(tasks);
    }

    public void updateTaskAt(final int index, @NonNull final Task updated) {
        final ArrayList<Task> tasks = loadTasks();
        if (index < 0 || index >= tasks.size()) {
            return;
        }
        tasks.set(index, updated);
        saveTasks(tasks);
    }

    private void saveTasks(@NonNull final ArrayList<Task> tasks) {
        final JSONArray array = new JSONArray();
        for (int i = 0; i < tasks.size(); i++) {
            array.put(toJson(tasks.get(i)));
        }
        prefs.edit().putString(KEY_TASKS_JSON, array.toString()).apply();
    }

    @NonNull
    private static JSONObject toJson(@NonNull final Task task) {
        final JSONObject obj = new JSONObject();
        try {
            obj.put(K_DESCRIPTION, task.Description != null ? task.Description : "");
            // Store Instant as ISO-8601 text
            obj.put(K_CREATION_DATE, task.CreationDate != null ? task.CreationDate.toString() : Instant.EPOCH.toString());
            obj.put(K_COMPLETED, task.Completed != null && task.Completed);
        } catch (final JSONException ignored) {
            // No-op: return whatever was built so far
        }
        return obj;
    }

    private static Task fromJson(@NonNull final JSONObject obj) {
        try {
            final String description = obj.optString(K_DESCRIPTION, "");
            final String creationDateText = obj.optString(K_CREATION_DATE, Instant.EPOCH.toString());
            final boolean completed = obj.optBoolean(K_COMPLETED, false);

            final Instant creationDate;
            try {
                creationDate = Instant.parse(creationDateText);
            } catch (final Exception ignored) {
                // Fallback if date is malformed
                return new Task(description, Instant.EPOCH, completed);
            }

            return new Task(description, creationDate, completed);
        } catch (final Exception ignored) {
            return null;
        }
    }
}
