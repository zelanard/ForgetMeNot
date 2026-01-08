package com.example.forgetmenot.habits;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forgetmenot.R;

import java.util.ArrayList;
import java.util.List;

public final class HabitsStatisticsAdapter extends RecyclerView.Adapter<HabitsStatisticsAdapter.VH> {

    private final ArrayList<HabitStatItem> items = new ArrayList<>();

    public void submit(@NonNull final List<HabitStatItem> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_stat_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, final int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvSummary;

        VH(@NonNull final View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStatHabitName);
            tvSummary = itemView.findViewById(R.id.tvStatSummary);
        }

        void bind(@NonNull final HabitStatItem item) {
            tvName.setText(item.Name);

            final String goalPart = "Goal: " + item.Goal;
            final String weekPart = "Last 7 days: " + item.TotalCompletions7 + " completions";
            final String metPart = "Goal met: " + item.DaysMetGoal7 + " / " + item.ScheduledDays7 + " scheduled days";
            final String streakPart = "Streak: " + item.CurrentStreak;

            tvSummary.setText(goalPart + "\n" + weekPart + "\n" + metPart + "\n" + streakPart);
        }
    }
}
