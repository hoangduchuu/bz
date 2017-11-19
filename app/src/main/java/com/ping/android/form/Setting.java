package com.ping.android.form;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.ultility.CommonMethod;

import java.util.HashMap;
import java.util.Map;

public class Setting {
    public Boolean notification;
    public Boolean private_profile;

    public Setting(DataSnapshot dataSnapshot) {
        this.notification = CommonMethod.getBooleanOf(dataSnapshot.child("notification").getValue());
        this.private_profile = CommonMethod.getBooleanOf(dataSnapshot.child("private_profile").getValue());

        if (this.notification == null) {
            this.notification = false;
        }

        if (this.private_profile == null) {
            this.private_profile = false;
        }
    }


    public Setting(Boolean chat_notification, Boolean profile_picture) {
        this.notification = chat_notification;
        this.private_profile = profile_picture;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("notification", notification);
        result.put("private_profile", private_profile);
        return result;
    }
}
