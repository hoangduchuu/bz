package com.ping.android.utils;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanluong on 1/25/18.
 */

public class DataSnapshotWrapper {
    DataSnapshot snapshot;

    public DataSnapshotWrapper(DataSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public String getStringValue(String name) {
        return getStringValue(name, "");
    }

    public String getStringValue(String name, String defaultValue) {
        Object value = getObject(name);
        return value != null ? value.toString() : defaultValue;
    }

    public int getIntValue(String name) {
        return getIntValue(name, -1);
    }

    public int getIntValue(String name, int defaultValue) {
        Integer value = snapshot.child(name).getValue(Integer.class);
        return value != null ? value : defaultValue;
    }

    public double getDoubleValue(String name) {
        return getDoubleValue(name, 0);
    }

    public double getDoubleValue(String name, double defaultValue) {
        if (isValid(name)) {
            Double value = snapshot.child(name).getValue(Double.class);
            return value != null ? value : defaultValue;
        }
        return defaultValue;
    }

    public boolean getBooleanValue(String name) {
        return getBooleanValue(name, false);
    }

    public boolean getBooleanValue(String name, boolean defaultValue) {
        if (isValid(name)) {
            Boolean value = snapshot.child(name).getValue(Boolean.class);
            return value != null ? value : defaultValue;
        }
        return defaultValue;
    }

    public Map getMapValue(String name) {
        Object object = getObject(name);
        if (object != null) {
            return (Map) object;
        }
        return new HashMap();
    }

    public Object getObject(String name) {
        return snapshot.child(name).getValue();
    }

    private boolean isValid(String name) {
        return snapshot.hasChild(name);
    }
}
