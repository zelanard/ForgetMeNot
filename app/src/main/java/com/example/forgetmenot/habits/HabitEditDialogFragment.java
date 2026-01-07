package com.example.forgetmenot.habits;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.forgetmenot.R;

import java.util.EnumSet;

public final class HabitEditDialogFragment extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_DESC = "desc";
    private static final String ARG_DAYS = "days"; // comma-separated enum names

    public static HabitEditDialogFragment newInstance(@NonNull final Habit habit) {
        final HabitEditDialogFragment f = new HabitEditDialogFragment();

        final Bundle b = new Bundle();
        b.putString(ARG_ID, habit.Id != null ? habit.Id : "");
        b.putString(ARG_NAME, habit.Name != null ? habit.Name : "");
        b.putString(ARG_DESC, habit.Description != null ? habit.Description : "");
        b.putString(ARG_DAYS, serializeDays(habit.ScheduledDays));
        f.setArguments(b);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final View v = requireActivity().getLayoutInflater().inflate(R.layout.habit_edit_dialog, null);

        final EditText etName = v.findViewById(R.id.etEditName);
        final EditText etDesc = v.findViewById(R.id.etEditDescription);

        final CheckBox cbMon = v.findViewById(R.id.cbEditMon);
        final CheckBox cbTue = v.findViewById(R.id.cbEditTue);
        final CheckBox cbWed = v.findViewById(R.id.cbEditWed);
        final CheckBox cbThu = v.findViewById(R.id.cbEditThu);
        final CheckBox cbFri = v.findViewById(R.id.cbEditFri);
        final CheckBox cbSat = v.findViewById(R.id.cbEditSat);
        final CheckBox cbSun = v.findViewById(R.id.cbEditSun);

        final Bundle args = getArguments() != null ? getArguments() : new Bundle();
        final String habitId = args.getString(ARG_ID, "");
        final String name = args.getString(ARG_NAME, "");
        final String desc = args.getString(ARG_DESC, "");
        final EnumSet<Habit.Day> days = deserializeDays(args.getString(ARG_DAYS, ""));

        etName.setText(name);
        etDesc.setText(desc);

        cbMon.setChecked(days.contains(Habit.Day.Monday));
        cbTue.setChecked(days.contains(Habit.Day.Tuesday));
        cbWed.setChecked(days.contains(Habit.Day.Wednesday));
        cbThu.setChecked(days.contains(Habit.Day.Thursday));
        cbFri.setChecked(days.contains(Habit.Day.Friday));
        cbSat.setChecked(days.contains(Habit.Day.Saturday));
        cbSun.setChecked(days.contains(Habit.Day.Sunday));

        return new AlertDialog.Builder(requireContext())
                .setTitle("Edit Habit")
                .setView(v)
                .setNegativeButton("Cancel", (d, which) -> {
                    // no-op
                })
                .setPositiveButton("Save", null) // override after show to validate
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return;
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            final View v = dialog.findViewById(android.R.id.content);
            if (v == null) {
                dismiss();
                return;
            }

            final View root = dialog.findViewById(R.id.etEditName).getRootView();

            final EditText etName = dialog.findViewById(R.id.etEditName);
            final EditText etDesc = dialog.findViewById(R.id.etEditDescription);

            final CheckBox cbMon = dialog.findViewById(R.id.cbEditMon);
            final CheckBox cbTue = dialog.findViewById(R.id.cbEditTue);
            final CheckBox cbWed = dialog.findViewById(R.id.cbEditWed);
            final CheckBox cbThu = dialog.findViewById(R.id.cbEditThu);
            final CheckBox cbFri = dialog.findViewById(R.id.cbEditFri);
            final CheckBox cbSat = dialog.findViewById(R.id.cbEditSat);
            final CheckBox cbSun = dialog.findViewById(R.id.cbEditSun);

            final String newName = etName != null && etName.getText() != null ? etName.getText().toString().trim() : "";
            final String newDesc = etDesc != null && etDesc.getText() != null ? etDesc.getText().toString().trim() : "";

            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(requireContext(), "Name is required.", Toast.LENGTH_SHORT).show();
                return;
            }

            final EnumSet<Habit.Day> newDays = EnumSet.noneOf(Habit.Day.class);
            if (cbMon != null && cbMon.isChecked()) newDays.add(Habit.Day.Monday);
            if (cbTue != null && cbTue.isChecked()) newDays.add(Habit.Day.Tuesday);
            if (cbWed != null && cbWed.isChecked()) newDays.add(Habit.Day.Wednesday);
            if (cbThu != null && cbThu.isChecked()) newDays.add(Habit.Day.Thursday);
            if (cbFri != null && cbFri.isChecked()) newDays.add(Habit.Day.Friday);
            if (cbSat != null && cbSat.isChecked()) newDays.add(Habit.Day.Saturday);
            if (cbSun != null && cbSun.isChecked()) newDays.add(Habit.Day.Sunday);

            if (newDays.isEmpty()) {
                Toast.makeText(requireContext(), "Select at least one day.", Toast.LENGTH_SHORT).show();
                return;
            }

            final Bundle args = getArguments() != null ? getArguments() : new Bundle();
            final String habitId = args.getString(ARG_ID, "");
            if (TextUtils.isEmpty(habitId)) {
                dismiss();
                return;
            }

            final HabitsViewModel vm = new ViewModelProvider(requireActivity()).get(HabitsViewModel.class);
            vm.updateHabit(habitId, newName, newDesc, newDays);

            dismiss();
        });
    }

    @NonNull
    private static String serializeDays(@Nullable final EnumSet<Habit.Day> days) {
        if (days == null || days.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final Habit.Day d : days) {
            if (!first) sb.append(",");
            sb.append(d.name());
            first = false;
        }
        return sb.toString();
    }

    @NonNull
    private static EnumSet<Habit.Day> deserializeDays(@NonNull final String text) {
        final EnumSet<Habit.Day> set = EnumSet.noneOf(Habit.Day.class);
        if (text.trim().isEmpty()) {
            return set;
        }

        final String[] parts = text.split(",");
        for (int i = 0; i < parts.length; i++) {
            final String p = parts[i] != null ? parts[i].trim() : "";
            if (p.isEmpty()) continue;
            try {
                set.add(Habit.Day.valueOf(p));
            } catch (final Exception ignored) {
            }
        }
        return set;
    }
}
