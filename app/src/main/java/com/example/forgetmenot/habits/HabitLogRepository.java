package com.example.forgetmenot.habits;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public final class HabitLogRepository {

    private static final String PREFS_NAME = "habit_logs_prefs";
    private static final String KEY_LOGS_JSON = "habit_logs_json";

    private static final String K_HABIT_ID = "habitId";
    private static final String K_DATE = "date";   // yyyy-MM-dd
    private static final String K_COUNT = "count"; // NEW

    private final SharedPreferences prefs;

    public HabitLogRepository(@NonNull final Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public ArrayList<HabitProgress> loadProgress() {
        final String json = prefs.getString(KEY_LOGS_JSON, "[]");
        final ArrayList<HabitProgress> result = new ArrayList<>();

        try {
            final JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                final JSONObject obj = array.optJSONObject(i);
                if (obj == null) continue;

                final String habitId = obj.optString(K_HABIT_ID, null);
                final String dateText = obj.optString(K_DATE, null);
                final int count = obj.optInt(K_COUNT, 0);

                if (habitId == null || dateText == null) continue;

                try {
                    final LocalDate date = LocalDate.parse(dateText);
                    result.add(new HabitProgress(habitId, date, count));
                } catch (final Exception ignored) {
                }
            }
        } catch (final JSONException ignored) {
        }

        return result;
    }

    @NonNull
    public HashMap<String, Integer> getCountsForDate(@NonNull final LocalDate date) {
        final ArrayList<HabitProgress> all = loadProgress();
        final HashMap<String, Integer> map = new HashMap<>();

        for (int i = 0; i < all.size(); i++) {
            final HabitProgress p = all.get(i);
            if (date.equals(p.Date) && p.HabitId != null && !p.HabitId.trim().isEmpty()) {
                map.put(p.HabitId, Math.max(0, p.Count));
            }
        }
        return map;
    }

    public int incrementForDate(@NonNull final String habitId, @NonNull final LocalDate date) {
        final ArrayList<HabitProgress> all = loadProgress();

        for (int i = 0; i < all.size(); i++) {
            final HabitProgress p = all.get(i);
            if (habitId.equals(p.HabitId) && date.equals(p.Date)) {
                p.Count = p.Count + 1;
                saveProgress(all);
                return p.Count;
            }
        }

        // If no entry exists yet for today, create one with count=1
        all.add(new HabitProgress(habitId, date, 1));
        saveProgress(all);
        return 1;
    }

    public void setCountForDate(@NonNull final String habitId, @NonNull final LocalDate date, final int count) {
        final int safe = Math.max(0, count);
        final ArrayList<HabitProgress> all = loadProgress();

        for (int i = 0; i < all.size(); i++) {
            final HabitProgress p = all.get(i);
            if (habitId.equals(p.HabitId) && date.equals(p.Date)) {
                if (safe == 0) {
                    all.remove(i);
                } else {
                    p.Count = safe;
                }
                saveProgress(all);
                return;
            }
        }

        if (safe > 0) {
            all.add(new HabitProgress(habitId, date, safe));
            saveProgress(all);
        }
    }

    private void saveProgress(@NonNull final ArrayList<HabitProgress> all) {
        final JSONArray array = new JSONArray();

        for (int i = 0; i < all.size(); i++) {
            final HabitProgress p = all.get(i);
            final JSONObject obj = new JSONObject();
            try {
                obj.put(K_HABIT_ID, p.HabitId);
                obj.put(K_DATE, p.Date.toString());
                obj.put(K_COUNT, p.Count);
                array.put(obj);
            } catch (final JSONException ignored) {
            }
        }

        prefs.edit().putString(KEY_LOGS_JSON, array.toString()).apply();
    }

    public void deleteAllForHabit(@NonNull final String habitId) {
        final ArrayList<HabitProgress> all = loadProgress();
        for (int i = all.size() - 1; i >= 0; i--) {
            if (habitId.equals(all.get(i).HabitId)) {
                all.remove(i);
            }
        }
        saveProgress(all);
    }

}
