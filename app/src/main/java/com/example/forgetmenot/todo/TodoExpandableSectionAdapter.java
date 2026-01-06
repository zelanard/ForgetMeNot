package com.example.forgetmenot.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forgetmenot.R;

import java.util.ArrayList;
import java.util.List;

public final class TodoExpandableSectionAdapter extends RecyclerView.Adapter<TodoExpandableSectionAdapter.VH> {

    public interface Actions {
        void onSetCompleted(int indexInAllTasks, boolean completed);
        void onLongPressDelete(int indexInAllTasks);
    }

    private final Actions actions;
    private final ArrayList<TaskSection> sections = new ArrayList<>();
    private boolean[] expanded = new boolean[0];

    public TodoExpandableSectionAdapter(@NonNull final Actions actions) {
        this.actions = actions;
    }

    public void submit(@NonNull final List<TaskSection> newSections) {
        sections.clear();
        sections.addAll(newSections);

        // Reset expansion state to match new size; default expanded = true for Active, false for Completed
        expanded = new boolean[sections.size()];
        for (int i = 0; i < sections.size(); i++) {
            final String title = sections.get(i).Title.toLowerCase();
            expanded[i] = title.startsWith("active");
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_expandable_task_section, parent, false);
        return new VH(v, actions);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, final int position) {
        final TaskSection section = sections.get(position);

        holder.title.setText(section.Title);
        holder.bindItems(section.Items);

        final boolean isExpanded = expanded[position];
        holder.recycler.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.icon.setRotation(isExpanded ? 180f : 0f);

        holder.header.setOnClickListener(v -> {
            expanded[position] = !expanded[position];
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static final class VH extends RecyclerView.ViewHolder {

        final View header;
        final TextView title;
        final ImageView icon;
        final RecyclerView recycler;

        private final TasksAdapter tasksAdapter;

        VH(@NonNull final View itemView, @NonNull final Actions actions) {
            super(itemView);

            header = itemView.findViewById(R.id.header);
            title = itemView.findViewById(R.id.section_title);
            icon = itemView.findViewById(R.id.expand_icon);
            recycler = itemView.findViewById(R.id.section_recycler);

            recycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recycler.setNestedScrollingEnabled(false);

            tasksAdapter = new TasksAdapter(new TasksAdapter.Actions() {
                @Override
                public void onSetCompleted(final int indexInAllTasks, final boolean completed) {
                    actions.onSetCompleted(indexInAllTasks, completed);
                }

                @Override
                public void onLongPressDelete(final int indexInAllTasks) {
                    actions.onLongPressDelete(indexInAllTasks);
                }
            });

            recycler.setAdapter(tasksAdapter);
        }

        void bindItems(@NonNull final List<TaskItem> items) {
            tasksAdapter.submit(items);
        }
    }
}
