package com.example.forgetmenot;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.forgetmenot.ui.expandable.ExpandableSection;
import com.example.forgetmenot.ui.expandable.ExpandableSectionActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabMenu;
    private View pageTodo;
    private View pageWorkflow;
    private View pageOvertime;
    private View pagePrintData;
    private View pageSettings;
    private View pageAbout;
    private android.widget.TextView txtCurrentPage;

    private androidx.recyclerview.widget.RecyclerView recyclerAbout;
    private com.example.forgetmenot.work_flow.WorkflowController workflowController;

    private MenuHandler menuHandler;
    private MenuHandler.MenuItem location = MenuHandler.MenuItem.WORKFLOW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        menuHandler = new MenuHandler(this);

        initGUI();       // must come before renderLocation
        initPages();
        initAboutPage();
        initWorkflowPage();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        renderLocation(location);  // safe now
    }

    private void initGUI() {
        fabMenu = findViewById(R.id.fab_menu);
        fabMenu.setOnClickListener(this::showFabMenu);
        txtCurrentPage = findViewById(R.id.txt_current_page);
    }
    private void initPages() {
        pageTodo = findViewById(R.id.page_todo);
        pageWorkflow = findViewById(R.id.page_workflow);
        pageOvertime = findViewById(R.id.page_overtime);
        pagePrintData = findViewById(R.id.page_print_data);
        pageSettings = findViewById(R.id.page_settings);
        pageAbout = findViewById(R.id.page_about);
    }

    private void initAboutPage() {
        recyclerAbout = findViewById(R.id.recycler_about);
        recyclerAbout.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        java.util.ArrayList<com.example.forgetmenot.ui.expandable.ExpandableSection> sections =
                new java.util.ArrayList<>();

        sections.add(new ExpandableSection(
                getString(R.string.about_user_guide_title),
                getString(R.string.about_user_guide_body)
        ));
        sections.add(new ExpandableSection(
                getString(R.string.about_faq_title),
                getString(R.string.about_faq_body)
        ));
        sections.add(new ExpandableSection(
                getString(R.string.about_eula_title),
                getString(R.string.about_eula_body)
        ));
        sections.add(new ExpandableSection(
                getString(R.string.about_gdpr_title),
                getString(R.string.about_gdpr_body)
        ));

        // Reuse your existing adapter class
        recyclerAbout.setAdapter(new com.example.forgetmenot.ui.expandable.ExpandableSectionAdapter(sections));
    }

    private void initWorkflowPage() {
        View workflowRoot = findViewById(R.id.page_workflow); // or R.id.workflow_root if you set it
        workflowController = new com.example.forgetmenot.work_flow.WorkflowController(workflowRoot);
    }

    private void renderLocation(@NonNull MenuHandler.MenuItem loc) {
        pageTodo.setVisibility(View.GONE);
        pageWorkflow.setVisibility(View.GONE);
        pageOvertime.setVisibility(View.GONE);
        pagePrintData.setVisibility(View.GONE);
        pageSettings.setVisibility(View.GONE);
        pageAbout.setVisibility(View.GONE);

        if (loc == MenuHandler.MenuItem.TODO) {
            pageTodo.setVisibility(View.VISIBLE);
            txtCurrentPage.setText(R.string.header_todo);

        } else if (loc == MenuHandler.MenuItem.WORKFLOW) {
            pageWorkflow.setVisibility(View.VISIBLE);
            txtCurrentPage.setText(R.string.header_workflow);

        } else if (loc == MenuHandler.MenuItem.OVERTIME) {
            pageOvertime.setVisibility(View.VISIBLE);
            txtCurrentPage.setText(R.string.header_overtime_view);

        } else if (loc == MenuHandler.MenuItem.PRINT_DATA) {
            pagePrintData.setVisibility(View.VISIBLE);
            txtCurrentPage.setText(R.string.header_print_data);

        } else if (loc == MenuHandler.MenuItem.SETTINGS) {
            pageSettings.setVisibility(View.VISIBLE);
            txtCurrentPage.setText(R.string.header_settings);

        } else if (loc == MenuHandler.MenuItem.ABOUT) {
            pageAbout.setVisibility(View.VISIBLE);
            txtCurrentPage.setText(R.string.header_about);
        }
    }


    private void showFabMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_todo) {
                    location = menuHandler.menuTodoClicked();
                    renderLocation(location);
                    return true;

                } else if (id == R.id.menu_workFlow) {
                    location = menuHandler.menuWorkflowClicked();
                    renderLocation(location);
                    return true;

                } else if (id == R.id.menu_overTimeView) {
                    location = menuHandler.menuOverTimeViewClicked();
                    renderLocation(location);
                    return true;

                } else if (id == R.id.menu_printData) {
                    location = menuHandler.menuPrintDataClicked();
                    renderLocation(location);
                    return true;

                } else if (id == R.id.menu_settings) {
                    location = menuHandler.menuSettingsClicked();
                    renderLocation(location);
                    return true;

                } else if (id == R.id.menu_about) {
                    // IMPORTANT: Do NOT start a new Activity if About is a page inside MainActivity
                    location = menuHandler.menuAboutClicked();
                    renderLocation(location);
                    return true;
                }

                return false;
            }
        });

        popup.show();
    }
}
