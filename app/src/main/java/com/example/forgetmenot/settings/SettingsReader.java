package com.example.forgetmenot.settings;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class SettingsReader {

    private SettingsReader() {}

    public static boolean isWorkEndNotificationEnabled(final Context context) {
        try {
            final File f = new File(context.getFilesDir(), "settings.json");
            if (!f.exists()) {
                return false;
            }

            final byte[] bytes = Files.readAllBytes(f.toPath());
            final String json = new String(bytes, StandardCharsets.UTF_8);

            final JSONObject root = new JSONObject(json);
            final JSONObject notifications = root.optJSONObject("notifications");
            if (notifications == null) {
                return false;
            }

            return notifications.optBoolean("workend", false);
        } catch (Exception ex) {
            return false;
        }
    }
}
