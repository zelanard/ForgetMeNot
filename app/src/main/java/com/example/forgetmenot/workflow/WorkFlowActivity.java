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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.forgetmenot.BaseActivity;
import com.example.forgetmenot.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class WorkFlowActivity extends BaseActivity {

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBaseContentView(R.layout.work_flow);

        // Ensure notification channel exists (API 26+ requirement)
        NotificationChannels.ensureChannels(this);

        viewModel = new ViewModelProvider(this).get(WorkflowViewModel.class);

        tvStatus = findViewById(R.id.tvStatus);
        tvSelectedPreview = findViewById(R.id.tvSelectedPreview);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etIntendedHours = findViewById(R.id.etIntendedHours);
        etIntendedMinutes = findViewById(R.id.etIntendedMinutes);
        btnToggleCheck = findViewById(R.id.btnToggleCheck);

        // Default selection to "now"
        final LocalDateTime now = LocalDateTime.now();
        selectedDate = now.toLocalDate();
        selectedTime = now.toLocalTime().withSecond(0).withNano(0);

        updateSelectedPreview();

        etDate.setOnClickListener(v -> pickDate());
        etTime.setOnClickListener(v -> pickTime());

        btnToggleCheck.setOnClickListener(v -> toggleCheck());

        // Observe open session state
        viewModel.getOpenSession().observe(this, open -> {
            final boolean checkedIn = (open != null && open.isOpen());
            tvStatus.setText(checkedIn ? getString(R.string.workflow_status_checked_in)
                    : getString(R.string.workflow_status_checked_out));
            btnToggleCheck.setText(checkedIn ? getString(R.string.workflow_action_check_out)
                    : getString(R.string.workflow_action_check_in));
        });
    }

    private void pickDate() {
        final LocalDate d = selectedDate != null ? selectedDate : LocalDate.now();
        new DatePickerDialog(
                this,
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
                this,
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
        final LocalTime t = selectedTime != null ? selectedTime : LocalTime.now().withSecond(0).withNano(0);

        etDate.setText(d.toString()); // yyyy-MM-dd
        etTime.setText(String.format("%02d:%02d", t.getHour(), t.getMinute()));

        tvSelectedPreview.setText(getString(R.string.workflow_selected_preview, d.toString(), etTime.getText()));
    }

    private void toggleCheck() {
        final LocalDate d = selectedDate != null ? selectedDate : LocalDate.now();
        final LocalTime t = selectedTime != null ? selectedTime : LocalTime.now().withSecond(0).withNano(0);

        final LocalDateTime ldt = LocalDateTime.of(d, t);
        final Instant when = ldt.atZone(ZoneId.systemDefault()).toInstant();

        // Determine intended minutes
        final int hours = parseIntSafe(etIntendedHours, 8, 1);
        final int mins  = parseIntSafe(etIntendedMinutes, 0, 0);
        final int intendedMinutes = Math.max(0, (hours * 60) + mins);

        // If open session exists -> check out, else check in
        final WorkSession open = viewModel.getOpenSession().getValue();
        final boolean checkedIn = (open != null && open.isOpen());

        if (checkedIn) {
            viewModel.checkOut(when);
            cancelWorkdayOverAlarm();
        } else {
            viewModel.checkIn(when, intendedMinutes);

            Instant end = when.plusSeconds(intendedMinutes * 60L);
            scheduleWorkdayOverAlarm(end, getString(R.string.workday_is_over));
        }
    }

    private static int parseIntSafe(@NonNull EditText et, int defaultValue, int minValue) {
        final CharSequence text = et.getText();
        if (text == null) return defaultValue;

        final String s = text.toString().trim();
        if (s.isEmpty()) return defaultValue;

        try {
            final int value = Integer.parseInt(s);
            return value < minValue ? defaultValue : value;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private void scheduleWorkdayOverAlarm(Instant endInstant, String message) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        // Android 12+ exact alarm restrictions: check and (optionally) send user to settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                // You can show your own dialog before sending them here.
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(i);
                // You may also choose to fall back to inexact alarms if desired.
                return;
            }
        }

        Intent intent = new Intent(this, WorkdayOverReceiver.class);
        intent.putExtra(WorkdayOverReceiver.EXTRA_MESSAGE, message);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                2001, // request code: keep stable so you can update/cancel
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = endInstant.toEpochMilli();

        // Use exact while idle so it still fires under Doze
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }

    private void cancelWorkdayOverAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, WorkdayOverReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
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
