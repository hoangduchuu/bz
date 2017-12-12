package com.ping.android.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.ultility.CommonMethod;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Group {
    public String key;
    public String conversationID;
    public String groupName;
    public double timestamp;
    public String groupAvatar;
    public Map<String, Boolean> memberIDs;
    public Map<String, Boolean> deleteStatuses;

    public List<User> members = new ArrayList<>();

    public Group() {
        memberIDs = new HashMap<>();
    }

    public static Group from(DataSnapshot dataSnapshot) {
        Group group = dataSnapshot.getValue(Group.class);
        Assert.assertNotNull(group);
        group.key = dataSnapshot.getKey();
        return group;
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
        result.put("groupAvatar", groupAvatar);
        return result;
    }
}
