package com.tl.rxfirebase.events;

import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by tuanluong on 1/22/18.
 */

public class ChildEvent {
    public Type type;
    public DataSnapshot dataSnapshot;
    @Nullable
    public String previousChildName;

    public static ChildEvent from(DataSnapshot dataSnapshot, Type type, @Nullable String previousChildName) {
        ChildEvent event = new ChildEvent();
        event.dataSnapshot = dataSnapshot;
        event.type = type;
        event.previousChildName = previousChildName;
        return event;
    }

    public enum Type {
        CHILD_ADDED, CHILD_CHANGED, CHILD_MOVED, CHILD_REMOVED
    }
}
