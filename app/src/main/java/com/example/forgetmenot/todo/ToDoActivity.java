package com.example.forgetmenot.todo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.forgetmenot.BaseActivity;
import com.example.forgetmenot.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.security.InvalidParameterException;

public final class ToDoActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBaseContentView(R.layout.todo);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        final TodoPagerAdapter adapter = new TodoPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull final TabLayout.Tab tab, final int position) {
                switch (position){
                    case 0:
                        tab.setText(R.string.Tasks_Tab_Header);
                        break;
                    case 1:
                        tab.setText(R.string.Add_Tab_Header);
                        break;
                    default:
                        throw new IllegalArgumentException("Tab not recofnized");
                }
            }
        }).attach();
    }
}
