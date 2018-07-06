package com.ping.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

public class BadgeHelper {
    private static final String BADGE_COUNT_CONVERSATION = "BADGE_COUNT_CONVERSATION";
    private static final String BADGE_COUNT_MISSED_CALL = "BADGE_COUNT_MISSED_CALL";
    private final SharedPreferences prefs;
    private Context context;
    private static Gson gson;

    public BadgeHelper(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void showBadge() {
        int total = totalMessageCount() + totalMissedCall();
        ShortcutBadger.applyCount(context, total);
    }

    public int totalMessageCount() {
        Map<String, Integer> badgeCounts = getBadgeCountMap();
        int sum = 0;
        for (String key : badgeCounts.keySet()) {
            sum = sum + badgeCounts.get(key);
        }
        return sum;
    }

    public int totalMissedCall() {
        return prefs.getInt(BADGE_COUNT_MISSED_CALL, 0);
    }

    public void read(String key) {
        Map<String, Integer> badgeCounts = getBadgeCountMap();
        if (badgeCounts.containsKey(key)) {
            badgeCounts.remove(key);
            setBadgeCounts(badgeCounts);
            showBadge();
        }
    }

    public void clearMissedCall() {
        prefs.edit().putInt(BADGE_COUNT_MISSED_CALL, 0).apply();
        showBadge();
    }

    public void increaseMissedCall() {
        int missedCall = prefs.getInt(BADGE_COUNT_MISSED_CALL, 0);
        prefs.edit().putInt(BADGE_COUNT_MISSED_CALL, missedCall + 1).apply();
        showBadge();
    }

    public void increaseBadgeCount(String key) {
        Map<String, Integer> badgeCounts = getBadgeCountMap();
        int value = 1;
        if (badgeCounts.containsKey(key)) {
            value = value + badgeCounts.get(key);
        }
        badgeCounts.put(key, value);
        setBadgeCounts(badgeCounts);
        this.showBadge();
    }

    private Map<String, Integer> getBadgeCountMap() {
        String json = prefs.getString(BADGE_COUNT_CONVERSATION, "");
        Map<String, Integer> ret = new HashMap<>();
        if (!TextUtils.isEmpty(json)) {
            gson = getGson();
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            ret = gson.fromJson(json, type);
        }
        return ret;
    }

    private void setBadgeCounts(Map<String, Integer> data) {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = getGson();
        editor.putString(BADGE_COUNT_CONVERSATION, gson.toJson(data));
        editor.apply();
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }
}
