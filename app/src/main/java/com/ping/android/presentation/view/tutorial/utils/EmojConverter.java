package com.ping.android.presentation.view.tutorial.utils;

import android.text.TextUtils;

import com.ping.android.presentation.view.tutorial.EmojiMapper;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.Log;

import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Huu Hoang on 02/01/2019
 */
@Singleton
public class EmojConverter {
    private static final String emojiRegex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";
    @Inject
    public EmojConverter() { }

    @Inject
    EmojiMapper data;

    public String encodeMessage(String message) {
        if (TextUtils.isEmpty(message))
            return message;
        Map<String, String> mappings = data.getPeopleEmoji();

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


