package com.ping.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.Gson;
import com.ping.android.utils.DataProvider;
import com.ping.android.utils.DataSnapshotWrapper;
import com.ping.android.utils.configs.Constant;

import org.json.JSONObject;

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
    // FIXME: consider remove this property
    public Boolean loginStatus;
    public boolean showMappingConfirm;
    public Map<String, String> mappings = new HashMap<>();
    public Setting settings;
    public Map<String, Boolean> blocks = new HashMap<>();
    public Map<String, Boolean> blockBys = new HashMap<>();

    /**
     * Map contains devices that opponentUser logged in, if count bigger than 0, it means users is online
     */
    public Map<String, Double> devices = new HashMap<>();

    // Local variable
    public Map<String, Boolean> friends = new HashMap<>();
    public Constant.TYPE_FRIEND typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
    public String nickName;
    
    public User() {}

    public User(DataSnapshot dataSnapshot) {
        this.key = dataSnapshot.getKey();
        DataSnapshotWrapper wrapper = new DataSnapshotWrapper(dataSnapshot);

        this.firstName = wrapper.getStringValue("first_name");
        this.lastName = wrapper.getStringValue("last_name");
        this.pingID = wrapper.getStringValue("ping_id");
        this.quickBloxID = wrapper.getIntValue("quickBloxID");
        this.email = wrapper.getStringValue("email");
        this.password = wrapper.getStringValue("password");
        this.phone = wrapper.getStringValue("phone");
        this.profile = wrapper.getStringValue("profile");
        this.loginStatus = wrapper.getBooleanValue("loginStatus");
        this.showMappingConfirm = wrapper.getBooleanValue("show_mapping_confirm");
        this.mappings = (Map<String, String>) wrapper.getMapValue("mappings");
        this.settings = new Setting(dataSnapshot.child("settings"));
        this.blocks = (Map<String, Boolean>) wrapper.getMapValue("blocks");
        if (blocks == null) blocks = new HashMap<>();
        this.blockBys = (Map<String, Boolean>) wrapper.getMapValue("blockBys");
        this.devices = wrapper.getMapValue("devices");
        if (this.blockBys == null) this.blockBys = new HashMap<>();
        if (this.mappings == null) {
            this.mappings = DataProvider.getDefaultMapping();
        }
        if (this.settings == null) {
            this.settings = Setting.defaultSetting();
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
        this.phone = "";
        this.password = password;
        this.showMappingConfirm = false;
        this.mappings = DataProvider.getDefaultMapping();
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
        //friendList = in.createTypedArrayList(User.CREATOR);
        typeFriend = Constant.TYPE_FRIEND.valueOf(in.readString());
        Gson gson = new Gson();
        //friends = gson.fromJson(in.readString(), Map.class);
        blocks = gson.fromJson(in.readString(), Map.class);
        blockBys = gson.fromJson(in.readString(), Map.class);
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
        //result.put("friends", friends);
        result.put("mappings", mappings);
        result.put("settings", settings.toMap());
        result.put("profile", profile);
        result.put("devices", devices);

        return result;
    }

    public String getDisplayName() {
        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
            return pingID;
        } else {
            return String.format("%s %s", firstName, lastName).trim();
        }
    }

    public String getFirstName() {
        return !TextUtils.isEmpty(firstName) ? firstName : pingID;
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
        //parcel.writeTypedList(friendList);
        parcel.writeString(typeFriend.toString());
         //jsonObject = new JSONObject(friends);
        //parcel.writeString(jsonObject.toString());
        JSONObject jsonObject = new JSONObject(blocks);
        parcel.writeString(jsonObject.toString());
        jsonObject = new JSONObject(blockBys);
        parcel.writeString(jsonObject.toString());
    }
}
