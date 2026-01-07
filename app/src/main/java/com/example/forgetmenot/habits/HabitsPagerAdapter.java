package com.example.forgetmenot.habits;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public final class HabitsPagerAdapter extends FragmentStateAdapter {

    public HabitsPagerAdapter(@NonNull final FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        if (position == 0) {
            return new HabitsShowFragment();
        }
        return new HabitsAddFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
