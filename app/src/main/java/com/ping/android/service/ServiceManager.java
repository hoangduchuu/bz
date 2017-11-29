package com.ping.android.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.ping.android.App;
import com.ping.android.activity.R;
import com.ping.android.db.QbUsersDbManager;
import com.ping.android.form.Mapping;
import com.ping.android.form.Setting;
import com.ping.android.model.Call;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.ultility.Consts;
import com.ping.android.util.QBResRequestExecutor;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;
import com.ping.android.utils.Toaster;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBEventType;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.users.model.QBUser;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ServiceManager {
    private static final String TAG = ServiceManager.class.getSimpleName();
    public static final int IMG_DEFAULT = R.drawable.ic_avatar_gray;
    private static ServiceManager ourInstance = new ServiceManager();
    private final String emojiRegex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";
    boolean isGettingUser = false;
    private FirebaseAuth auth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private User currentUser;
    private ArrayList<User> allUsers;
    private ArrayList<Callback> getUserCallbacks;
    private ArrayList<Callback> updateProfileCallbacks;
    private SharedPrefsHelper sharedPrefsHelper;
    //private QBUser currentQBUser;
    //ArrayList<QBUser> allQBUsers;
    private QBResRequestExecutor requestExecutor;
    private QbUsersDbManager dbManager;
    private Bitmap defaultProfileImage;

    private Map<String, Bitmap> profileImages = new HashMap();

    private ServiceManager() {
        getUserCallbacks = new ArrayList<>();
        updateProfileCallbacks = new ArrayList<>();
        allUsers = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        storage = FirebaseStorage.getInstance();

        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        requestExecutor = App.getInstance().getQbResRequestExecutor();
        dbManager = QbUsersDbManager.getInstance(App.getInstance().getBaseContext());

        defaultProfileImage = BitmapFactory.decodeResource(App.getInstance().getApplicationContext().getResources(),
                R.drawable.ic_avatar_gray);
    }

    public static ServiceManager getInstance() {
        return ourInstance;
    }

    public void resetData() {
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String userID = postSnapshot.getKey();
                    mDatabase.child("users").child(userID).child("conversations").setValue(null);
                    mDatabase.child("users").child(userID).child("groups").setValue(null);
                    mDatabase.child("users").child(userID).child("friends").setValue(null);
                    mDatabase.child("users").child(userID).child("calls").setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });

    }

    //-------------------------------------------------------
    // Start User region
    //-------------------------------------------------------
    public void initUserData(Callback completion) {
        if (currentUser != null) {
            completion.complete(null);
            return;
        }
        if (isGettingUser) {
            getUserCallbacks.add(completion);
            return;
        }
        getUserCallbacks.add(completion);
        isGettingUser = true;
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        mDatabase.child("users").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(String.format("users.%s addListenerForSingleValueEvent onDataChange key: %s",
                        mFirebaseUser.getUid(), dataSnapshot.getKey()));
                isGettingUser = false;
                currentUser = new User(dataSnapshot);
                initAllUsers();
                for (Callback callback : getUserCallbacks) {
                    callback.complete(null);
                }
                getUserCallbacks.clear();
                currentUser.initFriendList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Get data error: " + databaseError.getDetails());
                completion.complete("Get data error");
            }
        });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public FirebaseUser getFirebaseUserUser() {
        return mFirebaseUser;
    }

    public String getFirstName(User user) {
        return !StringUtils.isEmpty(user.firstName) ? user.firstName : user.pingID;
    }

    public void initAllUsers() {
        allUsers = new ArrayList<>();
        mDatabase.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = new User(dataSnapshot);
                if (StringUtils.isEmpty(user.pingID)) {
                    return;
                }
                if (!user.key.equals(mFirebaseUser.getUid())) {
                    updateTypeFriend(user);
                    allUsers.add(user);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                User user = new User(dataSnapshot);
                for (int i = 0; i < allUsers.size(); i++) {
                    if (allUsers.get(i).key.equals(user.key)) {
                        allUsers.set(i, user);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public ArrayList<User> getAllUsers() {
        for (User user : allUsers) {
            updateTypeFriend(user);
        }
        return allUsers;
    }

    public void getUser(String id, Callback completion) {
        for (User user : allUsers) {
            if (user.key.equals(id)) {
                completion.complete(null, user);
                return;
            }
        }

        if (currentUser != null && currentUser.key.equals(id)) {
            completion.complete(null, currentUser);
            return;
        }

        mDatabase.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(String.format("users.%s addListenerForSingleValueEvent onDataChange key: %s",
                        id, dataSnapshot.getKey()));
                User user = new User(dataSnapshot);
                completion.complete(null, user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                completion.complete("Get data error");
            }
        });
    }

    public void updateShowMappingConfirm(Boolean value) {
        currentUser.showMappingConfirm = value;
        mDatabase.child("users").child(currentUser.key).child("show_mapping_confirm").setValue(value);
    }

    public void updatePhone(String phone) {
        currentUser.phone = phone;
        mDatabase.child("users").child(currentUser.key).child("phone").setValue(phone);
    }

    public void updateProfile(String profile) {
        currentUser.profile = profile;
        mDatabase.child("users").child(currentUser.key).child("profile").setValue(profile);
    }

    public void updateQuickBlox(Long quickBloxID) {
        currentUser.quickBloxID = quickBloxID;
        mDatabase.child("users").child(currentUser.key).child("quickBloxID").setValue(quickBloxID);
    }

    public void updateLoginStatus(Boolean status) {
        if (currentUser != null) {
            currentUser.loginStatus = status;
            mDatabase.child("users").child(currentUser.key).child("loginStatus").setValue(status);
        }
        if (!status) {
            currentUser = null;
        }
    }

    public Boolean getLoginStatus(String userID) {
        for (User user : allUsers) {
            if (user.key.equals(userID)) {
                if (user.loginStatus != null)
                    return user.loginStatus;
                return false;
            }
        }
        return false;
    }


//    public List<Bitmap> getProfileImage(List<User> members) {
//        // TODO check later
//        //TODO handle when there is more than 4 users
//        ArrayList<Bitmap> bitmaps = new ArrayList<>();
////        for (int i = 0; i < members.size() && i < 4; i++) {
////            bitmaps.add(getProfileImage(members.get(i)));
////        }
//        bitmaps.add(defaultProfileImage);
//        return bitmaps;
//    }

    public void addContact(User contact) {
        mDatabase.child("users").child(currentUser.key).child("friends").child(contact.key).setValue(true);
        currentUser.friends.put(contact.key, true);
        contact.typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
        currentUser.friendList.add(contact);
    }

    public void deleteContact(User contact) {
        mDatabase.child("users").child(currentUser.key).child("friends").child(contact.key).setValue(null);
        currentUser.friends.remove(contact.key);
        contact.typeFriend = Constant.TYPE_FRIEND.NON_FRIEND;
        currentUser.friendList.remove(contact);
    }

    public void updateBlock(User user, Boolean block) {
        updateBlock(user.key, block);
    }

    public void updateBlock(String userID, Boolean block) {
        if(block) {
            mDatabase.child("users").child(currentUser.key).child("blocks").child(userID).setValue(true);
            currentUser.blocks.put(userID, true);
        } else {
            mDatabase.child("users").child(currentUser.key).child("blocks").child(userID).setValue(null);
            currentUser.blocks.remove(userID);
        }
    }

    public boolean isBlockBy(User contact) {
        boolean isBlocked = false;
        String currentUserId = currentUser.key;
        if (contact != null && contact.blocks != null && contact.blocks.containsKey(currentUserId)) {
            isBlocked = contact.blocks.get(currentUserId);
        }
        return isBlocked;
    }

    public boolean isBlock(String userID) {
        if(currentUser.blocks == null || currentUser.blocks.containsKey(userID)) {
            return currentUser.blocks.get(userID);
        }
        return false;
    }

    private void updateTypeFriend(User user) {
        user.typeFriend = Constant.TYPE_FRIEND.NON_FRIEND;
        if (currentUser != null && MapUtils.isNotEmpty(currentUser.friends)) {
            for (Object request : currentUser.friends.keySet()) {
                if (request.toString().equals(user.key)) {
                    user.typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
                    break;
                }
            }
        }
    }

    //-------------------------------------------------------
    // End User region
    //-------------------------------------------------------

    public Boolean getCurrentMarkStatus(Map<String, Boolean> markStatuses) {
        if (MapUtils.isEmpty(markStatuses) || !markStatuses.containsKey(currentUser.key)) {
            //TODO get current setting
            return false;
        }
        return markStatuses.get(currentUser.key);
    }

    public Boolean getCurrentMarkStatus(Map<String, Boolean> markStatuses, Map<String, Boolean> maskMessages) {
        if (MapUtils.isEmpty(markStatuses) || !markStatuses.containsKey(currentUser.key)) {
            return getMaskSetting(maskMessages);
        }

        return markStatuses.get(currentUser.key);
    }

    public Boolean getCurrentDeleteStatus(Map<String, Boolean> deleteStatuses) {
        if (MapUtils.isEmpty(deleteStatuses) || !deleteStatuses.containsKey(currentUser.key)) {
            return false;
        }
        return deleteStatuses.get(currentUser.key);
    }

    public Boolean getCurrentReadStatus(Map<String, Boolean> readStatuses) {
        if (MapUtils.isEmpty(readStatuses) || !readStatuses.containsKey(currentUser.key)) {
            return false;
        }
        return readStatuses.get(currentUser.key);
    }

    public Long getCurrentStatus(Map<String, Long> statuses) {
        if (MapUtils.isEmpty(statuses) || !statuses.containsKey(currentUser.key)) {
            return Constant.MESSAGE_STATUS_SENT;
        }
        return statuses.get(currentUser.key);
    }

    public Boolean getNotificationsSetting(Map<String, Boolean> notifications) {
        if (MapUtils.isEmpty(notifications) || !notifications.containsKey(currentUser.key)) {
            return currentUser.settings.notification;
        }
        return notifications.get(currentUser.key);
    }

    public Boolean getMaskSetting(Map<String, Boolean> maskMessages) {
        if (MapUtils.isEmpty(maskMessages) || !maskMessages.containsKey(currentUser.key)) {
            return false;
        }
        return maskMessages.get(currentUser.key);
    }

    public Boolean getPuzzleSetting(Map<String, Boolean> puzzleMessages) {
        if (MapUtils.isEmpty(puzzleMessages) || !puzzleMessages.containsKey(currentUser.key)) {
            return false;
        }
        return puzzleMessages.get(currentUser.key);
    }

    public Boolean getMaskOutputSetting(Map<String, Boolean> maskOutputs) {
        if (MapUtils.isEmpty(maskOutputs) || !maskOutputs.containsKey(currentUser.key)) {
            return false;
        }
        return maskOutputs.get(currentUser.key);
    }

    //-------------------------------------------------------
    // Start Group region
    //-------------------------------------------------------
    public void listenGroupChange(String userId, ChildEventListener listener) {
        mDatabase.child("users").child(userId).child("groups").addChildEventListener(listener);
    }

    public void stopListenGroupChange(String userId, ChildEventListener listener) {
        mDatabase.child("users").child(userId).child("groups").removeEventListener(listener);
    }

    public String getGroupKey() {
        return mDatabase.child("groups").push().getKey();
    }

    public void createGroup(Group group) {
        mDatabase.child("groups").child(group.key).setValue(group.toMap());

        for (String userId : group.memberIDs.keySet()) {
            mDatabase.child("users").child(userId).child("groups").child(group.key).setValue(group.toMap());
        }
    }

    public void getGroup(String id, Callback completion) {
        mDatabase.child("users").child(currentUser.key).child("groups").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = Group.from(dataSnapshot);
                completion.complete(null, group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                completion.complete("Get data error");
            }
        });
    }

    public void deleteGroup(List<Group> groups) {
        for (Group group : groups) {
            mDatabase.child("groups").child(group.key).child("deleteStatuses").child(currentUser.key).setValue(true);
            for (User user : group.members) {
                mDatabase.child("users").child(user.key).child("groups").child(group.key).
                        child("deleteStatuses").child(currentUser.key).setValue(true);
            }
        }
    }

    public void leaveGroup(List<Group> groups) {
        deleteGroup(groups);
        deleteConversationInGroup(groups);
        for (Group group : groups) {
            mDatabase.child("groups").child(group.key).child("memberIDs").child(currentUser.key).setValue(null);
            for (User user : group.members) {
                mDatabase.child("users").child(user.key).child("groups").child(group.key).
                        child("memberIDs").child(currentUser.key).setValue(null);
            }
        }
    }

    public void addMember(Group group, ArrayList<String>  userIDs) {
        for(String userID : userIDs) {
            group.memberIDs.put(userID, true);
        }

        for(String userID : userIDs) {
            mDatabase.child("groups").child(group.key).child("memberIDs").child(userID).setValue(true);
            for (User user : group.members) {
                mDatabase.child("users").child(user.key).child("groups").child(group.key).
                        child("memberIDs").child(userID).setValue(true);
            }
            mDatabase.child("users").child(userID).child("groups").child(group.key).setValue(group);
        }
    }

    public void renameGroup(Group group, String name) {
        mDatabase.child("groups").child(group.key).child("groupName").setValue(name);
        for(String userID : group.memberIDs.keySet()) {
            mDatabase.child("users").child(userID).child("groups").child(group.key).child("groupName").setValue(name);
        }
    }

    public void updateGroupAvatar(String groupId, Set<String> memberIDs, String value) {
        mDatabase.child("groups").child(groupId).child("groupAvatar").setValue(value);
        for (String userId : memberIDs) {
            mDatabase.child("users").child(userId).child("groups").child(groupId).child("groupAvatar").setValue(value);
        }
    }

    public void uploadGroupAvatar(String groupId, File file, Callback callback) {
        String fileName = System.currentTimeMillis() + groupId + ".png";
        String imageStoragePath = "group" + File.separator + groupId + File.separator + fileName;
        StorageReference photoRef = storage.getReferenceFromUrl(Constant.URL_STORAGE_REFERENCE).child(imageStoragePath);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(file));
        uploadTask.addOnFailureListener(e -> {
            e.printStackTrace();
            callback.complete(e);
        }).addOnSuccessListener(taskSnapshot -> {
            String downloadUrl = Constant.URL_STORAGE_REFERENCE + "/" + taskSnapshot.getMetadata().getPath();
            callback.complete(null, downloadUrl);
        });
    }

    //-------------------------------------------------------
    // Start Mapping region
    //-------------------------------------------------------
    public Map<String, String> getDefaultMapping() {
        Map<String, String> mappings = new HashMap<>();
        for (int i = 0; i < 26; i++) {
            String mapKey = "" + (char) ('A' + i);
            mappings.put(mapKey, "");
        }
        return mappings;
    }

    public List<Mapping> getDefaultMappingList() {
        List<Mapping> mappings = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            String mapKey = "" + (char) ('A' + i);
            Mapping mapping = new Mapping(mapKey, "");
            mappings.add(mapping);
        }
        Collections.sort(mappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping lhs, Mapping rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.mapKey.compareTo(rhs.mapKey);
            }
        });
        return mappings;
    }

    public void updateMapping(Map<String, String> mappings) {
        currentUser.mappings = mappings;
        mDatabase.child("users").child(currentUser.key).child("mappings").setValue(mappings);
    }

    public Map<String, String> getMappingFromList(List<Mapping> mappings) {
        Map<String, String> mappingMap = new HashMap<>();
        for (Mapping mapping : mappings) {
            mappingMap.put(mapping.mapKey, mapping.mapValue);
        }
        return mappingMap;
    }

    public List<Mapping> getListFromMapping(Map<String, String> mappingMap) {
        List<Mapping> mappings = new ArrayList<>();
        for (Map.Entry<String, String> entry : mappingMap.entrySet()) {
            Mapping mapping = new Mapping();
            mapping.mapKey = entry.getKey();
            mapping.mapValue = entry.getValue();
            mappings.add(mapping);
        }
        Collections.sort(mappings, new Comparator<Mapping>() {
            @Override
            public int compare(Mapping lhs, Mapping rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.mapKey.compareTo(rhs.mapKey);
            }
        });
        return mappings;
    }

    // Setting region
    public Setting getDefaultSetting() {
        Setting setting = new Setting(false, false);
        return setting;
    }

    public void updateSetting(Setting setting) {
        currentUser.settings = setting;
        mDatabase.child("users").child(currentUser.key).child("settings").setValue(setting.toMap());
    }

    // Conversation region
    public void updateConversationReadStatus(Conversation conversation, Boolean isRead) {
        mDatabase.child("conversations").child(conversation.key).child("readStatuses").child(currentUser.key).setValue(true);
        mDatabase.child("users").child(currentUser.key).child("conversations").child(conversation.key).child("readStatuses").child(currentUser.key).setValue(true);
    }

    public void updateConversationReadStatus(List<Conversation> conversations, Boolean isRead) {
        for (Conversation conversation : conversations) {
            updateConversationReadStatus(conversation, isRead);
        }
    }

    public void deleteConversation(List<Conversation> conversations) {
        for (Conversation conversation : conversations) {
            mDatabase.child("conversations").child(conversation.key).child("deleteStatuses").child(currentUser.key).setValue(true);
            mDatabase.child("messages").child(conversation.key).setValue(null);
            for (User user : conversation.members) {
                mDatabase.child("users").child(user.key).child("conversations").child(conversation.key).
                        child("deleteStatuses").child(currentUser.key).setValue(true);
                // TODO logical delete message belong conversation
                // mDatabase.child("users").child(user.key).child("messages").child(conversation.key).setValue(null);
            }
        }
    }

    public void deleteConversationInGroup(List<Group> groups) {
        for (Group group : groups) {
            if(StringUtils.isEmpty(group.conversationID))
                continue;
            mDatabase.child("conversations").child(group.conversationID).child("deleteStatuses").child(currentUser.key).setValue(true);
            mDatabase.child("messages").child(group.conversationID).setValue(null);
            for (User user : group.members) {
                mDatabase.child("users").child(user.key).child("conversations").child(group.conversationID).
                        child("deleteStatuses").child(currentUser.key).setValue(true);
                // TODO logical delete message belong conversation
                // mDatabase.child("users").child(user.key).child("messages").child(conversation.key).setValue(null);
            }
        }
    }

    public void getConversationData(String fromUserId, String toUserId, Callback completion) {
        getConversationData(fromUserId + toUserId, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                if(error == null) {
                    Conversation conversation = (Conversation) data[0];
                    completion.complete(null, conversation);
                }else {
                    getConversationData(toUserId + fromUserId, new Callback() {
                        @Override
                        public void complete(Object error, Object... data) {
                            if(error == null) {
                                Conversation conversation = (Conversation) data[0];
                                completion.complete(null, conversation);
                            }else {
                                completion.complete("Error");
                            }
                        }
                    });
                }
            }
        });
    }

    public void getConversationData(String conversationID, Callback completion) {
        mDatabase.child("users").child(currentUser.key).child("conversations").child(conversationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Conversation conversation = Conversation.from(dataSnapshot);
                    completion.complete(null, conversation);
                } else {
                    completion.complete("Error");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void changeNotificationConversation(Conversation conversation, Boolean data) {
        if(conversation.notifications == null) {
            conversation.notifications = new HashMap<>();
        }
        conversation.notifications.put(currentUser.key, data);
        mDatabase.child("conversations").child(conversation.key).child("notifications").child(currentUser.key).setValue(data);
        for(String userID: conversation.memberIDs.keySet()) {
            mDatabase.child("users").child(userID).child("conversations").child(conversation.key).child("notifications").child(currentUser.key).setValue(data);
        }
    }

    public void changeMaskConversation(Conversation conversation, Boolean data) {
        if(conversation.maskMessages == null) {
            conversation.maskMessages = new HashMap<>();
        }
        conversation.maskMessages.put(currentUser.key, data);
        mDatabase.child("conversations").child(conversation.key).child("maskMessages").child(currentUser.key).setValue(data);
        for(String userID: conversation.memberIDs.keySet()) {
            mDatabase.child("users").child(userID).child("conversations").child(conversation.key).child("maskMessages").child(currentUser.key).setValue(data);
        }
    }

    public void changePuzzleConversation(Conversation conversation, Boolean data) {
        if(conversation.puzzleMessages == null) {
            conversation.puzzleMessages = new HashMap<>();
        }
        conversation.puzzleMessages.put(currentUser.key, data);
        mDatabase.child("conversations").child(conversation.key).child("puzzleMessages").child(currentUser.key).setValue(data);
        for(String userID: conversation.memberIDs.keySet()) {
            mDatabase.child("users").child(userID).child("conversations").child(conversation.key).child("puzzleMessages").child(currentUser.key).setValue(data);
        }
    }

    public void changeMaskOutputConversation(Conversation conversation, Boolean data) {
        if(conversation.maskOutputs == null) {
            conversation.maskOutputs = new HashMap<>();
        }
        conversation.maskOutputs.put(currentUser.key, data);
        mDatabase.child("conversations").child(conversation.key).child("maskOutputs").child(currentUser.key).setValue(data);
        for(String userID: conversation.memberIDs.keySet()) {
            mDatabase.child("users").child(userID).child("conversations").child(conversation.key).child("maskOutputs").child(currentUser.key).setValue(data);
        }
    }

    //Call region
    public void deleteCalls(List<Call> calls) {
        if (CollectionUtils.isEmpty(calls)) {
            return;
        }
        for (Call call : calls) {
            deleteCall(call);
        }
    }

    public void deleteCall(Call call) {
        mDatabase.child("calls").child(call.key).child("deleteStatuses").child(currentUser.key).setValue(true);
        mDatabase.child("users").child(currentUser.key).child("calls").child(call.key).child("deleteStatuses").
                child(currentUser.key).setValue(true);
    }

    // Chat region
    public void initMembers(Map<String, Boolean> memberIDs, Callback completion) {
        List<User> members = new ArrayList<>();
        List<String> finishIDs = new ArrayList<>();
        List<String> getIDs = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : memberIDs.entrySet()) {
            String id = entry.getKey();
            Boolean exist = entry.getValue();
            if (exist)
                getIDs.add(id);
        }

        for (String id : getIDs) {
            getUser(id, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    User user = (User) data[0];
                    members.add(user);
                    finishIDs.add(user.key);
                    if (finishIDs.size() == memberIDs.size()) {
                        completion.complete(null, members);
                    }
                }
            });
        }
    }

    public void initMembers(List<String> memberIDs, Callback completion) {
        List<User> members = new ArrayList<>();
        List<String> finishIDs = new ArrayList<>();

        for (String id : memberIDs) {
            getUser(id, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    User user = (User) data[0];
                    members.add(user);
                    finishIDs.add(user.key);
                    if (finishIDs.size() == memberIDs.size()) {
                        completion.complete(null, members);
                    }
                }
            });
        }
    }

    public void createConversationIDForPVPChat(String fromUserId, String toUserId, Callback completion) {
        String conversationID = fromUserId.compareTo(toUserId) > 0 ? fromUserId + toUserId : toUserId + fromUserId;
        mDatabase.child("conversations").equalTo(conversationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Conversation conversation = Conversation.createNewConversation(fromUserId, toUserId);
                    mDatabase.child("conversations").child(conversationID).setValue(conversation);
                    // Tuan - create conversation node in users/{userId}/conversations/{conversationId}
                    mDatabase.child("users").child(fromUserId).child("conversations").child(conversationID).setValue(conversation);
                }
                completion.complete(null, conversationID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                completion.complete(databaseError, conversationID);
            }
        });
    }

    public void createConversationForGroupChat(String fromUserId, Group group, Callback completion) {
        Conversation conversation = Conversation.createNewGroupConversation(fromUserId, group);
        String conversationKey = mDatabase.child("conversations").push().getKey();
        mDatabase.child("conversations").child(conversationKey).setValue(conversation);
        mDatabase.child("groups").child(group.key).child("conversationID").setValue(conversationKey);
        for (String userKey : group.memberIDs.keySet()) {
            mDatabase.child("users").child(userKey).child("groups").child(group.key).child("conversationID").setValue(conversationKey);
            mDatabase.child("users").child(userKey).child("conversations").child(conversationKey).setValue(conversation);
        }
        completion.complete(null, conversationKey);
    }

    public String encodeMessage(Context context, String message) {
        if (StringUtils.isEmpty(message))
            return message;
        Map<String, String> mappings = currentUser.mappings;

        String modifyMessage = message;
        modifyMessage = StringUtils.stripAccents(modifyMessage.toUpperCase());

        String returnMessage = "";
        String[] chars = message.split("");
        String[] modifyChars = modifyMessage.split("");
        for (int i = 0; i < modifyChars.length; i++) {
            Pattern p = Pattern.compile(emojiRegex);
            if (p.matcher(chars[i]).matches()) {
                returnMessage += chars[i];
                continue;
            }
            String key = modifyChars[i];
            if (mappings.containsKey(replaceSpecialChar(context, chars[i]))) {
                key = replaceSpecialChar(context, chars[i]);
            }
            if (mappings.containsKey(key) && !StringUtils.isEmpty(mappings.get(key))) {
                returnMessage += mappings.get(key);
            } else {
                returnMessage += chars[i];
            }
        }
        return returnMessage;
    }

    public String replaceSpecialChar(Context context, String msg) {
        if (StringUtils.isEmpty(msg)) {
            return msg;
        }
        if (context.getString(R.string.special_char_d_1).equals(msg) ||
                context.getString(R.string.special_char_d_2).equals(msg)) {
            return "D";
        }
        if ("đ".equals(msg) || "Đ".equals(msg)) {
            return "D";
        } else if (msg.equals("Œ") || msg.equals("ø")) {
            return "O";
        } else if (msg.equals("Æ")) {
            return "A";
        }
        return msg;
    }

    public void updateMarkStatus(String conversationID, String messageID, Boolean markStatus) {
        mDatabase.child("messages").child(conversationID).child(messageID).child("markStatuses").child(currentUser.key).setValue(markStatus);
        mDatabase.child("conversations").child(conversationID).child("markStatuses").child(currentUser.key).setValue(markStatus);
        mDatabase.child("users").child(currentUser.key).child("conversations").child(conversationID).child("markStatuses").child(currentUser.key).setValue(markStatus);
    }

    public void updateMessageStatus(String conversationID, String messageID, Long messageStatus) {
        mDatabase.child("messages").child(conversationID).child(messageID).child("status").
                child(currentUser.key).setValue(messageStatus);
    }

    public void deleteMessage(String conversationID, List<Message> messages) {
        for (Message message : messages) {
            mDatabase.child("messages").child(conversationID).child(message.key).child("deleteStatuses").child(currentUser.key).setValue(true);
        }
    }

    public void updateMessageMark(String conversationID, List<Message> messages, boolean mark) {
        for (Message message : messages) {
            mDatabase.child("messages").child(conversationID).child(message.key).child("markStatuses").child(currentUser.key).setValue(mark);
        }
    }

    // Network
    public boolean getNetworkStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    // Quick Box
    public void signUpNewUserQB() {
        QBUser qbUser = new QBUser();

        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(Constant.QB_PING_ROOM);
//        qbUser.setFullName(currentUser.pingID);
//        qbUser.setEmail(currentUser.email);
        qbUser.setLogin(currentUser.pingID);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
//        qbUser.setTags(userTags);

        requestExecutor.signUpNewUser(qbUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        updateQuickBlox(result.getId().longValue());
                        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
                        signInCreatedUser(qbUser, true);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(qbUser, true);
                        } else {
                            Toaster.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }

    public void signInQB() {
        signInCreatedUser(getQBUser(), true);
    }

    private QBUser getQBUser() {
        if (currentUser == null) {
            return null;
        }
        QBUser qbUser = new QBUser();
        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(Constant.QB_PING_ROOM);
        qbUser.setId(currentUser.quickBloxID.intValue());
//        qbUser.setFullName(currentUser.pingID);
//        qbUser.setEmail(currentUser.email);
        qbUser.setLogin(currentUser.pingID);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
//        qbUser.setTags(userTags);

        return qbUser;
    }

    private void signInCreatedUser(final QBUser user, final boolean deleteCurrentUser) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                //currentQBUser = result;
                //result.setPassword(Consts.DEFAULT_USER_PASSWORD);
                //saveQBUserData(result);
                result.setPassword(Consts.DEFAULT_USER_PASSWORD);
                startCallService(result);
                //getAllQBUsers();
            }

            @Override
            public void onError(QBResponseException responseException) {
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

//    private void saveQBUserData(QBUser qbUser) {
//        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
//        sharedPrefsHelper.save(Constant.QB_PING_ROOM, qbUser.getTags().get(0));
//        sharedPrefsHelper.saveQbUser(qbUser);
//    }

    public void loadQBUser() {
        startCallService(getQBUser());
//        if (sharedPrefsHelper.hasQbUser()) {
//            //currentQBUser = sharedPrefsHelper.getQbUser();
//            //allQBUsers = dbManager.getAllUsers();
//            //getAllQBUsers();
//            startCallService(sharedPrefsHelper.getQbUser());
//        }
    }

//    private void getAllQBUsers() {
//        requestExecutor.loadUsersByTag(Constant.QB_PING_ROOM, new QBEntityCallback<ArrayList<QBUser>>() {
//            @Override
//            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
//                //allQBUsers = new ArrayList<QBUser>(result);
//                dbManager.saveAllUsers(result, true);
//            }
//
//            @Override
//            public void onError(QBResponseException responseException) {
//            }
//        });
//    }

    private void startCallService(QBUser qbUser) {
        Intent tempIntent = new Intent(ActivityLifecycle.getForegroundActivity(), CallService.class);
        PendingIntent pendingIntent = ActivityLifecycle.getForegroundActivity().createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(ActivityLifecycle.getForegroundActivity(), qbUser, pendingIntent);
    }

//    public void logoutQB() {
//        UsersUtils.removeUserData(App.getInstance().getApplicationContext());
////        requestExecutor.deleteCurrentUser(currentQBUser.getId(), new QBEntityCallback<Void>() {
////            @Override
////            public void onSuccess(Void aVoid, Bundle bundle) {
////                CallService.logout(App.getInstance().getApplicationContext());
////                Log.d(TAG, "Current user was deleted from QB");
////            }
////
////            @Override
////            public void onError(QBResponseException e) {
////                Log.e(TAG, "Current user wasn't deleted from QB " + e);
////            }
////        });
//    }

    //    public QBUser getQBUserByPingID(String pingID) {
//        for (QBUser qbUser : allQBUsers) {
//            if (qbUser.getFullName().equals(pingID))
//                return qbUser;
//        }
//        return null;
//    }
//
    public User getUserByQBId(Integer ID) {

        for (User user : allUsers) {
            if (user.quickBloxID != null && user.quickBloxID.equals(ID.longValue())) {
                return user;
            }
        }
        return null;
    }

    public void sendCallingNotificationToUser(int quickBloxId, String callType){
        String messageData = String.format("%s is %s calling", ServiceManager.getInstance().currentUser.getDisplayName(), callType);
        JsonObject object = new JsonObject();
        object.addProperty("message", messageData);
        object.addProperty("ios_badge", "1");
        object.addProperty("ios_sound", "default");
        object.addProperty("notificationType", "incoming_call");
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(quickBloxId);
        //event.setUserId(quickBloxId);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        //add more props
        //event.setPushType(QBPushType.APNS);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        QBPushNotifications.createEvent(event).performAsync(new QBEntityCallback<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(e);
            }
        });




    }
}

