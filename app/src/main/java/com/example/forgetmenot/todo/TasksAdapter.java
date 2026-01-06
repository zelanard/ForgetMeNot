package com.example.forgetmenot.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forgetmenot.R;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.VH> {

    public interface Actions {
        void onSetCompleted(int indexInAllTasks, boolean completed);
        void onLongPressDelete(int indexInAllTasks);
    }

    private final Actions actions;
    private final ArrayList<TaskItem> items = new ArrayList<>();

    private final DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    public TasksAdapter(@NonNull final Actions actions) {
        this.actions = actions;
    }

    public void submit(@NonNull final List<TaskItem> taskItems) {
        items.clear();
        items.addAll(taskItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, final int position) {
        final TaskItem item = items.get(position);
        holder.bind(item, actions, formatter);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {

        private final CheckBox cbCompleted;
        private final TextView tvDescription;
        private final TextView tvCreated;
        private final ImageButton btnDelete;

        VH(@NonNull final View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreated = itemView.findViewById(R.id.tvCreated);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(
                @NonNull final TaskItem item,
                @NonNull final Actions actions,
                @NonNull final DateTimeFormatter formatter
        ) {
            final Task t = item.Task;

            tvDescription.setText(t.Description != null ? t.Description : "");

            final Instant created = t.CreationDate != null ? t.CreationDate : Instant.EPOCH;
            tvCreated.setText(formatter.format(created));

            final boolean completed = t.Completed != null && t.Completed.booleanValue();

            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(completed);
            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) ->
                    actions.onSetCompleted(item.IndexInAllTasks, isChecked)
            );

            // Delete button (trash icon)
            btnDelete.setOnClickListener(v ->
                    actions.onLongPressDelete(item.IndexInAllTasks)
            );

            // Optional: keep long-press delete too (you can remove this block if you want)
            itemView.setOnLongClickListener(v -> {
                actions.onLongPressDelete(item.IndexInAllTasks);
                return true;
            });
        }
    }
}
