package com.ping.android.model;

import com.google.firebase.database.DataSnapshot;

import java.util.Map;

/**
 * Created by tuanluong on 1/25/18.
 */

public class DataSnapshotWrapper {
    DataSnapshot snapshot;

    public DataSnapshotWrapper(DataSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public String getStringValue(String name, String defaultValue) {
        Object object = getObject(name);
        if (object != null) {
            return object.toString();
        }
        return defaultValue;
    }

    private Object getObject(String name) {
        return snapshot.child(name).getValue();
    }
}
