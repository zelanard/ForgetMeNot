package com.example.forgetmenot.habits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forgetmenot.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HabitsStatisticsFragment extends Fragment {

    private HabitsStatisticsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.habits_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recycler = view.findViewById(R.id.recyclerHabitStats);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HabitsStatisticsAdapter();
        recycler.setAdapter(adapter);

        loadAndShowStats();
    }

    private void loadAndShowStats() {
        final HabitRepository habitRepo = new HabitRepository(requireContext());
        final HabitLogRepository logRepo = new HabitLogRepository(requireContext());

        final ArrayList<Habit> habits = habitRepo.loadHabits();
        final ArrayList<HabitProgress> progress = logRepo.loadProgress();

        // Build lookup: habitId -> (date -> count)
        final HashMap<String, HashMap<LocalDate, Integer>> counts = new HashMap<>();
        for (int i = 0; i < progress.size(); i++) {
            final HabitProgress p = progress.get(i);
            if (p == null || p.HabitId == null || p.HabitId.trim().isEmpty() || p.Date == null) {
                continue;
            }

            final HashMap<LocalDate, Integer> perDate =
                    counts.containsKey(p.HabitId) ? counts.get(p.HabitId) : new HashMap<>();

            perDate.put(p.Date, Math.max(0, p.Count));
            counts.put(p.HabitId, perDate);
        }

        final LocalDate today = LocalDate.now();
        final LocalDate from = today.minusDays(6); // 7-day window including today

        final ArrayList<HabitStatItem> items = new ArrayList<>();

        for (int i = 0; i < habits.size(); i++) {
            final Habit h = habits.get(i);
            if (h == null || h.Id == null || h.Id.trim().isEmpty()) {
                continue;
            }

            final String habitId = h.Id;
            final int goal = Math.max(1, h.Goal);

            final HashMap<LocalDate, Integer> perDate = counts.containsKey(habitId)
                    ? counts.get(habitId)
                    : new HashMap<>();

            int totalCompletions7 = 0;
            int scheduledDays7 = 0;
            int daysMetGoal7 = 0;

            LocalDate d = from;
            while (!d.isAfter(today)) {
                final boolean isScheduled = isScheduledOn(h.ScheduledDays, d.getDayOfWeek());
                final int c = perDate.containsKey(d) ? perDate.get(d) : 0;

                totalCompletions7 += c;

                if (isScheduled) {
                    scheduledDays7++;
                    if (c >= goal) {
                        daysMetGoal7++;
                    }
                }

                d = d.plusDays(1);
            }

            final int streak = computeStreak(today, goal, h.ScheduledDays, perDate);

            final HabitStatItem item = new HabitStatItem(
                    safeText(h.Name),
                    totalCompletions7,
                    daysMetGoal7,
                    scheduledDays7,
                    streak,
                    goal
            );

            items.add(item);
        }

        adapter.submit(items);
    }

    private static int computeStreak(
            @NonNull final LocalDate today,
            final int goal,
            @Nullable final EnumSet<Habit.Day> scheduledDays,
            @NonNull final Map<LocalDate, Integer> perDate
    ) {
        // Streak: consecutive scheduled days met goal, going backwards from today.
        // Non-scheduled days are skipped (do not break streak).
        int streak = 0;

        // Safety cap so we don’t loop forever if something is weird.
        LocalDate d = today;
        for (int i = 0; i < 365; i++) {
            final boolean scheduled = isScheduledOn(scheduledDays, d.getDayOfWeek());
            if (scheduled) {
                final int c = perDate.containsKey(d) ? Math.max(0, perDate.get(d)) : 0;
                if (c >= goal) {
                    streak++;
                } else {
                    break;
                }
            }
            d = d.minusDays(1);
        }

        return streak;
    }

    private static boolean isScheduledOn(@Nullable final EnumSet<Habit.Day> days, @NonNull final DayOfWeek dow) {
        if (days == null || days.isEmpty()) {
            return false;
        }

        // Map java.time.DayOfWeek -> your Habit.Day enum
        final Habit.Day hd;
        switch (dow) {
            case MONDAY:
                hd = Habit.Day.Monday; break;
            case TUESDAY:
                hd = Habit.Day.Tuesday; break;
            case WEDNESDAY:
                hd = Habit.Day.Wednesday; break;
            case THURSDAY:
                hd = Habit.Day.Thursday; break;
            case FRIDAY:
                hd = Habit.Day.Friday; break;
            case SATURDAY:
                hd = Habit.Day.Saturday; break;
            case SUNDAY:
            default:
                hd = Habit.Day.Sunday; break;
        }

        return days.contains(hd);
    }

    @NonNull
    private static String safeText(@Nullable final String s) {
        return s != null ? s : "";
    }
}
