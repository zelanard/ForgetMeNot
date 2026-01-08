package com.example.forgetmenot.workflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public final class WorkflowOvertimeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        final TextView tv = new TextView(requireContext());
        tv.setText("Overtime overview (coming next)");
        tv.setPadding(24, 24, 24, 24);
        return tv;
    }
}
