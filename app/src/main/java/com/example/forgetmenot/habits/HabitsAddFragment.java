package com.example.forgetmenot.habits;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.forgetmenot.R;

import java.util.EnumSet;

public final class HabitsAddFragment extends Fragment {

    private HabitsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.habit_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        final View daysExpandable = view.findViewById(R.id.daysExpandable);

        final TextView title = daysExpandable.findViewById(R.id.section_title);
        final View header = daysExpandable.findViewById(R.id.header);
        final View content = daysExpandable.findViewById(R.id.section_content);
        final android.widget.ImageView icon = daysExpandable.findViewById(R.id.expand_icon);

        title.setText(R.string.habit_days_title);

// Inflate checkboxes into section_content
        final android.widget.LinearLayout container = daysExpandable.findViewById(R.id.section_content);
        final View daysView = LayoutInflater.from(requireContext())
                .inflate(R.layout.habit_days_checkboxes, container, false);
        container.addView(daysView);

// Toggle
        header.setOnClickListener(v -> {
            final boolean willExpand = content.getVisibility() != View.VISIBLE;
            content.setVisibility(willExpand ? View.VISIBLE : View.GONE);
            icon.setRotation(willExpand ? 180f : 0f);
        });

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HabitsViewModel.class);

        final EditText etName = view.findViewById(R.id.etName);
        final EditText etDescription = view.findViewById(R.id.etDescription);
        final EditText etGoal = view.findViewById(R.id.etGoal);

        final CheckBox cbMon = daysExpandable.findViewById(R.id.cbMon);
        final CheckBox cbTue = daysExpandable.findViewById(R.id.cbTue);
        final CheckBox cbWed = daysExpandable.findViewById(R.id.cbWed);
        final CheckBox cbThu = daysExpandable.findViewById(R.id.cbThu);
        final CheckBox cbFri = daysExpandable.findViewById(R.id.cbFri);
        final CheckBox cbSat = daysExpandable.findViewById(R.id.cbSat);
        final CheckBox cbSun = daysExpandable.findViewById(R.id.cbSun);

        final Button btnAdd = view.findViewById(R.id.btnAddHabit);

        btnAdd.setOnClickListener(v -> {
            final String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            final String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            final String goalText = etGoal.getText() != null ? etGoal.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), "Please enter a habit name.", Toast.LENGTH_SHORT).show();
                return;
            }

            final int goal;
            try {
                goal = Math.max(1, Integer.parseInt(TextUtils.isEmpty(goalText) ? "1" : goalText));
            } catch (final NumberFormatException ignored) {
                Toast.makeText(requireContext(), "Goal must be a whole number (1 or more).", Toast.LENGTH_SHORT).show();
                return;
            }

            final EnumSet<Habit.Day> days = EnumSet.noneOf(Habit.Day.class);
            if (cbMon.isChecked()) days.add(Habit.Day.Monday);
            if (cbTue.isChecked()) days.add(Habit.Day.Tuesday);
            if (cbWed.isChecked()) days.add(Habit.Day.Wednesday);
            if (cbThu.isChecked()) days.add(Habit.Day.Thursday);
            if (cbFri.isChecked()) days.add(Habit.Day.Friday);
            if (cbSat.isChecked()) days.add(Habit.Day.Saturday);
            if (cbSun.isChecked()) days.add(Habit.Day.Sunday);

            if (days.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one day.", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.addHabit(name, description, days, goal);

            etName.setText("");
            etDescription.setText("");
            etGoal.setText("1");

            cbMon.setChecked(false);
            cbTue.setChecked(false);
            cbWed.setChecked(false);
            cbThu.setChecked(false);
            cbFri.setChecked(false);
            cbSat.setChecked(false);
            cbSun.setChecked(false);

            Toast.makeText(requireContext(), "Habit added.", Toast.LENGTH_SHORT).show();
        });
    }
}
