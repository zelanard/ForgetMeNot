package com.example.forgetmenot;

import android.content.Context;
import android.widget.Toast;

public final class MenuHandler {

    public enum MenuItem {
        TODO,
        WORKFLOW,
        OVERTIME,
        PRINT_DATA,
        SETTINGS,
        ABOUT
    }

    private final Context context;

    public MenuHandler(Context context) {
        this.context = context;
    }

    public MenuItem menuTodoClicked() {
        return MenuItem.TODO;
    }

    public MenuItem menuWorkflowClicked() {
        return MenuItem.WORKFLOW;
    }

    public MenuItem menuOverTimeViewClicked() {
        return MenuItem.OVERTIME;
    }

    public MenuItem menuPrintDataClicked() {
        return MenuItem.PRINT_DATA;
    }

    public MenuItem menuSettingsClicked() {
        return MenuItem.SETTINGS;
    }

    public MenuItem menuAboutClicked() {
        return MenuItem.ABOUT;
    }
}
