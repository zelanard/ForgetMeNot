package com.example.forgetmenot.habits;

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

public final class HabitsShowFragment extends Fragment {

    private HabitsViewModel viewModel;
    private HabitsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.habit_show, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HabitsViewModel.class);

        final RecyclerView recycler = view.findViewById(R.id.recyclerHabits);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HabitsAdapter(new HabitsAdapter.Actions() {
            @Override
            public void onDelete(final int index) {
                viewModel.removeHabitAt(index);
            }

            @Override
            public void onIncrementToday(@NonNull final String habitId) {
                viewModel.incrementToday(habitId);
            }
        });

        recycler.setAdapter(adapter);

        viewModel.getHabits().observe(getViewLifecycleOwner(), habits -> adapter.submit(habits));
        viewModel.getCountToday().observe(getViewLifecycleOwner(), map -> adapter.submitCountToday(map));
    }
}
