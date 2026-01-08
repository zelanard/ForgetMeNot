package com.example.forgetmenot.workflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.forgetmenot.R;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorkflowOvertimeFragment extends Fragment {

    // 37h/week across Mon–Fri => 7h24m/day => 444 minutes
    private static final int TARGET_MINUTES_PER_WEEKDAY = 37 * 60 / 5; // 444

    private WorkflowViewModel viewModel;

    private TextView tvWeekRange;
    private TextView tvProjection;
    private TextView tvBreakdown;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.workflow_overtime, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWeekRange = view.findViewById(R.id.tvWeekRange);
        tvProjection = view.findViewById(R.id.tvProjection);
        tvBreakdown = view.findViewById(R.id.tvBreakdown);

        viewModel = new ViewModelProvider(requireActivity()).get(WorkflowViewModel.class);

        viewModel.getSessions().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(@Nullable final List<WorkSession> sessions) {
        final ZoneId zone = ZoneId.systemDefault();
        final LocalDate today = LocalDate.now(zone);

        final LocalDate weekStart =
                today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        final LocalDate weekEnd = weekStart.plusDays(6);

        tvWeekRange.setText(getString(
                R.string.workflow_overtime_week_range,
                weekStart.toString(),
                weekEnd.toString()
        ));

        // Stable order: Mon..Sun
        final LinkedHashMap<LocalDate, Integer> workedMinutesByDay = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            workedMinutesByDay.put(weekStart.plusDays(i), 0);
        }

        if (sessions != null) {
            for (int i = 0; i < sessions.size(); i++) {
                final WorkSession s = sessions.get(i);
                if (s == null || s.CheckIn == null) continue;

                final Instant start = s.CheckIn;
                final Instant end = (s.CheckOut != null) ? s.CheckOut : Instant.now();

                final Instant weekStartInstant =
                        weekStart.atStartOfDay(zone).toInstant();
                final Instant weekEndExclusiveInstant =
                        weekEnd.plusDays(1).atStartOfDay(zone).toInstant();

                if (end.isBefore(weekStartInstant)
                        || !start.isBefore(weekEndExclusiveInstant)) {
                    continue;
                }

                // Split session into day buckets
                for (final Map.Entry<LocalDate, Integer> e : workedMinutesByDay.entrySet()) {
                    final LocalDate d = e.getKey();

                    final Instant dayStart = d.atStartOfDay(zone).toInstant();
                    final Instant dayEnd = d.plusDays(1).atStartOfDay(zone).toInstant();

                    final long overlapMillis = overlapMillis(start, end, dayStart, dayEnd);
                    if (overlapMillis <= 0) continue;

                    final int addMinutes = (int) (overlapMillis / 60000L);
                    workedMinutesByDay.put(d, e.getValue() + addMinutes);
                }
            }
        }

        final StringBuilder sb = new StringBuilder(512);
        int runningAccumMinutes = 0;

        for (final Map.Entry<LocalDate, Integer> e : workedMinutesByDay.entrySet()) {
            final LocalDate d = e.getKey();
            final int actualWorked = e.getValue();
            final int target = targetMinutesFor(d);

            final boolean isFuture = d.isAfter(today);

            final int displayWorked;
            final int dailyDelta;

            if (isFuture) {
                // Projection: assume full target on future weekdays
                displayWorked = target;
                dailyDelta = 0;
            } else {
                displayWorked = actualWorked;
                dailyDelta = actualWorked - target;
            }

            runningAccumMinutes += dailyDelta;

            sb.append(shortDayName(d.getDayOfWeek()))
                    .append(" ")
                    .append(d.toString())
                    .append(isFuture ? " (assumed)" : "")
                    .append("\n")
                    .append("  Worked: ")
                    .append(formatMinutes(displayWorked))
                    .append("\n")
                    .append("  Daily over/under: ")
                    .append(formatSignedMinutes(dailyDelta))
                    .append("\n")
                    .append("  Accumulated: ")
                    .append(formatSignedMinutes(runningAccumMinutes))
                    .append("\n\n");
        }

        tvBreakdown.setText(sb.toString().trim());

        // Final accumulated value already reflects the projection
        tvProjection.setText(getString(
                R.string.workflow_overtime_projection,
                formatSignedMinutes(runningAccumMinutes)
        ));
    }

    private static int targetMinutesFor(@NonNull final LocalDate date) {
        final DayOfWeek dow = date.getDayOfWeek();
        switch (dow) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                return TARGET_MINUTES_PER_WEEKDAY;
            default:
                return 0; // weekend
        }
    }

    /**
     * Compute accumulated over/under through 'today':
     * - past days: actual worked vs target
     * - today: actual so far vs target
     * - future days: assumed target worked (delta 0), so excluded
     */
    private static int computeRunningThrough(
            @NonNull final LinkedHashMap<LocalDate, Integer> workedByDay,
            @NonNull final LocalDate today
    ) {
        int total = 0;
        for (final Map.Entry<LocalDate, Integer> e : workedByDay.entrySet()) {
            final LocalDate d = e.getKey();
            if (d.isAfter(today)) continue; // future days assumed full => delta 0
            total += (e.getValue() - targetMinutesFor(d));
        }
        return total;
    }

    private static long overlapMillis(
            @NonNull final Instant aStart,
            @NonNull final Instant aEnd,
            @NonNull final Instant bStart,
            @NonNull final Instant bEnd
    ) {
        final long start = Math.max(aStart.toEpochMilli(), bStart.toEpochMilli());
        final long end = Math.min(aEnd.toEpochMilli(), bEnd.toEpochMilli());
        return Math.max(0L, end - start);
    }

    private static String formatMinutes(final int minutes) {
        final int h = minutes / 60;
        final int m = Math.abs(minutes % 60);
        return String.format("%dh %02dm", h, m);
    }

    private static String formatSignedMinutes(final int minutes) {
        final String sign = (minutes >= 0) ? "+" : "-";
        final int abs = Math.abs(minutes);
        final int h = abs / 60;
        final int m = abs % 60;
        return String.format("%s%dh %02dm", sign, h, m);
    }

    private static String shortDayName(@NonNull final DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Mon";
            case TUESDAY: return "Tue";
            case WEDNESDAY: return "Wed";
            case THURSDAY: return "Thu";
            case FRIDAY: return "Fri";
            case SATURDAY: return "Sat";
            case SUNDAY: return "Sun";
            default: return "";
        }
    }
}
