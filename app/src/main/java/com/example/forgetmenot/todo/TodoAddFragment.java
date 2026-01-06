package com.example.forgetmenot.todo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.forgetmenot.R;

public final class TodoAddFragment extends Fragment {

    private TodoViewModel viewModel;

    public TodoAddFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.todo_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        final EditText etTask = view.findViewById(R.id.etTask);
        final Button btnAdd = view.findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String text = etTask.getText() != null ? etTask.getText().toString().trim() : "";
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(requireContext(), "Please enter a task.", Toast.LENGTH_SHORT).show();
                    return;
                }

                viewModel.addTask(text);
                etTask.setText("");
                Toast.makeText(requireContext(), "Task added.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
