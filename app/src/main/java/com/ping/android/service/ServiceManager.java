package com.ping.android.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ServiceManager {
    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager ourInstance = new ServiceManager();
    private final String emojiRegex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private User currentUser;
    //private QBUser currentQBUser;
    //ArrayList<QBUser> allQBUsers;

    private ServiceManager() {
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        storage = FirebaseStorage.getInstance();
    }

    public static ServiceManager getInstance() {
        return ourInstance;
    }

    //-------------------------------------------------------
    // Start User region
    //-------------------------------------------------------

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void updateShowMappingConfirm(Boolean value) {
        currentUser.showMappingConfirm = value;
        mDatabase.child("users").child(currentUser.key).child("show_mapping_confirm").setValue(value);
    }

    public void updatePhone(String phone) {
        currentUser.phone = phone;
        mDatabase.child("users").child(currentUser.key).child("phone").setValue(phone);
    }

//    public void updateProfile(String profile) {
//        currentUser.profile = profile;
//        mDatabase.child("users").child(currentUser.key).child("profile").setValue(profile);
//    }
//
//    public void updateQuickBlox(int quickBloxID) {
//        currentUser.quickBloxID = quickBloxID;
//        mDatabase.child("users").child(currentUser.key).child("quickBloxID").setValue(quickBloxID);
//    }

    public void updateLoginStatus(Boolean status) {
        if (currentUser != null) {
            currentUser.loginStatus = status;
            mDatabase.child("users").child(currentUser.key).child("loginStatus").setValue(status);
        }
        if (!status) {
            currentUser = null;
        }
        SharedPrefsHelper.getInstance().save("isLoggedIn", status);
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
        mDatabase.child("friends").child(currentUser.key).child(contact.key).setValue(true);
        //currentUser.friends.put(contact.key, true);
        contact.typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
        currentUser.friendList.add(contact);
    }

//    public void deleteContact(User contact) {
//        mDatabase.child("friends").child(currentUser.key).child(contact.key).setValue(null);
//        ///currentUser.friends.remove(contact.key);
//        contact.typeFriend = Constant.TYPE_FRIEND.NON_FRIEND;
//        currentUser.friendList.remove(contact);
//    }

    public boolean isBlockBy(User contact) {
        boolean isBlocked = false;
        String currentUserId = currentUser.key;
        if (contact != null && contact.blocks != null && contact.blocks.containsKey(currentUserId)) {
            isBlocked = contact.blocks.get(currentUserId);
        }
        return isBlocked;
    }

    public boolean isBlock(String userID) {
        if (currentUser.blocks == null || currentUser.blocks.containsKey(userID)) {
            return currentUser.blocks.get(userID);
        }
        return false;
    }

    //-------------------------------------------------------
    // End User region
    //-------------------------------------------------------

//    public Boolean getCurrentMarkStatus(Map<String, Boolean> markStatuses) {
//        if (MapUtils.isEmpty(markStatuses) || !markStatuses.containsKey(currentUser.key)) {
//            //TODO get current setting
//            return false;
//        }
//        return markStatuses.get(currentUser.key);
//    }

    public Boolean getCurrentMarkStatus(Map<String, Boolean> markStatuses, Map<String, Boolean> maskMessages) {
        if (markStatuses == null || !markStatuses.containsKey(currentUser.key)) {
            return getMaskSetting(maskMessages);
        }

        return markStatuses.get(currentUser.key);
    }

//    public Boolean getCurrentDeleteStatus(Map<String, Boolean> deleteStatuses) {
//        if (MapUtils.isEmpty(deleteStatuses) || !deleteStatuses.containsKey(currentUser.key)) {
//            return false;
//        }
//        return deleteStatuses.get(currentUser.key);
//    }
//
//    public Boolean getCurrentReadStatus(Map<String, Boolean> readStatuses) {
//        if (MapUtils.isEmpty(readStatuses) || !readStatuses.containsKey(currentUser.key)) {
//            return false;
//        }
//        return readStatuses.get(currentUser.key);
//    }

    public int getCurrentStatus(Map<String, Integer> statuses) {
        if (statuses == null || !statuses.containsKey(currentUser.key)) {
            return Constant.MESSAGE_STATUS_SENT;
        }
        return statuses.get(currentUser.key);
    }

//    public Boolean getNotificationsSetting(Map<String, Boolean> notifications) {
//        if (MapUtils.isEmpty(notifications) || !notifications.containsKey(currentUser.key)) {
//            return currentUser.settings.notification;
//        }
//        return notifications.get(currentUser.key);
//    }

    public Boolean getMaskSetting(Map<String, Boolean> maskMessages) {
        if (maskMessages == null || !maskMessages.containsKey(currentUser.key)) {
            return false;
        }
        return maskMessages.get(currentUser.key);
    }

//    public Boolean getPuzzleSetting(Map<String, Boolean> puzzleMessages) {
//        if (MapUtils.isEmpty(puzzleMessages) || !puzzleMessages.containsKey(currentUser.key)) {
//            return false;
//        }
//        return puzzleMessages.get(currentUser.key);
//    }

//    public Boolean getMaskOutputSetting(Map<String, Boolean> maskOutputs) {
//        if (MapUtils.isEmpty(maskOutputs) || !maskOutputs.containsKey(currentUser.key)) {
//            return false;
//        }
//        return maskOutputs.get(currentUser.key);
//    }

    //-------------------------------------------------------
    // Start Group region
    //-------------------------------------------------------
//    public void listenGroupChange(String userId, ChildEventListener listener) {
//        mDatabase.child("groups").child(userId).addChildEventListener(listener);
//    }
//
//    public void stopListenGroupChange(String userId, ChildEventListener listener) {
//        mDatabase.child("groups").child(userId).removeEventListener(listener);
//    }
//
//    public void getGroup(String id, Callback completion) {
//        mDatabase.child("groups").child(currentUser.key).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    Group group = Group.from(dataSnapshot);
//                    completion.complete(null, group);
//                } else {
//                    completion.complete(new NullPointerException());
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                completion.complete("Get data error");
//            }
//        });
//    }
//
//    public void deleteGroup(List<Group> groups) {
//        for (Group group : groups) {
//            mDatabase.child("groups").child(group.key).child("deleteStatuses").child(currentUser.key).setValue(true);
//            for (User user : group.members) {
//                mDatabase.child("groups").child(user.key).child(group.key).
//                        child("deleteStatuses").child(currentUser.key).setValue(true);
//            }
//        }
//    }
//
//    public void renameGroup(Group group, String name) {
//        mDatabase.child("groups").child(group.key).child("groupName").setValue(name);
//        for (String userID : group.memberIDs.keySet()) {
//            mDatabase.child("groups").child(userID).child(group.key).child("groupName").setValue(name);
//        }
//    }
//
//    public void updateGroupAvatar(String groupId, Set<String> memberIDs, String value) {
//        mDatabase.child("groups").child(groupId).child("groupAvatar").setValue(value);
//        for (String userId : memberIDs) {
//            mDatabase.child("groups").child(userId).child(groupId).child("groupAvatar").setValue(value);
//        }
//    }

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

//    public List<Mapping> getDefaultMappingList() {
//        List<Mapping> mappings = new ArrayList<>();
//        for (int i = 0; i < 26; i++) {
//            String mapKey = "" + (char) ('A' + i);
//            Mapping mapping = new Mapping(mapKey, "");
//            mappings.add(mapping);
//        }
//        Collections.sort(mappings, (lhs, rhs) -> {
//            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
//            return lhs.mapKey.compareTo(rhs.mapKey);
//        });
//        return mappings;
//    }

    public void updateMapping(Map<String, String> mappings) {
        currentUser.mappings = mappings;
        mDatabase.child("users").child(currentUser.key).child("mappings").setValue(mappings);
    }

//    public Map<String, String> getMappingFromList(List<Mapping> mappings) {
//        Map<String, String> mappingMap = new HashMap<>();
//        for (Mapping mapping : mappings) {
//            mappingMap.put(mapping.mapKey, mapping.mapValue);
//        }
//        return mappingMap;
//    }
//
//    public List<Mapping> getListFromMapping(Map<String, String> mappingMap) {
//        List<Mapping> mappings = new ArrayList<>();
//        for (Map.Entry<String, String> entry : mappingMap.entrySet()) {
//            Mapping mapping = new Mapping();
//            mapping.mapKey = entry.getKey();
//            Object value = entry.getValue();
//            mapping.mapValue = value.toString();
//            mappings.add(mapping);
//        }
//        Collections.sort(mappings, new Comparator<Mapping>() {
//            @Override
//            public int compare(Mapping lhs, Mapping rhs) {
//                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
//                return lhs.mapKey.compareTo(rhs.mapKey);
//            }
//        });
//        return mappings;
//    }

    // Setting region
//    public Setting getDefaultSetting() {
//        Setting setting = new Setting(true, false);
//        return setting;
//    }
//
//    public void updateSetting(Setting setting) {
//        currentUser.settings = setting;
//        mDatabase.child("users").child(currentUser.key).child("settings").setValue(setting.toMap());
//    }

    // Conversation region
//    public void updateConversationReadStatus(Conversation conversation, Boolean isRead) {
//        mDatabase.child("conversations").child(conversation.key).child("readStatuses").child(currentUser.key).setValue(true);
//        mDatabase.child("conversations").child(currentUser.key).child(conversation.key).child("readStatuses").child(currentUser.key).setValue(true);
//    }
//
//    public void updateConversationReadStatus(List<Conversation> conversations, Boolean isRead) {
//        for (Conversation conversation : conversations) {
//            updateConversationReadStatus(conversation, isRead);
//        }
//    }

//    public void deleteConversationInGroup(List<Group> groups) {
//        double timestamp = System.currentTimeMillis() / 1000d;
//        for (Group group : groups) {
//            if (StringUtils.isEmpty(group.conversationID))
//                continue;
//            mDatabase.child("conversations").child(group.conversationID).child("deleteStatuses").child(currentUser.key).setValue(true);
//            mDatabase.child("conversations").child(group.conversationID).child("deleteTimestamps").child(currentUser.key).setValue(timestamp);
//            //mDatabase.child("messages").child(group.conversationID).setValue(null);
//            for (User user : group.members) {
//                mDatabase.child("conversations").child(user.key).child(group.conversationID).
//                        child("deleteStatuses").child(currentUser.key).setValue(true);
//                mDatabase.child("conversations").child(user.key).child(group.conversationID).
//                        child("deleteTimestamps").child(currentUser.key).setValue(timestamp);
//                // TODO logical delete message belong conversation
//                // mDatabase.child("users").child(opponentUser.key).child("messages").child(conversation.key).setValue(null);
//            }
//        }
//    }

//    public void getConversationData(String fromUserId, String toUserId, Callback completion) {
//        getConversationData(fromUserId + toUserId, new Callback() {
//            @Override
//            public void complete(Object error, Object... data) {
//                if (error == null) {
//                    Conversation conversation = (Conversation) data[0];
//                    completion.complete(null, conversation);
//                } else {
//                    getConversationData(toUserId + fromUserId, new Callback() {
//                        @Override
//                        public void complete(Object error, Object... data) {
//                            if (error == null) {
//                                Conversation conversation = (Conversation) data[0];
//                                completion.complete(null, conversation);
//                            } else {
//                                completion.complete("Error");
//                            }
//                        }
//                    });
//                }
//            }
//        });
//    }

//    public void getConversationData(String conversationID, Callback completion) {
//        mDatabase.child("conversations").child(currentUser.key).child(conversationID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    Conversation conversation = Conversation.from(dataSnapshot);
//                    completion.complete(null, conversation);
//                } else {
//                    completion.complete("Error");
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }

    public void changeMaskOutputConversation(Conversation conversation, Boolean data) {
        if (conversation.maskOutputs == null) {
            conversation.maskOutputs = new HashMap<>();
        }
        conversation.maskOutputs.put(currentUser.key, data);
        mDatabase.child("conversations").child(conversation.key).child("maskOutputs").child(currentUser.key).setValue(data);
        if (conversation.members != null) {
            for (User user : conversation.members) {
                mDatabase.child("conversations").child(user.key).child(conversation.key).child("maskOutputs").child(currentUser.key).setValue(data);
            }
        }
    }

//    public Boolean getDeleteStatusConversation(Conversation conversation) {
//        if (MapUtils.isEmpty(conversation.deleteTimestamps) || !conversation.deleteTimestamps.containsKey(currentUser.key)) {
//            return false;
//        }
//        //conversation will not show if last message time stamp less than conversation deleted time
//        return conversation.deleteTimestamps.get(currentUser.key) > conversation.timesstamps;
//    }
//
//    public Double getLastDeleteTimeStamp(Conversation conversation) {
//        if (MapUtils.isEmpty(conversation.deleteTimestamps) || !conversation.deleteTimestamps.containsKey(currentUser.key)) {
//            return 0.0d;
//        }
//        Object value = conversation.deleteTimestamps.get(currentUser.key);
//        if (value instanceof Long) {
//            return ((Long) value).doubleValue();
//        } else {
//            return (Double) value;
//        }
//    }

    //Call region
//    public void deleteCalls(List<Call> calls) {
//        if (CollectionUtils.isEmpty(calls)) {
//            return;
//        }
//        for (Call call : calls) {
//            deleteCall(call);
//        }
//    }
//
//    public void deleteCall(Call call) {
//        mDatabase.child("calls").child(call.key).child("deleteStatuses").child(currentUser.key).setValue(true);
//        mDatabase.child("calls").child(currentUser.key).child(call.key).child("deleteStatuses").
//                child(currentUser.key).setValue(true);
//    }

    // Chat region

//    public void createConversationIDForPVPChat(String fromUserId, String toUserId, Callback completion) {
//        String conversationID = fromUserId.compareTo(toUserId) > 0 ? fromUserId + toUserId : toUserId + fromUserId;
//        mDatabase.child("conversations").child(fromUserId).child(conversationID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
//                    Conversation conversation = Conversation.createNewConversation(fromUserId, toUserId);
//                    mDatabase.child("conversations").child(conversationID).setValue(conversation);
//                    mDatabase.child("conversations").child(fromUserId).child(conversationID).setValue(conversation);
//                }
//                completion.complete(null, conversationID);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                completion.complete(databaseError, conversationID);
//            }
//        });
//    }

    public String encodeMessage(Context context, String message) {
        if (TextUtils.isEmpty(message))
            return message;
        Map<String, String> mappings = currentUser.mappings;

        String returnMessage = "";
        String[] chars = message.split("");

        for (int i = 0; i < chars.length; i++) {
            Pattern p = Pattern.compile(emojiRegex);
            if (p.matcher(chars[i]).matches()) {
                returnMessage += chars[i];
                continue;
            }
            String key = CommonMethod.foldToASCII(chars[i].toUpperCase());

            try {
                Object value = mappings.get(key);
                if (mappings.containsKey(key) && !TextUtils.isEmpty(value.toString())) {
                    returnMessage += value.toString();
                } else {
                    returnMessage += chars[i];
                }
            } catch (ClassCastException exception) {
                Log.e(key + "\n" + mappings.toString());
                exception.printStackTrace();
            }
        }
        return returnMessage;
    }

    public String encodeMessage(Map<String, String> mappings, String message) {
        if (TextUtils.isEmpty(message))
            return message;

        String modifyMessage = message;
        //modifyMessage = CommonMethod.foldToASCII(modifyMessage.toUpperCase().split(""));

        String returnMessage = "";
        String[] chars = message.split("");
        for (int i = 0; i < chars.length; i++) {
            Pattern p = Pattern.compile(emojiRegex);
            if (p.matcher(chars[i]).matches()) {
                returnMessage += chars[i];
                continue;
            }
            String key = CommonMethod.foldToASCII(chars[i].toUpperCase());
            Object value = mappings.get(key);
            if (value != null && !TextUtils.isEmpty(value.toString())) {
                returnMessage += mappings.get(key);
            } else {
                returnMessage += chars[i];
            }
        }
        return returnMessage;
    }

//    public String replaceSpecialChar(Context context, String msg) {
//        if (StringUtils.isEmpty(msg)) {
//            return msg;
//        }
//        if (context.getString(R.string.special_char_d_1).equals(msg) ||
//                context.getString(R.string.special_char_d_2).equals(msg)) {
//            return "D";
//        }
//        if ("đ".equals(msg) || "Đ".equals(msg)) {
//            return "D";
//        } else if (msg.equals("Œ") || msg.equals("ø")) {
//            return "O";
//        } else if (msg.equals("Æ")) {
//            return "A";
//        }
//        return msg;
//    }

    public void updateMarkStatus(String conversationID, String messageID, boolean markStatus) {
        mDatabase.child("messages").child(conversationID).child(messageID).child("markStatuses").child(currentUser.key).setValue(markStatus);
        mDatabase.child("conversations").child(conversationID).child("markStatuses").child(currentUser.key).setValue(markStatus);
        mDatabase.child("conversations").child(currentUser.key).child(conversationID).child("markStatuses").child(currentUser.key).setValue(markStatus);
    }

    public void updateMessageStatus(String conversationID, String messageID, int messageStatus) {
        mDatabase.child("messages").child(conversationID).child(messageID).child("status").
                child(currentUser.key).setValue(messageStatus);
    }

    // Network
    public boolean getNetworkStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}

