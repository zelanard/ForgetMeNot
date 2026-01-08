package com.example.forgetmenot.About;
import com.example.forgetmenot.R;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forgetmenot.BaseActivity;
import com.example.forgetmenot.ui.expandable.ExpandableSection;
import com.example.forgetmenot.ui.expandable.ExpandableSectionAdapter;

import java.util.ArrayList;

public final class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBaseContentView(R.layout.about);
        setTitle(R.string.header_info);

        final RecyclerView recycler = findViewById(R.id.recycler_sections);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        final ArrayList<ExpandableSection> sections = new ArrayList<>();

        sections.add(new ExpandableSection(
                getString(R.string.info_user_guide_title),
                getString(R.string.info_user_guide_body)
        ));
        sections.add(new ExpandableSection(
                getString(R.string.info_gdpr_title),
                getString(R.string.info_gdpr_body)
        ));
        sections.add(new ExpandableSection(
                getString(R.string.info_eula_title),
                getString(R.string.info_eula_body)
        ));
        sections.add(new ExpandableSection(
                getString(R.string.info_faq_title),
                getString(R.string.info_faq_body)
        ));

        recycler.setAdapter(new ExpandableSectionAdapter(sections));
    }
}
