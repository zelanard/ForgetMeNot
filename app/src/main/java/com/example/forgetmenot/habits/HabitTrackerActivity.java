package com.example.forgetmenot.habits;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.forgetmenot.BaseActivity;
import com.example.forgetmenot.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.widget.ViewPager2;

import java.security.InvalidParameterException;

public final class HabitTrackerActivity extends BaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBaseContentView(R.layout.habit_tracker);

        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        final ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new HabitsPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull final TabLayout.Tab tab, final int position) {
                switch (position){
                    case 0:
                        tab.setText(R.string.Habits_Tab_Header);
                        break;
                    case 1:
                        tab.setText(R.string.Add_Tab_Header);
                        break;
                    default:
                        throw new InvalidParameterException("Tab not recofnized");
                }
            }
        }).attach();
    }
}
