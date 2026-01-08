package com.example.forgetmenot.habits;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.security.InvalidParameterException;

public final class HabitsPagerAdapter extends FragmentStateAdapter {

    public HabitsPagerAdapter(@NonNull final FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        switch (position){
            case 0:
                return new HabitsShowFragment();
            case 1:
                return new HabitsAddFragment();
            case 2:
                return new HabitsStatisticsFragment();
            default:
                throw new IllegalArgumentException("Tab not recognized");
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
