package com.ping.android.presentation.view.tutorial;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Huu Hoang on 02/01/2019
 */
@Singleton
public class EmojiMapper {
     Map<String, String> maps = new HashMap<>();

    @Inject
    EmojiMapper() {
    }

    public  Map<String, String> getPeopleEmoji() {
        maps.put("A", "😃");
        maps.put("B", "👆");
        maps.put("C", "👒");
        maps.put("D", "👐");
        maps.put("E", "👩‍🔧");
        maps.put("F", "‍👩");
        maps.put("G", "😲");
        maps.put("H", "💇‍♂");
        maps.put("I", "😎");
        maps.put("J", "😯");
        maps.put("K", "😓");
        maps.put("L", "👩‍🍳");
        maps.put("M", "😂");
        maps.put("N", "👿");
        maps.put("O", "👩");
        maps.put("P", "😃");
        maps.put("Q", "🚶‍♀️");
        maps.put("R", "👶");
        maps.put("T", "👙");
        maps.put("U", "👩‍🍳");
        maps.put("W", "👨‍👦");
        maps.put("S", "👨‍🎓");
        maps.put("Y", "👩‍👩‍👧");
        maps.put("V", "😘");
        maps.put("Z", "💭");
        maps.put("X", "👩‍❤️‍👨");
        return maps;
    }
}