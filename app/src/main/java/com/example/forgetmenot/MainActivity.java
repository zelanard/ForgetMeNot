package com.example.forgetmenot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public final class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabMenu;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fabMenu = findViewById(R.id.fab_menu);
        fabMenu.setOnClickListener(this::showFabMenu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            final Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showFabMenu(@NonNull final View anchor) {
        final PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull final MenuItem item) {
                final MenuHandler.MenuItem menuItem = mapMenuIdToEnum(item.getItemId());
                if (menuItem == null) {
                    return false;
                }

                final boolean handled = MenuHandler.handleMenuClick(MainActivity.this, menuItem);
                if (!handled) {
                    Toast.makeText(MainActivity.this, "Not implemented yet.", Toast.LENGTH_SHORT).show();
                }
                return handled;
            }
        });

        popup.show();
    }

    /**
     * UI layer mapping: R.id.* -> MenuHandler.MenuItem
     * Keeps resource IDs out of MenuHandler.
     */
    private MenuHandler.MenuItem mapMenuIdToEnum(final int menuId) {
        if (menuId == R.id.menu_todo) {
            return MenuHandler.MenuItem.TODO;
        } else if (menuId == R.id.menu_habit_tracker) {
            return MenuHandler.MenuItem.HABIT_TRACKER;
        } else if (menuId == R.id.menu_workFlow) {
            return MenuHandler.MenuItem.WORKFLOW;
        } else if (menuId == R.id.menu_overTimeView) {
            return MenuHandler.MenuItem.OVERTIME;
        } else if (menuId == R.id.menu_printData) {
            return MenuHandler.MenuItem.PRINT_DATA;
        } else if (menuId == R.id.menu_settings) {
            return MenuHandler.MenuItem.SETTINGS;
        } else if (menuId == R.id.menu_about) {
            return MenuHandler.MenuItem.ABOUT;
        }

        return null;
    }
}
