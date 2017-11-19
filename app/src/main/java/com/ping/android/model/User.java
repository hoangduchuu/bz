package com.ping.android.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.form.Setting;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    public String key;
    public String firstName;
    public String lastName;
    public String pingID;
    public Long quickBloxID;
    public String password;
    public String email;
    public String phone;
    public String profile;
    public Boolean loginStatus;
    public Boolean showMappingConfirm;
    public Map<String, String> mappings;
    public Setting settings;
    public Map<String, Boolean> friends;
    public Map<String, Boolean> blocks;

    // Local variable
    public ArrayList<User> friendList  = new ArrayList<>();
    public Constant.TYPE_FRIEND typeFriend;

    public User(DataSnapshot dataSnapshot) {
        this.key = dataSnapshot.getKey();
        this.firstName = CommonMethod.getStringOf(dataSnapshot.child("first_name").getValue());
        this.lastName = CommonMethod.getStringOf(dataSnapshot.child("last_name").getValue());
        this.pingID = CommonMethod.getStringOf(dataSnapshot.child("ping_id").getValue());
        this.quickBloxID = CommonMethod.getLongOf(dataSnapshot.child("quickBloxID").getValue());
        this.email = CommonMethod.getStringOf(dataSnapshot.child("email").getValue());
        this.password = CommonMethod.getStringOf(dataSnapshot.child("password").getValue());
        this.phone = CommonMethod.getStringOf(dataSnapshot.child("phone").getValue());
        this.profile = CommonMethod.getStringOf(dataSnapshot.child("profile").getValue());
        this.loginStatus = CommonMethod.getBooleanOf(dataSnapshot.child("loginStatus").getValue());
        this.showMappingConfirm = CommonMethod.getBooleanOf(dataSnapshot.child("show_mapping_confirm").getValue());
        this.mappings = (Map<String, String>) dataSnapshot.child("mappings").getValue();
        this.settings = new Setting(dataSnapshot.child("settings"));
        this.friends = (Map<String, Boolean>) dataSnapshot.child("friends").getValue();
        this.blocks = (Map<String, Boolean>) dataSnapshot.child("blocks").getValue();
        if (this.mappings == null) {
            this.mappings = ServiceManager.getInstance().getDefaultMapping();
        }
        if (this.settings == null) {
            this.settings = ServiceManager.getInstance().getDefaultSetting();
        }
        if (this.friends == null) {
            this.friends = new HashMap();
        }
        if (this.blocks == null) {
            this.blocks = new HashMap();
        }
    }

    public User(String firstName, String lastName, String pingID, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.pingID = pingID;
        this.email = email;
        this.password = password;
        this.showMappingConfirm = false;
        this.mappings = ServiceManager.getInstance().getDefaultMapping();
        this.settings = ServiceManager.getInstance().getDefaultSetting();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("first_name", firstName);
        result.put("last_name", lastName);
        result.put("ping_id", pingID);
        result.put("quickBloxID", quickBloxID);
        result.put("password", password);
        result.put("email", email);
        result.put("phone", phone);
        result.put("loginStatus", loginStatus);
        result.put("show_mapping_confirm", showMappingConfirm);
        result.put("friends", friends);
        result.put("mappings", mappings);
        result.put("settings", settings.toMap());
        result.put("profile", profile);

        return result;
    }

    public String getDisplayName() {
        if (StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)) {
            return pingID;
        } else {
            return String.format("%s %s", firstName, lastName).trim();
        }
    }

    public void initFriendList() {
        friendList = new ArrayList<>();
        for(String userID : friends.keySet()) {
            ServiceManager.getInstance().getUser(userID, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    if (error == null) {
                        User user = (User) data[0];
                        friendList.add(user);
                    }
                }
            });
        }
    }

    public void updateData(User user) {
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.quickBloxID = user.quickBloxID;
        this.password = user.password;
        this.email = user.email;
        this.phone = user.phone;
        this.profile = user.profile;
        this.loginStatus= user.loginStatus;
        this.showMappingConfirm = user.showMappingConfirm;
        this.mappings = user.mappings;
        this.settings = user.settings;
        this.friends = user.friends;
        this.blocks = user.blocks;

        initFriendList();
    }
}
