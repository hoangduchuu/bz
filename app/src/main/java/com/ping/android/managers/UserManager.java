package com.ping.android.managers;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by tuanluong on 12/6/17.
 */

public class UserManager {
    private static final String emojiRegex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";

    private User user;
    private Map<String, Boolean> friends;
    // Currently, users will be cached for later use.
    // Should improve by invalidate user after a certain of time
    private Map<String, User> cachedUsers = new HashMap<>();

    public UserManager() { }

    public void setUser(User user) {
        this.user = user;
        SharedPrefsHelper.getInstance().save("isLoggedIn", true);
        SharedPrefsHelper.getInstance().save("quickbloxId", user.quickBloxID);
        SharedPrefsHelper.getInstance().save("pingId", user.pingID);
    }

    public void logout() {
        SharedPrefsHelper.getInstance().save("isLoggedIn", false);
        SharedPrefsHelper.getInstance().save("quickbloxId", 0);
        SharedPrefsHelper.getInstance().save("pingId", "");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        user = null;
    }

    public String encodeMessage(String message) {
        if (TextUtils.isEmpty(message))
            return message;
        Map<String, String> mappings = user.mappings;

        String[] chars = message.split("");
        StringBuilder messageBuffer = new StringBuilder();
        for (String aChar : chars) {
            Pattern p = Pattern.compile(emojiRegex);
            if (p.matcher(aChar).matches()) {
                messageBuffer.append(aChar);
                continue;
            }
            String key = CommonMethod.foldToASCII(aChar.toUpperCase());

            try {
                Object value = mappings.get(key);
                if (mappings.containsKey(key) && !TextUtils.isEmpty(value.toString())) {
                    messageBuffer.append(value.toString());
                } else {
                    messageBuffer.append(aChar);
                }
            } catch (ClassCastException exception) {
                Log.e(key + "\n" + mappings.toString());
                exception.printStackTrace();
            }
        }
        return messageBuffer.toString();
    }
}
