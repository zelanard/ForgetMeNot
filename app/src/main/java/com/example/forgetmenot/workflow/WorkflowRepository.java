package com.example.forgetmenot.workflow;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;

public final class WorkflowRepository {

    private static final String PREFS_NAME = "workflow_prefs";
    private static final String KEY_SESSIONS_JSON = "sessions_json";

    // JSON keys
    private static final String K_ID = "id";
    private static final String K_CHECKIN = "checkIn";
    private static final String K_CHECKOUT = "checkOut";
    private static final String K_INTENDED_MINUTES = "intendedMinutes";

    private final SharedPreferences prefs;

    public WorkflowRepository(@NonNull final Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public ArrayList<WorkSession> loadSessions() {
        final String json = prefs.getString(KEY_SESSIONS_JSON, "[]");
        final ArrayList<WorkSession> result = new ArrayList<>();

        try {
            final JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                final JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;

                final WorkSession s = fromJson(obj);
                if (s != null) result.add(s);
            }
        } catch (final JSONException ignored) {
            // corrupted => empty
        }

        return result;
    }

    public void saveSessions(@NonNull final ArrayList<WorkSession> sessions) {
        final JSONArray arr = new JSONArray();
        for (int i = 0; i < sessions.size(); i++) {
            arr.put(toJson(sessions.get(i)));
        }
        prefs.edit().putString(KEY_SESSIONS_JSON, arr.toString()).apply();
    }

    @Nullable
    public WorkSession getOpenSession() {
        final ArrayList<WorkSession> sessions = loadSessions();
        for (int i = sessions.size() - 1; i >= 0; i--) {
            final WorkSession s = sessions.get(i);
            if (s != null && s.isOpen()) {
                return s;
            }
        }
        return null;
    }

    @NonNull
    public WorkSession startSession(@NonNull final Instant checkIn, final int intendedMinutes) {
        final ArrayList<WorkSession> sessions = loadSessions();

        // Close any previously open session defensively (optional policy)
        for (int i = sessions.size() - 1; i >= 0; i--) {
            final WorkSession s = sessions.get(i);
            if (s != null && s.isOpen()) {
                s.CheckOut = checkIn; // auto-close at new checkin time
                break;
            }
        }

        final WorkSession created = new WorkSession(checkIn, intendedMinutes);
        sessions.add(created);
        saveSessions(sessions);
        return created;
    }

    public boolean endSession(@NonNull final String sessionId, @NonNull final Instant checkOut) {
        final ArrayList<WorkSession> sessions = loadSessions();

        for (int i = 0; i < sessions.size(); i++) {
            final WorkSession s = sessions.get(i);
            if (s != null && sessionId.equals(s.Id) && s.isOpen()) {
                s.CheckOut = checkOut;
                saveSessions(sessions);
                return true;
            }
        }

        return false;
    }

    @NonNull
    private static JSONObject toJson(@NonNull final WorkSession s) {
        final JSONObject obj = new JSONObject();
        try {
            obj.put(K_ID, s.Id != null ? s.Id : "");
            obj.put(K_CHECKIN, s.CheckIn != null ? s.CheckIn.toString() : Instant.EPOCH.toString());
            obj.put(K_CHECKOUT, s.CheckOut != null ? s.CheckOut.toString() : JSONObject.NULL);
            obj.put(K_INTENDED_MINUTES, Math.max(0, s.IntendedMinutes));
        } catch (final JSONException ignored) {
        }
        return obj;
    }

    @Nullable
    private static WorkSession fromJson(@NonNull final JSONObject obj) {
        try {
            final String id = obj.optString(K_ID, "");
            final String inText = obj.optString(K_CHECKIN, Instant.EPOCH.toString());
            final int intended = Math.max(0, obj.optInt(K_INTENDED_MINUTES, 0));

            final Instant checkIn;
            try {
                checkIn = Instant.parse(inText);
            } catch (final Exception ignored) {
                return null;
            }

            final WorkSession s = new WorkSession(checkIn, intended);
            if (id != null && !id.trim().isEmpty()) {
                s.Id = id;
            }

            // checkOut may be null
            if (!obj.isNull(K_CHECKOUT)) {
                final String outText = obj.optString(K_CHECKOUT, null);
                if (outText != null) {
                    try {
                        s.CheckOut = Instant.parse(outText);
                    } catch (final Exception ignored) {
                        s.CheckOut = null;
                    }
                }
            }

            return s;
        } catch (final Exception ignored) {
            return null;
        }
    }
}
