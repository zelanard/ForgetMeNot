package com.example.forgetmenot.habits;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forgetmenot.R;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.VH> {

    public interface Actions {
        void onDelete(int index);
        void onIncrementToday(@NonNull String habitId);
        void onEdit(@NonNull Habit habit);
    }

    private final Actions actions;
    private final ArrayList<Habit> items = new ArrayList<>();
    private final HashMap<String, Integer> countToday = new HashMap<>();

    public HabitsAdapter(@NonNull final Actions actions) {
        this.actions = actions;
    }

    public void submit(@NonNull final List<Habit> habits) {
        items.clear();
        items.addAll(habits);
        notifyDataSetChanged();
    }

    public void submitCountToday(@NonNull final Map<String, Integer> map) {
        countToday.clear();
        countToday.putAll(map);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, final int position) {
        final Habit habit = items.get(position);
        holder.bind(habit, position, actions, countToday);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {

        private final TextView tvHabitName;
        private final TextView tvHabitDescription;
        private final TextView tvHabitDays;

        private final TextView tvProgress;
        private final Button btnAddCompletion;
        private final ImageButton btnEditHabit;
        private final ImageButton btnDeleteHabit;

        VH(@NonNull final View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvHabitDescription = itemView.findViewById(R.id.tvHabitDescription);
            tvHabitDays = itemView.findViewById(R.id.tvHabitDays);

            tvProgress = itemView.findViewById(R.id.tvProgress);
            btnAddCompletion = itemView.findViewById(R.id.btnAddCompletion);
            btnEditHabit = itemView.findViewById(R.id.btnEditHabit);
            btnDeleteHabit = itemView.findViewById(R.id.btnDeleteHabit);
        }

        void bind(
                @NonNull final Habit habit,
                final int index,
                @NonNull final Actions actions,
                @NonNull final HashMap<String, Integer> countToday
        ) {
            tvHabitName.setText(habit.Name != null ? habit.Name : "");
            tvHabitDescription.setText(habit.Description != null ? habit.Description : "");
            tvHabitDays.setText(formatDays(habit.ScheduledDays));

            final String habitId = habit.Id != null ? habit.Id : "";
            final int goal = Math.max(1, habit.Goal);

            final int count = (!habitId.isEmpty() && countToday.containsKey(habitId))
                    ? Math.max(0, countToday.get(habitId))
                    : 0;

            final boolean completedToday = count >= goal;

            tvProgress.setText(completedToday ? (count + " / " + goal + " (Done)") : (count + " / " + goal));

            btnAddCompletion.setEnabled(!habitId.isEmpty());
            btnEditHabit.setOnClickListener(v -> actions.onEdit(habit));
            btnDeleteHabit.setOnClickListener(v -> actions.onDelete(index));
            btnAddCompletion.setOnClickListener(v -> {
                if (!habitId.isEmpty()) {
                    actions.onIncrementToday(habitId);
                }
            });
        }

        @NonNull
        private static String formatDays(final EnumSet<Habit.Day> days) {
            if (days == null || days.isEmpty()) {
                return "";
            }

            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (final Habit.Day d : days) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(d.name());
                first = false;
            }
            return sb.toString();
        }
    }
}
