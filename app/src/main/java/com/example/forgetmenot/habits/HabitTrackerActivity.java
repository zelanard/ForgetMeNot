package com.example.forgetmenot.habits;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.forgetmenot.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public final class HabitTrackerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_tracker);

        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        final ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new HabitsPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull final TabLayout.Tab tab, final int position) {
                if (position == 0) {
                    tab.setText("My Habits");
                } else {
                    tab.setText("New Habit");
                }
            }
        }).attach();
    }
}