package com.example.forgetmenot.todo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.security.InvalidParameterException;

public final class TodoPagerAdapter extends FragmentStateAdapter {

    public TodoPagerAdapter(@NonNull final FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        switch (position){
            case 0:
                return new TodoShowFragment();
            case 1:
                return new TodoAddFragment();
            default:
                throw new InvalidParameterException("ToDo tab is not recognized");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
