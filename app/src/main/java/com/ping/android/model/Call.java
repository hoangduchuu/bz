package com.ping.android.model;

import android.service.autofill.Dataset;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.ping.android.ultility.CommonMethod;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Call {
    public String key;
    public String senderId;
    public String receiveId;
    public int status;
    public double timestamp;
    public Map<String, Boolean> deleteStatuses = new HashMap<>();

    public List<User> members = new ArrayList<>();
    public User opponentUser;

    public Call() {}

    public Call(String senderId, String receiveId, int status, Map<String, Boolean> deleteStatuses, double timestamp) {
        this.senderId = senderId;
        this.receiveId = receiveId;
        this.status = status;
        this.deleteStatuses = deleteStatuses;
        this.timestamp = timestamp;
    }

    public static Call from(DataSnapshot dataSnapshot) {
        Call call = dataSnapshot.getValue(Call.class);
        Assert.assertNotNull(call);
        call.key = dataSnapshot.getKey();
        return call;
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
