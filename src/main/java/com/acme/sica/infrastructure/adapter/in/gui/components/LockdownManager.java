package com.acme.sica.infrastructure.adapter.in.gui.components;

import java.util.ArrayList;
import java.util.List;

public class LockdownManager {

    public interface LockdownListener {
        void onLockdownStateChanged(boolean isLockdownActive);
    }

    private static final LockdownManager instance = new LockdownManager();
    private boolean lockdownActive = false;
    private final List<LockdownListener> listeners = new ArrayList<>();

    private LockdownManager() {}

    public static LockdownManager getInstance() {
        return instance;
    }

    public boolean isLockdownActive() {
        return lockdownActive;
    }

    public void setLockdownActive(boolean active) {
        this.lockdownActive = active;
        notifyListeners();
    }

    public void addListener(LockdownListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(LockdownListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (LockdownListener l : listeners) {
            try {
                l.onLockdownStateChanged(lockdownActive);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
