package com.ping.android.utils.configs;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constant {
    public static final String START_ACTIVITY_GROUP_ID = "GROUP_ID";
    public static final String START_ACTIVITY_USER_ID = "USER_ID";

    public static final int IMAGE_GALLERY_REQUEST = 1;
    public static final int GAME_GALLERY_REQUEST = 2;
    public static final int PLACE_PICKER_REQUEST = 3;
    public static final int SELECT_CONTACT_REQUEST = 4;
    public static final int SELECT_IMAGE_REQUEST = 7;
    public static final int CROP_IMAGE_REQUEST = 8;

    public static final int MSG_TYPE_TEXT = 1;
    public static final int MSG_TYPE_IMAGE = 2;
    public static final int MSG_TYPE_VOICE = 3;
    public static final int MSG_TYPE_GAME = 4;
    public static final int MSG_TYPE_VIDEO = 5;
    public static final int MSG_TYPE_CALL = 6;
    public static final int MSG_TYPE_IMAGE_GROUP = 7;
    public static final int MSG_TYPE_STICKER = 8;
    public static final int MSG_TYPE_GIFS = 9;
    public static final int MSG_TYPE_SYSTEM = 11;

    public static final int MSG_TYPE_TYPING = 10;
    public static final int MSG_TYPE_PADDING = 11;

    public static final int CONVERSATION_TYPE_INDIVIDUAL = 1;
    public static final int CONVERSATION_TYPE_GROUP = 2;

    public static final int MESSAGE_STATUS_HIDE = -1;
    public static final int MESSAGE_STATUS_SENT = 0;
    public static final int MESSAGE_STATUS_DELIVERED = 1;
    public static final int MESSAGE_STATUS_ERROR = 2;
    public static final int MESSAGE_STATUS_GAME_PASS = 3;
    public static final int MESSAGE_STATUS_GAME_FAIL = 4;
    public static final int MESSAGE_STATUS_READ = 5;
    public static final int MESSAGE_STATUS_GAME_DELIVERED = 6;

    @IntDef({CALL_STATUS_MISS, CALL_STATUS_SUCCESS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CallStatus {}

    public static final int CALL_STATUS_SUCCESS = 0;
    public static final int CALL_STATUS_MISS = 1;

    public static final int NOTIFICATION_NEW_MSG = 0;

    public static final int IMAGE_LIMIT_WIDTH = 512;
    public static final int GAME_LIMIT_TIME = 30000;

    //public static final String URL_STORAGE_REFERENCE = "gs://ping-android-dev.appspot.com";

    public static final String PREFS_NAME = "PingPrefsFile";
    public static final String PREFS_KEY_MESSAGE_COUNT = "MESSAGE_COUNT";
    public static final String PREFS_KEY_MISSED_CALL_COUNT = "MISSED_CALL_COUNT";
    public static final String PREFS_KEY_MISSED_CALL_TIMESTAMP = "MISSED_CALL_TIMESTAMP";

    public static final String QB_PING_ROOM = "mnb";
    public static final String QB_ACC_PASS = "QB_ACC_PASS";

    public static final int LATEST_RECENT_MESSAGES = 50;
    public static final int LOAD_MORE_MESSAGE_AMOUNT = 20;

    public static final double MILLISECOND_PER_DAY = 1000 * 60 * 60 * 24;

    public enum TYPE_FRIEND {
        NON_FRIEND,
        IS_FRIEND
    }

    public static String URL_TERMS_OF_SERVICE = "http://www.bzzz.chat/terms-of-service/";
    public static String URL_PRIVACY = "http://www.bzzz.chat/privacy/";
    public static String URL_HELP = "http://www.bzzz.chat/help/";
}
