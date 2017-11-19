package com.ping.android.utils;

import android.content.Context;
import android.text.TextUtils;

import com.ping.android.db.QbUsersDbManager;
import com.ping.android.service.ServiceManager;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 09.06.16.
 */
public class UsersUtils {

    private static SharedPrefsHelper sharedPrefsHelper;
    private static QbUsersDbManager dbManager;

    public static String[] MAPING_KEY_CHARACTERS;
    public static String[] MAPING_VALUE_CHARACTERS;

    static {
        MAPING_KEY_CHARACTERS = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        MAPING_VALUE_CHARACTERS = new String[] {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9","R","S","T","U","V","W","X","Y","Z","0","1","!","@","#","$","%","^","&","*","(",")","-","_","+","=",",",".","/","?","|","{","[","]","}","<",">","~"};
    }

    public static String getUserNameOrId(QBUser qbUser, Integer userId) {
        if (qbUser == null) {
            return String.valueOf(userId);
        }

        String fullName = qbUser.getFullName();

        return TextUtils.isEmpty(fullName) ? String.valueOf(userId) : fullName;
    }

    public static ArrayList<QBUser> getListAllUsersFromIds(ArrayList<QBUser> existedUsers, List<Integer> allIds) {
        ArrayList<QBUser> qbUsers = new ArrayList<>();


        for (Integer userId : allIds) {
            QBUser stubUser = createStubUserById(userId);
            if (!existedUsers.contains(stubUser)) {
                qbUsers.add(stubUser);
            }
        }

        qbUsers.addAll(existedUsers);

        return qbUsers;
    }

    private static QBUser createStubUserById(Integer userId) {
        QBUser stubUser = new QBUser(userId);
        stubUser.setFullName(String.valueOf(userId));
        return stubUser;
    }

    public static ArrayList<Integer> getIdsNotLoadedUsers(ArrayList<QBUser> existedUsers, List<Integer> allIds) {
        ArrayList<Integer> idsNotLoadedUsers = new ArrayList<>();

        for (Integer userId : allIds) {
            QBUser stubUser = createStubUserById(userId);
            if (!existedUsers.contains(stubUser)) {
                idsNotLoadedUsers.add(userId);
            }
        }

        return idsNotLoadedUsers;
    }

    public static void removeUserData(Context context) {
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        sharedPrefsHelper.clearAllData();
        if (dbManager == null) {
            dbManager = QbUsersDbManager.getInstance(context);
        }
        dbManager.clearDB();
    }

    public static void randomizeTransphabet() {
        Map<String, String> mappings = new HashMap<>();
        List<String> values = Arrays.asList(UsersUtils.MAPING_VALUE_CHARACTERS);
        Collections.shuffle(values);
        String[] keys = UsersUtils.MAPING_KEY_CHARACTERS;
        for(int i = 0; i < keys.length; i++) {
            mappings.put(keys[i], values.get(i));
        }

        ServiceManager.getInstance().updateMapping(mappings);
    }
}