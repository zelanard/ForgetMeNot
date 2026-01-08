package com.example.forgetmenot.workflow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.forgetmenot.BaseActivity;
import com.example.forgetmenot.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Workflow entry point with 3 tabs:
 *  - Tab 1: Check in/out (existing functionality)
 *  - Tab 2: Overtime overview
 *  - Tab 3: Print
 */
public final class WorkFlowActivity extends BaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBaseContentView(R.layout.workflow_tabs);

        // Ensure notification channel exists (API 26+ requirement)
        NotificationChannels.ensureChannels(this);

        final TabLayout tabLayout = findViewById(R.id.workflow_tabs);
        final ViewPager2 pager = findViewById(R.id.workflow_pager);

        pager.setAdapter(new WorkflowTabsAdapter(this));
        pager.setOffscreenPageLimit(2);

        new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull final TabLayout.Tab tab, final int position) {
                switch (position) {
                    case 0:
                        tab.setText(R.string.workflow_tab_check);
                        break;
                    case 1:
                        tab.setText(R.string.workflow_tab_overtime);
                        break;
                    case 2:
                        tab.setText(R.string.workflow_tab_print);
                        break;
                    default:
                        tab.setText("Tab");
                        break;
                }
            }
        }).attach();
    }
}
