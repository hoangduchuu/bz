package com.ping.android.form;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.ultility.CommonMethod;

import java.util.HashMap;
import java.util.Map;

public class Setting {
    public boolean notification;
    public boolean private_profile;

    public Setting() {}

    public static Setting defaultSetting() {
        return new Setting(true, false);
    }

    public Setting(DataSnapshot dataSnapshot) {
        this.notification = CommonMethod.getBooleanOf(dataSnapshot.child("notification").getValue());
        this.private_profile = CommonMethod.getBooleanOf(dataSnapshot.child("private_profile").getValue());
    }


    public Setting(boolean chat_notification, boolean profile_picture) {
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
