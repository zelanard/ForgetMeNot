package com.example.forgetmenot.workflow;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorkflowViewModel extends AndroidViewModel {

    private final WorkflowRepository repository;

    private final MutableLiveData<List<WorkSession>> sessionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<WorkSession> openSessionLiveData = new MutableLiveData<>();

    public WorkflowViewModel(@NonNull final Application application) {
        super(application);
        repository = new WorkflowRepository(application.getApplicationContext());
        reload();
    }

    public LiveData<List<WorkSession>> getSessions() {
        return sessionsLiveData;
    }

    public LiveData<WorkSession> getOpenSession() {
        return openSessionLiveData;
    }

    public void checkIn(@NonNull final Instant when, final int intendedMinutes) {
        repository.startSession(when, intendedMinutes);
        reload();
    }

    public void checkOut(@NonNull final Instant when, final int breakMinutes) {
        final WorkSession open = repository.getOpenSession();
        if (open == null || open.Id == null || open.Id.trim().isEmpty()) {
            reload();
            return;
        }
        repository.endSession(open.Id, when, breakMinutes);
        reload();
    }

    private void reload() {
        final ArrayList<WorkSession> sessions = repository.loadSessions();
        sessionsLiveData.setValue(Collections.unmodifiableList(sessions));
        openSessionLiveData.setValue(repository.getOpenSession());
    }
}
