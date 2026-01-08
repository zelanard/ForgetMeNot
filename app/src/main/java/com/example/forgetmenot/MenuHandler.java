package com.example.forgetmenot;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.forgetmenot.About.AboutActivity;
import com.example.forgetmenot.habits.HabitTrackerActivity;
import com.example.forgetmenot.todo.ToDoActivity;
import com.example.forgetmenot.workflow.WorkFlowActivity;

public final class MenuHandler {

    public enum MenuItem {
        TODO,
        HABIT_TRACKER,
        WORKFLOW,
        SETTINGS,
        INFO
    }

    private MenuHandler() {
        // Utility class
    }

    /**
     * One menu enum -> one explicit Intent -> one Activity.
     */
    public static boolean handleMenuClick(
            @NonNull final Context context,
            @NonNull final MenuItem menuItem
    ) {
        final Intent intent;

        switch (menuItem) {
            case TODO:
                intent = new Intent(context, ToDoActivity.class);
                break;

            case HABIT_TRACKER:
                intent = new Intent(context, HabitTrackerActivity.class);
                break;

            case WORKFLOW:
                intent = new Intent(context, WorkFlowActivity.class);
                break;

            /*
            case SETTINGS:
                intent = new Intent(context, SettingsActivity.class);
                break;

            */
            case INFO:
                intent = new Intent(context, AboutActivity.class);
                break;

            default:
                return false;
        }

        context.startActivity(intent);
        return true;
    }
}
