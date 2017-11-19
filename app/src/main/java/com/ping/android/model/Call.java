package com.ping.android.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.ping.android.ultility.CommonMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Call {
    public String key;
    public String senderId;
    public String receiveId;
    public Long status;
    public Double timestamp;
    public Map<String, Boolean> deleteStatuses;

    public List<User> members = new ArrayList<>();
    public User opponentUser;

    public Call(String senderId, String receiveId, Long status, Map<String, Boolean> deleteStatuses, Double timestamp) {
        this.senderId = senderId;
        this.receiveId = receiveId;
        this.status = status;
        this.deleteStatuses = deleteStatuses;
        this.timestamp = timestamp;
    }

    public Call(DataSnapshot dataSnapshot) {
        this.key = dataSnapshot.getKey();
        this.senderId = CommonMethod.getStringOf(dataSnapshot.child("senderId").getValue());
        this.receiveId = CommonMethod.getStringOf(dataSnapshot.child("receiveId").getValue());
        this.status = CommonMethod.getLongOf(dataSnapshot.child("status").getValue());
        this.timestamp = CommonMethod.getDoubleOf(dataSnapshot.child("timestamp").getValue());
        this.deleteStatuses = (Map<String, Boolean>) dataSnapshot.child("deleteStatuses").getValue();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("senderId", senderId);
        result.put("receiveId", receiveId);
        result.put("status", status);
        result.put("timestamp", timestamp);
        result.put("deleteStatuses", deleteStatuses);
        return result;
    }
}
