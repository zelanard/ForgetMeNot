package com.example.forgetmenot;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String FAB_PREFS = "fab_prefs";
    private static final String KEY_FAB_X = "fab_x";
    private static final String KEY_FAB_Y = "fab_y";

    private FloatingActionButton fabMenu;

    /**
     * Call this instead of setContentView(...) in child activities.
     */
    protected final void setBaseContentView(@LayoutRes final int layoutResId) {
        super.setContentView(R.layout.activity_base);

        final View container = findViewById(R.id.content_container);
        getLayoutInflater().inflate(layoutResId, (android.view.ViewGroup) container, true);

        fabMenu = findViewById(R.id.fab_menu);
        restoreFabPosition(fabMenu);
        makeFabDraggable(fabMenu);
        fabMenu.setOnClickListener(this::showFabMenu);
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

                final boolean handled = MenuHandler.handleMenuClick(BaseActivity.this, menuItem);
                if (!handled) {
                    Toast.makeText(BaseActivity.this, R.string.not_implemented_yet, Toast.LENGTH_SHORT).show();
                }
                return handled;
            }
        });

        popup.show();
    }

    private MenuHandler.MenuItem mapMenuIdToEnum(final int menuId) {
        if (menuId == R.id.menu_todo) {
            return MenuHandler.MenuItem.TODO;
        } else if (menuId == R.id.menu_habit_tracker) {
            return MenuHandler.MenuItem.HABIT_TRACKER;
        } else if (menuId == R.id.menu_workFlow) {
            return MenuHandler.MenuItem.WORKFLOW;
        } else if (menuId == R.id.menu_settings) {
            return MenuHandler.MenuItem.SETTINGS;
        } else if (menuId == R.id.menu_about) {
            return MenuHandler.MenuItem.INFO;
        }
        return null;
    }

    private void makeFabDraggable(@NonNull final View fab) {
        final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        fab.setOnTouchListener(new View.OnTouchListener() {
            private float downRawX;
            private float downRawY;
            private float dX;
            private float dY;
            private boolean dragging;

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        dragging = false;
                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        dX = v.getX() - downRawX;
                        dY = v.getY() - downRawY;
                        return true;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        final float rawX = event.getRawX();
                        final float rawY = event.getRawY();

                        final float moveX = rawX - downRawX;
                        final float moveY = rawY - downRawY;

                        if (!dragging) {
                            final float dist2 = (moveX * moveX) + (moveY * moveY);
                            if (dist2 >= (float) (touchSlop * touchSlop)) {
                                dragging = true;
                            }
                        }

                        if (dragging) {
                            final float newX = rawX + dX;
                            final float newY = rawY + dY;

                            v.setX(clampX(v, newX));
                            v.setY(clampY(v, newY));
                        }
                        return true;
                    }

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        if (dragging) {
                            saveFabPosition(v);
                            return true;
                        }
                        v.performClick();
                        return true;
                    }

                    default:
                        return false;
                }
            }
        });
    }

    private float clampX(@NonNull final View v, final float x) {
        final View parent = (View) v.getParent();
        final float min = 0f;
        final float max = (float) (parent.getWidth() - v.getWidth());
        return Math.max(min, Math.min(max, x));
    }

    private float clampY(@NonNull final View v, final float y) {
        final View parent = (View) v.getParent();
        final float min = 0f;
        final float max = (float) (parent.getHeight() - v.getHeight());
        return Math.max(min, Math.min(max, y));
    }

    private void saveFabPosition(@NonNull final View fab) {
        final SharedPreferences prefs = getSharedPreferences(FAB_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_FAB_X, fab.getX())
                .putFloat(KEY_FAB_Y, fab.getY())
                .apply();
    }

    private void restoreFabPosition(@NonNull final View fab) {
        final SharedPreferences prefs = getSharedPreferences(FAB_PREFS, Context.MODE_PRIVATE);
        if (!prefs.contains(KEY_FAB_X) || !prefs.contains(KEY_FAB_Y)) {
            return;
        }

        final float x = prefs.getFloat(KEY_FAB_X, fab.getX());
        final float y = prefs.getFloat(KEY_FAB_Y, fab.getY());

        fab.post(() -> {
            fab.setX(clampX(fab, x));
            fab.setY(clampY(fab, y));
        });
    }
}
