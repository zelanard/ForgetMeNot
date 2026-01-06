package com.example.forgetmenot.todo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forgetmenot.R;

import java.util.ArrayList;
import java.util.List;

public final class TodoShowFragment extends Fragment {

    private TodoViewModel viewModel;
    private TodoExpandableSectionAdapter sectionAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.todo_show, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        final RecyclerView recycler = view.findViewById(R.id.recyclerTasks);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        sectionAdapter = new TodoExpandableSectionAdapter(new TodoExpandableSectionAdapter.Actions() {
            @Override
            public void onSetCompleted(final int indexInAllTasks, final boolean completed) {
                viewModel.setCompletedAt(indexInAllTasks, completed);
            }

            @Override
            public void onLongPressDelete(final int indexInAllTasks) {
                viewModel.removeTaskAt(indexInAllTasks);
            }
        });

        recycler.setAdapter(sectionAdapter);

        viewModel.getTasks().observe(getViewLifecycleOwner(), allTasks -> {
            final ArrayList<TaskItem> active = new ArrayList<>();
            final ArrayList<TaskItem> completed = new ArrayList<>();

            for (int i = 0; i < allTasks.size(); i++) {
                final Task t = allTasks.get(i);
                final boolean isCompleted = t.Completed != null && t.Completed.booleanValue();

                final TaskItem item = new TaskItem(i, t);
                if (isCompleted) {
                    completed.add(item);
                } else {
                    active.add(item);
                }
            }

            final List<TaskSection> sections = new ArrayList<>();
            sections.add(new TaskSection("Active (" + active.size() + ")", active));
            sections.add(new TaskSection("Completed (" + completed.size() + ")", completed));

            sectionAdapter.submit(sections);
        });
    }
}
