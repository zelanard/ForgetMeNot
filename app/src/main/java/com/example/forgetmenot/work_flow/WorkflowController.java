package com.example.forgetmenot.work_flow;

import com.example.forgetmenot.R;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.forgetmenot.settings.SettingsReader;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class WorkflowController {

    private static final String PREFS = "workflow_prefs";
    private static final String KEY_CHECKED_IN = "checked_in";

    private final Context context;

    private final TextView tvStatus;
    private final TextView tvPreview;

    private final EditText etDate;
    private final EditText etTime;
    private final EditText etMinutes;
    private final EditText etHours;
    private final Button btnToggle;

    private final Calendar selected;
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;

    public WorkflowController(@NonNull View root) {
        this.context = root.getContext(); // ✅ UI-safe context

        tvStatus = root.findViewById(R.id.tvStatus);
        tvPreview = root.findViewById(R.id.tvSelectedPreview);

        etDate = root.findViewById(R.id.etDate);
        etTime = root.findViewById(R.id.etTime);
        etMinutes = root.findViewById(R.id.etIntendedMinutes);
        etHours = root.findViewById(R.id.etIntendedHours);

        btnToggle = root.findViewById(R.id.btnToggleCheck);

        selected = Calendar.getInstance();
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);

        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        wireUi();
    }

    private String GetMinutes() {
        int minutes = 0;
        int hours = 0;

        String minutesText = etMinutes.getText().toString().trim();
        String hoursText   = etHours.getText().toString().trim();

        if (!minutesText.isEmpty()) {
            minutes = Integer.parseInt(minutesText);
        }

        if (!hoursText.isEmpty()) {
            hours = Integer.parseInt(hoursText);
        }

        int totalMinutes = minutes + (hours * 60);
        return String.valueOf(totalMinutes);
    }

    private void wireUi() {
        refreshDateTimeFields();

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        btnToggle.setOnClickListener(v -> toggleCheck());

        renderState();
        updatePreview();
    }

    private void showDatePicker() {
        final int year = selected.get(Calendar.YEAR);
        final int month = selected.get(Calendar.MONTH);
        final int day = selected.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(context, (view, y, m, d) -> {
            selected.set(Calendar.YEAR, y);
            selected.set(Calendar.MONTH, m);
            selected.set(Calendar.DAY_OF_MONTH, d);
            normalizeSelected();

            refreshDateTimeFields();
            updatePreview();
        }, year, month, day).show();
    }

    private void showTimePicker() {
        final int hour = selected.get(Calendar.HOUR_OF_DAY);
        final int minute = selected.get(Calendar.MINUTE);

        new TimePickerDialog(context, (tp, h, min) -> {
            selected.set(Calendar.HOUR_OF_DAY, h);
            selected.set(Calendar.MINUTE, min);
            normalizeSelected();

            refreshDateTimeFields();
            updatePreview();
        }, hour, minute, true).show();
    }

    private void normalizeSelected() {
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);
    }

    private void refreshDateTimeFields() {
        etDate.setText(dateFormat.format(selected.getTime()));
        etTime.setText(timeFormat.format(selected.getTime()));
    }

    private void toggleCheck() {
        final boolean checkedIn = context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_CHECKED_IN, false);

        final long selectedMillis = getSelectedMillis();
        final long roundedMillis = TimeRounding.roundToNearest15MinutesMillis(selectedMillis);

        if (!checkedIn) {
            final int intendedMinutes = parseIntendedMinutesOrThrow();

            try {
                CsvStore.appendCheckInRow(context, roundedMillis);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            if (SettingsReader.isWorkEndNotificationEnabled(context)) {
                final long endMillis = roundedMillis + (intendedMinutes * 60L * 1000L);
                WorkEndAlarm.schedule(context, endMillis);
            }

            setCheckedIn(true);
        } else {
            try {
                CsvStore.updateLastRowCheckOut(context, roundedMillis);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            WorkEndAlarm.cancel(context);
            setCheckedIn(false);
        }

        renderState();
        updatePreview();
    }

    private long getSelectedMillis() {
        return selected.getTimeInMillis();
    }

    private void updatePreview() {
        final long selectedMillis = getSelectedMillis();
        final long roundedMillis = TimeRounding.roundToNearest15MinutesMillis(selectedMillis);

        tvPreview.setText(
                "Selected: " + CsvStore.formatLocal(selectedMillis) +
                        " | Rounded: " + CsvStore.formatLocal(roundedMillis)
        );
    }

    private void renderState() {
        final boolean checkedIn = context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_CHECKED_IN, false);

        tvStatus.setText(checkedIn ? "Status: Checked in" : "Status: Checked out");
        btnToggle.setText(checkedIn ? "Check out" : "Check in");

        etMinutes.setEnabled(!checkedIn);
        etHours.setEnabled(!checkedIn);
        etDate.setEnabled(!checkedIn);
        etTime.setEnabled(!checkedIn);
    }

    private void setCheckedIn(final boolean checkedIn) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_CHECKED_IN, checkedIn)
                .apply();
    }

    private int parseIntendedMinutesOrThrow() {
        final CharSequence text = GetMinutes();
        final String s = text.toString().trim();
        if (s.isEmpty()) throw new IllegalStateException("Intended minutes is required on check-in.");
        final int minutes = Integer.parseInt(s);
        if (minutes <= 0) throw new IllegalStateException("Intended minutes must be > 0.");
        return minutes;
    }
}
