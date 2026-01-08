package com.example.forgetmenot.workflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public final class WorkflowTabsAdapter extends FragmentStateAdapter {

    public WorkflowTabsAdapter(@NonNull final FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        switch (position) {
            case 0:
                return new WorkflowCheckFragment();
            case 1:
                return new WorkflowOvertimeFragment();
            case 2:
                return new WorkflowPrintFragment();
            default:
                return new WorkflowCheckFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
