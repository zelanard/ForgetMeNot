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

    public void addHabit(@NonNull final String name, @NonNull final String description,
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

        final HashMap<String, Integer> counts = new HashMap<>();
        final HashMap<String, Boolean> done = new HashMap<>();

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
        }

        countTodayLiveData.setValue(Collections.unmodifiableMap(counts));
        doneTodayLiveData.setValue(Collections.unmodifiableMap(done));
    }

    public void updateHabit(@NonNull final String habitId, @NonNull final String name,
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
}
