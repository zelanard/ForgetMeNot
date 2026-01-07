package com.example.forgetmenot.habits;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

        final Bundle args = getArguments() != null ? getArguments() : new Bundle();
        final String habitId = args.getString(ARG_ID, "");
        final String name = args.getString(ARG_NAME, "");
        final String desc = args.getString(ARG_DESC, "");
        final EnumSet<Habit.Day> days = deserializeDays(args.getString(ARG_DAYS, ""));

        etName.setText(name);
        etDesc.setText(desc);

        // Expandable "Days" section container (expandable_container.xml included in habit_edit_dialog.xml)
        final View daysExpandable = v.findViewById(R.id.daysExpandable);

        final TextView sectionTitle = daysExpandable.findViewById(R.id.section_title);
        final View header = daysExpandable.findViewById(R.id.header);
        final View content = daysExpandable.findViewById(R.id.section_content);
        final ImageView icon = daysExpandable.findViewById(R.id.expand_icon);

        sectionTitle.setText(R.string.habit_days_title);

        // Inflate shared checkbox group into the expandable container
        final LinearLayout container = daysExpandable.findViewById(R.id.section_content);
        final View daysView = LayoutInflater.from(requireContext())
                .inflate(R.layout.habit_days_checkboxes, container, false);
        container.addView(daysView);

        // Find checkboxes from the inflated content (ids: cbMon..cbSun)
        final CheckBox cbMon = daysExpandable.findViewById(R.id.cbMon);
        final CheckBox cbTue = daysExpandable.findViewById(R.id.cbTue);
        final CheckBox cbWed = daysExpandable.findViewById(R.id.cbWed);
        final CheckBox cbThu = daysExpandable.findViewById(R.id.cbThu);
        final CheckBox cbFri = daysExpandable.findViewById(R.id.cbFri);
        final CheckBox cbSat = daysExpandable.findViewById(R.id.cbSat);
        final CheckBox cbSun = daysExpandable.findViewById(R.id.cbSun);

        cbMon.setChecked(days.contains(Habit.Day.Monday));
        cbTue.setChecked(days.contains(Habit.Day.Tuesday));
        cbWed.setChecked(days.contains(Habit.Day.Wednesday));
        cbThu.setChecked(days.contains(Habit.Day.Thursday));
        cbFri.setChecked(days.contains(Habit.Day.Friday));
        cbSat.setChecked(days.contains(Habit.Day.Saturday));
        cbSun.setChecked(days.contains(Habit.Day.Sunday));

        // Optional: start expanded in edit mode (comment out if you prefer collapsed)
        content.setVisibility(View.VISIBLE);
        icon.setRotation(180f);

        header.setOnClickListener(x -> {
            final boolean willExpand = content.getVisibility() != View.VISIBLE;
            content.setVisibility(willExpand ? View.VISIBLE : View.GONE);
            icon.setRotation(willExpand ? 180f : 0f);
        });

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.habit_edit_title)
                .setView(v)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, null) // override after show to validate
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

            final EditText etName = dialog.findViewById(R.id.etEditName);
            final EditText etDesc = dialog.findViewById(R.id.etEditDescription);

            final String newName = etName != null && etName.getText() != null ? etName.getText().toString().trim() : "";
            final String newDesc = etDesc != null && etDesc.getText() != null ? etDesc.getText().toString().trim() : "";

            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(requireContext(), R.string.habit_error_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            final View daysExpandable = dialog.findViewById(R.id.daysExpandable);

            final CheckBox cbMon = daysExpandable != null ? daysExpandable.findViewById(R.id.cbMon) : null;
            final CheckBox cbTue = daysExpandable != null ? daysExpandable.findViewById(R.id.cbTue) : null;
            final CheckBox cbWed = daysExpandable != null ? daysExpandable.findViewById(R.id.cbWed) : null;
            final CheckBox cbThu = daysExpandable != null ? daysExpandable.findViewById(R.id.cbThu) : null;
            final CheckBox cbFri = daysExpandable != null ? daysExpandable.findViewById(R.id.cbFri) : null;
            final CheckBox cbSat = daysExpandable != null ? daysExpandable.findViewById(R.id.cbSat) : null;
            final CheckBox cbSun = daysExpandable != null ? daysExpandable.findViewById(R.id.cbSun) : null;

            final EnumSet<Habit.Day> newDays = EnumSet.noneOf(Habit.Day.class);
            if (cbMon != null && cbMon.isChecked()) newDays.add(Habit.Day.Monday);
            if (cbTue != null && cbTue.isChecked()) newDays.add(Habit.Day.Tuesday);
            if (cbWed != null && cbWed.isChecked()) newDays.add(Habit.Day.Wednesday);
            if (cbThu != null && cbThu.isChecked()) newDays.add(Habit.Day.Thursday);
            if (cbFri != null && cbFri.isChecked()) newDays.add(Habit.Day.Friday);
            if (cbSat != null && cbSat.isChecked()) newDays.add(Habit.Day.Saturday);
            if (cbSun != null && cbSun.isChecked()) newDays.add(Habit.Day.Sunday);

            if (newDays.isEmpty()) {
                Toast.makeText(requireContext(), R.string.habit_error_days_required, Toast.LENGTH_SHORT).show();
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
