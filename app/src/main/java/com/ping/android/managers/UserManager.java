package com.ping.android.managers;

import android.text.TextUtils;

import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 12/6/17.
 */
@Singleton
public class UserManager {
    private static final String emojiRegex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";

    @Inject
    UserRepository userRepository;

    private User user;
    private Map<String, Boolean> friends;
    // Currently, users will be cached for later use.
    // Should improve by invalidate user after a certain of time
    private Map<String, User> cachedUsers = new HashMap<>();

    @Inject
    public UserManager() { }

    public Observable<User> getCurrentUser() {
        if (user != null) {
            return Observable.just(user);
        } else {
            return userRepository.getCurrentUser();
        }
    }

    public void setUser(User user) {
        // Keep friends data
        if (user != null) {
            user.friends = this.friends;
        }
        this.user = user;
        SharedPrefsHelper.getInstance().save("isLoggedIn", true);
        SharedPrefsHelper.getInstance().save("quickbloxId", user.quickBloxID);
        SharedPrefsHelper.getInstance().save("pingId", user.pingID);
    }

    public void setFriendsData(Map<String, Boolean> map) {
        this.friends = map;
        if (this.user != null) {
            this.user.friends = map;
        }
    }

    public void setCachedUser(User user) {
        cachedUsers.put(user.key, user);
    }

    public User getCacheUser(String userId) {
        return cachedUsers.get(userId);
    }

    public Observable<User> getUser(String userId) {
        User user = getCacheUser(userId);
        if (user != null) {
            Log.e("User was cached " + user.getDisplayName());
            return Observable.just(user);
        } else {
            return userRepository.getUser(userId)
                    .doOnNext(this::setCachedUser);
        }
    }

    public Observable<List<User>> getUserList(Map<String, Boolean> userIds) {
        return Observable.fromArray(userIds.keySet().toArray())
                .flatMap(userId -> getUser((String) userId))
                .take(userIds.size())
                .toList()
                .toObservable();
    }

    public void logout() {
        SharedPrefsHelper.getInstance().save("isLoggedIn", false);
        SharedPrefsHelper.getInstance().save("quickbloxId", 0);
        SharedPrefsHelper.getInstance().save("pingId", "");
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
