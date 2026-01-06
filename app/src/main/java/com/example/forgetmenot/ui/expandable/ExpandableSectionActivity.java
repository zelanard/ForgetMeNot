package com.example.forgetmenot.ui.expandable;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.forgetmenot.R;

import java.util.ArrayList;

public final class ExpandableSectionActivity extends AppCompatActivity {

    public static final String EXTRA_SCREEN_TITLE = "extra_screen_title";
    public static final String EXTRA_SECTIONS = "extra_sections";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expandable_section);

        String screenTitle = getIntent().getStringExtra(EXTRA_SCREEN_TITLE);
        if (screenTitle != null) {
            setTitle(screenTitle);
        }

        ArrayList<ExpandableSection> sections =
                getIntent().getParcelableArrayListExtra(EXTRA_SECTIONS);

        if (sections == null) {
            sections = new ArrayList<>();
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_sections);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExpandableSectionAdapter(sections));
    }

    @NonNull
    public static Bundle buildArgs(@NonNull String screenTitle,
                                   @NonNull ArrayList<ExpandableSection> sections) {
        Bundle b = new Bundle();
        b.putString(EXTRA_SCREEN_TITLE, screenTitle);
        b.putParcelableArrayList(EXTRA_SECTIONS, sections);
        return b;
    }
}
