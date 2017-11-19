package com.ping.android.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.ping.android.ultility.CommonMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {
    public String key;
    public String conversationID;
    public String groupName;
    public Double timestamp;
    public Map<String, Boolean> memberIDs;
    public Map<String, Boolean> deleteStatuses;

    public List<User> members = new ArrayList<>();

    public Group() {
        memberIDs = new HashMap<>();
    }

    public Group(DataSnapshot dataSnapshot) {
        this.key = dataSnapshot.getKey();
        this.conversationID = CommonMethod.getStringOf(dataSnapshot.child("conversationID").getValue());
        this.groupName = CommonMethod.getStringOf(dataSnapshot.child("groupName").getValue());
        this.timestamp = CommonMethod.getDoubleOf(dataSnapshot.child("timestamp").getValue());
        this.memberIDs = (Map<String, Boolean>) dataSnapshot.child("memberIDs").getValue();
        this.deleteStatuses = (Map<String, Boolean>) dataSnapshot.child("deleteStatuses").getValue();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("conversationID", conversationID);
        result.put("groupName", groupName);
        result.put("timestamp", timestamp);
        result.put("memberIDs", memberIDs);
        result.put("deleteStatuses", deleteStatuses);
        return result;
    }
}
