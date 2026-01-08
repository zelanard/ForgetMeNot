package com.example.forgetmenot.habits;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HabitsViewModel extends AndroidViewModel {

    private final HabitRepository repository;
    private final HabitLogRepository logRepository;

    private final MutableLiveData<List<Habit>> habitsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> countTodayLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Boolean>> doneTodayLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> streakFiresLiveData = new MutableLiveData<>();

    public HabitsViewModel(@NonNull final Application application) {
        super(application);
        repository = new HabitRepository(application.getApplicationContext());
        logRepository = new HabitLogRepository(application.getApplicationContext());
        reload();
    }

    public LiveData<List<Habit>> getHabits() {
        return habitsLiveData;
    }

    public LiveData<Map<String, Integer>> getCountToday() {
        return countTodayLiveData;
    }

    public LiveData<Map<String, Boolean>> getDoneToday() {
        return doneTodayLiveData;
    }

    public LiveData<Map<String, Integer>> getStreakFires() { return streakFiresLiveData; }

    public void addHabit(
            @NonNull final String name, @NonNull final String description,
            @NonNull final EnumSet<Habit.Day> days, final int goal) {
        final Habit habit = new Habit(name, description, Instant.now(), days, goal);
        repository.addHabit(habit);
        reload();
    }

    public void removeHabitAt(final int index) {
        final ArrayList<Habit> habits = repository.loadHabits();
        if (index < 0 || index >= habits.size()) {
            return;
        }

        final Habit habit = habits.get(index);
        if (habit.Id != null && !habit.Id.trim().isEmpty()) {
            logRepository.deleteAllForHabit(habit.Id);
        }

        repository.removeHabitAt(index);
        reload();
    }

    public void incrementToday(@NonNull final String habitId) {
        final LocalDate today = LocalDate.now();
        logRepository.incrementForDate(habitId, today);
        reloadTodayMaps(); // refresh today state only
    }

    private void reload() {
        final ArrayList<Habit> habits = repository.loadHabits();
        habitsLiveData.setValue(Collections.unmodifiableList(habits));
        reloadTodayMapsWith(habits); // IMPORTANT: also populate counts/done at startup
    }

    private void reloadTodayMaps() {
        final ArrayList<Habit> habits = repository.loadHabits();
        reloadTodayMapsWith(habits);
    }

    private void reloadTodayMapsWith(@NonNull final ArrayList<Habit> habits) {
        final LocalDate today = LocalDate.now();

        final HashMap<String, Integer> countsFromRepo =
                logRepository.getCountsForDate(today);

        // Build full history lookup for streaks: habitId -> (date -> count)
        final ArrayList<HabitProgress> allProgress = logRepository.loadProgress();
        final HashMap<String, HashMap<LocalDate, Integer>> history = new HashMap<>();
        for (int i = 0; i < allProgress.size(); i++) {
            final HabitProgress p = allProgress.get(i);
            if (p == null || p.HabitId == null || p.HabitId.trim().isEmpty() || p.Date == null) continue;

            final HashMap<LocalDate, Integer> perDate =
                    history.containsKey(p.HabitId) ? history.get(p.HabitId) : new HashMap<>();

            perDate.put(p.Date, Math.max(0, p.Count));
            history.put(p.HabitId, perDate);
        }

        final HashMap<String, Integer> counts = new HashMap<>();
        final HashMap<String, Boolean> done = new HashMap<>();
        final HashMap<String, Integer> fires = new HashMap<>();

        for (int i = 0; i < habits.size(); i++) {
            final Habit h = habits.get(i);
            if (h.Id == null || h.Id.trim().isEmpty()) {
                continue;
            }

            final int goal = Math.max(1, h.Goal);

            final int c = countsFromRepo.containsKey(h.Id)
                    ? countsFromRepo.get(h.Id)
                    : 0;

            counts.put(h.Id, c);
            done.put(h.Id, c >= goal);

            // streak (scheduled days met) -> fires (per 7 streak days)
            final HashMap<LocalDate, Integer> perDate = history.containsKey(h.Id)
                    ? history.get(h.Id)
                    : new HashMap<>();

            final EnumSet<Habit.Day> sched = h.ScheduledDays != null
                    ? h.ScheduledDays
                    : EnumSet.noneOf(Habit.Day.class);

            final int streakDays = computeScheduledStreakDays(today, goal, sched, perDate);
            final int fireCount = Math.min(3, streakDays / 7);
            fires.put(h.Id, fireCount);
        }

        countTodayLiveData.setValue(Collections.unmodifiableMap(counts));
        doneTodayLiveData.setValue(Collections.unmodifiableMap(done));
        streakFiresLiveData.setValue(Collections.unmodifiableMap(fires));
    }

    public void updateHabit(
            @NonNull final String habitId, @NonNull final String name,
            @NonNull final String description, @NonNull final EnumSet<Habit.Day> days) {
        final ArrayList<Habit> habits = repository.loadHabits();
        Habit existing = null;

        for (int i = 0; i < habits.size(); i++) {
            final Habit h = habits.get(i);
            if (h.Id != null && h.Id.equals(habitId)) {
                existing = h;
                break;
            }
        }

        if (existing == null) {
            return;
        }

        // Preserve immutable fields / identity / goal / start date
        final Habit updated = new Habit(name, description,
                existing.StartDate != null ? existing.StartDate : Instant.now(),
                days,
                Math.max(1, existing.Goal));

        updated.Id = existing.Id;

        repository.updateHabit(updated);
        reload();
    }

    private static int computeScheduledStreakDays(
            @NonNull final java.time.LocalDate today, final int goal,
            @NonNull final java.util.EnumSet<Habit.Day> scheduledDays,
            @NonNull final java.util.Map<java.time.LocalDate, Integer> countsByDate ) {
        int streak = 0;
        java.time.LocalDate d = today;

        // Cap scan depth (1 year) to avoid pathological loops
        for (int i = 0; i < 366; i++) {
            final boolean scheduled = isScheduledOn(scheduledDays, d.getDayOfWeek());
            if (scheduled) {
                final int c = countsByDate.containsKey(d) ? Math.max(0, countsByDate.get(d)) : 0;
                if (c >= goal) {
                    streak++;
                } else {
                    break;
                }
            }
            // Skip non-scheduled days (do not break streak)
            d = d.minusDays(1);
        }

        return streak;
    }

    private static boolean isScheduledOn(
            @NonNull final java.util.EnumSet<Habit.Day> scheduledDays,
            @NonNull final java.time.DayOfWeek dow ) {
        final Habit.Day hd;
        switch (dow) {
            case MONDAY: hd = Habit.Day.Monday; break;
            case TUESDAY: hd = Habit.Day.Tuesday; break;
            case WEDNESDAY: hd = Habit.Day.Wednesday; break;
            case THURSDAY: hd = Habit.Day.Thursday; break;
            case FRIDAY: hd = Habit.Day.Friday; break;
            case SATURDAY: hd = Habit.Day.Saturday; break;
            case SUNDAY:
            default: hd = Habit.Day.Sunday; break;
        }
        return scheduledDays.contains(hd);
    }
}
