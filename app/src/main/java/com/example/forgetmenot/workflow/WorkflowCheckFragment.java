package com.example.forgetmenot.workflow;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.forgetmenot.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Tab 1: retains the existing check-in/out functionality.
 */
public final class WorkflowCheckFragment extends Fragment {

    private WorkflowViewModel viewModel;

    private TextView tvStatus;
    private TextView tvSelectedPreview;
    private EditText etDate;
    private EditText etTime;
    private EditText etIntendedHours;
    private EditText etIntendedMinutes;
    private Button btnToggleCheck;

    private LocalDate selectedDate;
    private LocalTime selectedTime;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.work_flow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ensure notification channel exists (API 26+ requirement)
        NotificationChannels.ensureChannels(requireContext());

        // Activity-scoped VM so tabs can share it later
        viewModel = new ViewModelProvider(requireActivity()).get(WorkflowViewModel.class);

        tvStatus = view.findViewById(R.id.tvStatus);
        tvSelectedPreview = view.findViewById(R.id.tvSelectedPreview);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        etIntendedHours = view.findViewById(R.id.etIntendedHours);
        etIntendedMinutes = view.findViewById(R.id.etIntendedMinutes);
        btnToggleCheck = view.findViewById(R.id.btnToggleCheck);

        // Default selection to "now"
        final LocalDateTime now = LocalDateTime.now();
        selectedDate = now.toLocalDate();
        selectedTime = now.toLocalTime().withSecond(0).withNano(0);

        updateSelectedPreview();

        etDate.setOnClickListener(v -> pickDate());
        etTime.setOnClickListener(v -> pickTime());
        btnToggleCheck.setOnClickListener(v -> toggleCheck());

        // Observe open session state
        viewModel.getOpenSession().observe(getViewLifecycleOwner(), open -> {
            final boolean checkedIn = (open != null && open.isOpen());
            tvStatus.setText(checkedIn
                    ? getString(R.string.workflow_status_checked_in)
                    : getString(R.string.workflow_status_checked_out));
            btnToggleCheck.setText(checkedIn
                    ? getString(R.string.workflow_action_check_out)
                    : getString(R.string.workflow_action_check_in));
        });
    }

    private void pickDate() {
        final LocalDate d = selectedDate != null ? selectedDate : LocalDate.now();
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateSelectedPreview();
                },
                d.getYear(),
                d.getMonthValue() - 1,
                d.getDayOfMonth()
        ).show();
    }

    private void pickTime() {
        final LocalTime t = selectedTime != null ? selectedTime : LocalTime.now();
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime = LocalTime.of(hourOfDay, minute);
                    updateSelectedPreview();
                },
                t.getHour(),
                t.getMinute(),
                true
        ).show();
    }

    private void updateSelectedPreview() {
        final LocalDate d = selectedDate != null ? selectedDate : LocalDate.now();
        final LocalTime t = selectedTime != null
                ? selectedTime
                : LocalTime.now().withSecond(0).withNano(0);

        etDate.setText(d.toString()); // yyyy-MM-dd
        etTime.setText(String.format("%02d:%02d", t.getHour(), t.getMinute()));

        tvSelectedPreview.setText(getString(
                R.string.workflow_selected_preview,
                d.toString(),
                etTime.getText()
        ));
    }

    private void toggleCheck() {
        final LocalDate d = selectedDate != null ? selectedDate : LocalDate.now();
        final LocalTime t = selectedTime != null
                ? selectedTime
                : LocalTime.now().withSecond(0).withNano(0);

        final LocalDateTime ldt = LocalDateTime.of(d, t);
        final Instant when = ldt.atZone(ZoneId.systemDefault()).toInstant();

        // Determine intended minutes
        final int hours = parseIntSafe(etIntendedHours, 8, 1);
        final int mins = parseIntSafe(etIntendedMinutes, 0, 0);
        final int intendedMinutes = Math.max(0, (hours * 60) + mins);

        // If open session exists -> check out, else check in
        final WorkSession open = viewModel.getOpenSession().getValue();
        final boolean checkedIn = (open != null && open.isOpen());

        if (checkedIn) {
            viewModel.checkOut(when);
            cancelWorkdayOverAlarm();
        } else {
            viewModel.checkIn(when, intendedMinutes);

            final Instant end = when.plusSeconds(intendedMinutes * 60L);
            scheduleWorkdayOverAlarm(end, getString(R.string.workday_is_over));
        }
    }

    private static int parseIntSafe(
            @NonNull final EditText et,
            final int defaultValue,
            final int minValue
    ) {
        final CharSequence text = et.getText();
        if (text == null) return defaultValue;

        final String s = text.toString().trim();
        if (s.isEmpty()) return defaultValue;

        try {
            final int value = Integer.parseInt(s);
            return value < minValue ? defaultValue : value;
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private void scheduleWorkdayOverAlarm(@NonNull final Instant endInstant, @NonNull final String message) {
        final Context context = requireContext();
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        // Android 12+ exact alarm restrictions: check and (optionally) send user to settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                final Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(i);
                return;
            }
        }

        final Intent intent = new Intent(context, WorkdayOverReceiver.class);
        intent.putExtra(WorkdayOverReceiver.EXTRA_MESSAGE, message);

        final PendingIntent pi = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        final long triggerAtMillis = endInstant.toEpochMilli();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }

    private void cancelWorkdayOverAlarm() {
        final Context context = requireContext();
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        final Intent intent = new Intent(context, WorkdayOverReceiver.class);

        final PendingIntent pi = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }
}
