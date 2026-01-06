package com.example.forgetmenot.todo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public final class TodoPagerAdapter extends FragmentStateAdapter {

    public TodoPagerAdapter(@NonNull final FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        if (position == 0) {
            return new TodoAddFragment();
        }
        return new TodoShowFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
