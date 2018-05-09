package com.ping.android.model;

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
        Object object = getObject(name);
        if (object != null) {
            return object.toString();
        }
        return defaultValue;
    }

    public int getIntValue(String name) {
        return getIntValue(name, -1);
    }

    public int getIntValue(String name, int defaultValue) {
        Object object = getObject(name);
        if (object != null) {
            return Integer.parseInt(object.toString());
        }
        return defaultValue;
    }

    public double getDoubleValue(String name) {
        return getDoubleValue(name, 0);
    }

    public double getDoubleValue(String name, double defaultValue) {
        Object object = getObject(name);
        if (object != null) {
            return Double.parseDouble(object.toString());
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

    private Object getObject(String name) {
        return snapshot.child(name).getValue();
    }
}
