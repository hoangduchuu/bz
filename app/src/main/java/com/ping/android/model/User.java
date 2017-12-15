package com.ping.android.model;

import android.os.Parcel;
import android.os.Parcelable;

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
public class User implements Parcelable {

    public String key;
    public String firstName;
    public String lastName;
    public String pingID;
    public int quickBloxID;
    public String password;
    public String email;
    public String phone;
    public String profile;
    public Boolean loginStatus;
    public boolean showMappingConfirm;
    public Map<String, String> mappings;
    public Setting settings;
    public Map<String, Boolean> friends;
    public Map<String, Boolean> blocks;
    public Map<String, Boolean> blockBys;

    // Local variable
    public ArrayList<User> friendList  = new ArrayList<>();
    public Constant.TYPE_FRIEND typeFriend;

    public User() {}

    public User(DataSnapshot dataSnapshot) {
        this.key = dataSnapshot.getKey();
        this.firstName = CommonMethod.getStringOf(dataSnapshot.child("first_name").getValue());
        this.lastName = CommonMethod.getStringOf(dataSnapshot.child("last_name").getValue());
        this.pingID = CommonMethod.getStringOf(dataSnapshot.child("ping_id").getValue());
        this.quickBloxID = CommonMethod.getIntOf(dataSnapshot.child("quickBloxID").getValue());
        this.email = CommonMethod.getStringOf(dataSnapshot.child("email").getValue());
        this.password = CommonMethod.getStringOf(dataSnapshot.child("password").getValue());
        this.phone = CommonMethod.getStringOf(dataSnapshot.child("phone").getValue());
        this.profile = CommonMethod.getStringOf(dataSnapshot.child("profile").getValue());
        this.loginStatus = CommonMethod.getBooleanOf(dataSnapshot.child("loginStatus").getValue());
        this.showMappingConfirm = CommonMethod.getBooleanOf(dataSnapshot.child("show_mapping_confirm").getValue());
        this.mappings = (Map<String, String>) dataSnapshot.child("mappings").getValue();
        this.settings = new Setting(dataSnapshot.child("settings"));
        this.friends = (Map<String, Boolean>) dataSnapshot.child("friends").getValue();
        if (friends == null) friends = new HashMap<>();
        this.blocks = (Map<String, Boolean>) dataSnapshot.child("blocks").getValue();
        if (blocks == null) blocks = new HashMap<>();
        this.blockBys = (Map<String, Boolean>) dataSnapshot.child("blockBys").getValue();
        if (this.blockBys == null) this.blockBys = new HashMap<>();
        if (this.mappings == null) {
            this.mappings = ServiceManager.getInstance().getDefaultMapping();
        }
        if (this.settings == null) {
            this.settings = Setting.defaultSetting();
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
        this.settings = Setting.defaultSetting();
    }

    protected User(Parcel in) {
        key = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        pingID = in.readString();
        quickBloxID = in.readInt();

        password = in.readString();
        email = in.readString();
        phone = in.readString();
        profile = in.readString();
        byte tmpLoginStatus = in.readByte();
        loginStatus = tmpLoginStatus == 0 ? null : tmpLoginStatus == 1;
        byte tmpShowMappingConfirm = in.readByte();
        showMappingConfirm = tmpShowMappingConfirm == 1;
        friendList = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(pingID);
        parcel.writeInt(quickBloxID);

        parcel.writeString(password);
        parcel.writeString(email);
        parcel.writeString(phone);
        parcel.writeString(profile);
        parcel.writeByte((byte) (loginStatus == null ? 0 : loginStatus ? 1 : 2));
        parcel.writeByte((byte) (showMappingConfirm ? 1 : 2));
        parcel.writeTypedList(friendList);
    }
}
