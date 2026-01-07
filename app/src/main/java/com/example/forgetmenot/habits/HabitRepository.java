package com.example.forgetmenot.habits;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;

public final class HabitRepository {

    private static final String PREFS_NAME = "habits_prefs";
    private static final String KEY_HABITS_JSON = "habits_json";

    private static final String K_ID = "id";
    private static final String K_NAME = "name";
    private static final String K_DESCRIPTION = "description";
    private static final String K_START_DATE = "startDate";
    private static final String K_DAYS = "days";
    private static final String K_GOAL = "goal";

    private final SharedPreferences prefs;

    public HabitRepository(@NonNull final Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public ArrayList<Habit> loadHabits() {
        final String json = prefs.getString(KEY_HABITS_JSON, "[]");
        final ArrayList<Habit> result = new ArrayList<>();
        boolean changed = false;

        try {
            final JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                final JSONObject obj = array.optJSONObject(i);
                if (obj == null) {
                    continue;
                }

                final Habit habit = fromJson(obj);
                if (habit != null) {
                    // Migration: if habit had no id in JSON, fromJson generated one -> persist it.
                    if (habit.Id == null || habit.Id.trim().isEmpty()) {
                        habit.Id = java.util.UUID.randomUUID().toString();
                        changed = true;
                    }
                    result.add(habit);

                    // Also detect the "missing id in JSON" case (most important)
                    final String rawId = obj.optString(K_ID, "");
                    if (rawId == null || rawId.trim().isEmpty()) {
                        changed = true;
                    }
                }
            }
        } catch (final JSONException ignored) {
            // If corrupted, treat as empty
        }

        // Persist generated IDs so they are stable across loads
        if (changed) {
            saveHabits(result);
        }

        return result;
    }

    public void addHabit(@NonNull final Habit habit) {
        final ArrayList<Habit> habits = loadHabits();
        habits.add(habit);
        saveHabits(habits);
    }

    public void removeHabitAt(final int index) {
        final ArrayList<Habit> habits = loadHabits();
        if (index < 0 || index >= habits.size()) {
            return;
        }
        habits.remove(index);
        saveHabits(habits);
    }

    private void saveHabits(@NonNull final ArrayList<Habit> habits) {
        final JSONArray array = new JSONArray();
        for (int i = 0; i < habits.size(); i++) {
            array.put(toJson(habits.get(i)));
        }
        prefs.edit().putString(KEY_HABITS_JSON, array.toString()).apply();
    }

    @NonNull
    private static JSONObject toJson(@NonNull final Habit habit) {
        final JSONObject obj = new JSONObject();
        try {
            obj.put(K_ID, habit.Id != null ? habit.Id : "");
            obj.put(K_NAME, habit.Name != null ? habit.Name : "");
            obj.put(K_DESCRIPTION, habit.Description != null ? habit.Description : "");
            obj.put(K_START_DATE, habit.StartDate != null ? habit.StartDate.toString() : Instant.EPOCH.toString());
            obj.put(K_GOAL, Math.max(1, habit.Goal));

            final JSONArray daysArr = new JSONArray();
            if (habit.ScheduledDays != null) {
                for (final Habit.Day d : habit.ScheduledDays) {
                    daysArr.put(d.name());
                }
            }
            obj.put(K_DAYS, daysArr);

        } catch (final JSONException ignored) {
        }
        return obj;
    }

    private static Habit fromJson(@NonNull final JSONObject obj) {
        try {
            final String id = obj.optString(K_ID, "");
            final String name = obj.optString(K_NAME, "");
            final String description = obj.optString(K_DESCRIPTION, "");
            final String startDateText = obj.optString(K_START_DATE, Instant.EPOCH.toString());
            final int goal = Math.max(1, obj.optInt(K_GOAL, 1));

            final Instant startDate;
            try {
                startDate = Instant.parse(startDateText);
            } catch (final Exception ignored) {
                return null;
            }

            final EnumSet<Habit.Day> days = EnumSet.noneOf(Habit.Day.class);
            final JSONArray daysArr = obj.optJSONArray(K_DAYS);
            if (daysArr != null) {
                for (int i = 0; i < daysArr.length(); i++) {
                    final String d = daysArr.optString(i, null);
                    if (d == null) {
                        continue;
                    }
                    try {
                        days.add(Habit.Day.valueOf(d));
                    } catch (final IllegalArgumentException ignored) {
                    }
                }
            }
            final Habit h = new Habit(name, description, startDate, days, goal);
            if (!id.isEmpty() && !id.trim().isEmpty()) {
                h.Id = id;
            } else {
                // generate if missing
                h.Id = java.util.UUID.randomUUID().toString();
            }
            return h;
        } catch (final Exception ignored) {
            return null;
        }
    }

    public void updateHabit(@NonNull final Habit updated) {
        if (updated.Id == null || updated.Id.trim().isEmpty()) {
            return;
        }

        final ArrayList<Habit> habits = loadHabits();
        for (int i = 0; i < habits.size(); i++) {
            final Habit h = habits.get(i);
            if (h.Id != null && h.Id.equals(updated.Id)) {
                habits.set(i, updated);
                saveHabits(habits);
                return;
            }
        }
    }
}
