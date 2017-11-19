package com.ping.android.ultility;

public class Constant {
    public  static final String START_ACTIVITY_GROUP_ID = "GROUP_ID";
    public  static final String START_ACTIVITY_USER_ID = "USER_ID";

    public static final int IMAGE_GALLERY_REQUEST = 1;
    public static final int GAME_GALLERY_REQUEST = 2;
    public static final int PLACE_PICKER_REQUEST = 3;
    public static final int SELECT_CONTACT_REQUEST = 4;
    public static final int SELECT_IMAGE_REQUEST = 7;
    public static final int CROP_IMAGE_REQUEST = 8;

    public static final Long MSG_TYPE_TEXT = 1L;
    public static final Long MSG_TYPE_IMAGE = 2L;
    public static final Long MSG_TYPE_VOICE = 3L;
    public static final Long MSG_TYPE_GAME = 4L;
    public static final Long MSG_TYPE_TYPING = 10L;

    public static final Long CONVERSATION_TYPE_INDIVIDUAL = 1L;
    public static final Long CONVERSATION_TYPE_GROUP = 2L;

    public static final Long MESSAGE_STATUS_HIDE = -1L;
    public static final Long MESSAGE_STATUS_SENT = 0L;
    public static final Long MESSAGE_STATUS_DELIVERED = 1L;
    public static final Long MESSAGE_STATUS_ERROR = 2L;
    public static final Long MESSAGE_STATUS_GAME_PASS = 3L;
    public static final Long MESSAGE_STATUS_GAME_FAIL = 4L;
    public static final Long MESSAGE_STATUS_GAME_INIT = 5L;
    public static final Long MESSAGE_STATUS_GAME_DELIVERED = 6L;

    public static final Long CALL_STATUS_SUCCESS = 0L;
    public static final Long CALL_STATUS_MISS = 1L;

    public static final int NOTIFICATION_NEW_MSG = 0;

    public static final int IMAGE_LIMIT_WIDTH = 512;
    public static final int GAME_LIMIT_TIME = 30000;

    public static final String URL_STORAGE_REFERENCE = "gs://ping-android-dev.appspot.com";

    public static final String PREFS_NAME = "PingPrefsFile";
    public static final String PREFS_KEY_MESSAGE_COUNT = "MESSAGE_COUNT";

    public static final String QB_PING_ROOM = "mnb";
    public static final String QB_ACC_PASS = "QB_ACC_PASS";

    public enum TYPE_FRIEND {
        NON_FRIEND,
        IS_FRIEND
    }

    public enum MESSAGE_TYPE {
        TEXT,
        IMAGE,
        VOICE,
        GAME
    }

    public enum NETWORK_STATUS {
        CONNECTED,
        CONNECTING,
        NOCONNECT
    }

    public static String URL_TERMS_OF_SERVICE = "http://www.bzzz.chat/terms-of-service/";
    public static String URL_PRIVACY = "http://www.bzzz.chat/privacy/";
    public static String URL_HELP = "http://www.bzzz.chat/help/";
}
