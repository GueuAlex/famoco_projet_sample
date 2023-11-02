package com.famoco.morphodemo.initialization;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public enum ConnectionAction {
    INFO(0),
    NEXT_ACTIVITY(1),
    DIALOG_MESSAGE(2),
    PERMISSION_DENIED(3);

    private int value;
    @SuppressLint("UseSparseArrays")
    private static Map<Integer, ConnectionAction> map = new HashMap<>();

    ConnectionAction(int value) {
        this.value = value;
    }

    static {
        for (ConnectionAction connectionAction : ConnectionAction.values()) {
            map.put(connectionAction.value, connectionAction);
        }
    }

    public static ConnectionAction valueOf(int connectionAction) {
        return map.get(connectionAction);
    }

    public int getValue() {
        return value;
    }
}
