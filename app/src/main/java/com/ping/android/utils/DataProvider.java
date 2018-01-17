package com.ping.android.utils;

import com.ping.android.model.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataProvider {
    public static List<Language> getLanguages() {
        Language[] list = new Language[] {
                new Language("ar", "Arabic", "ضصثقفغعهخحجشسيبلتنمكةاءظطذدزروى"),
                new Language("bn", "Bengali", "অইউঋঌএওঐঔৡৠঊঈআকচটতপখছফগজডদবঘঝঢধভঙঞণনমযরলবশষসহড়ঢ়য়"),
                new Language("zh", "Chinese", "我在这里等你回来了是啊今天早上好点就学完 读取和好不想让自己变好多事情但"),
                new Language("ka", "Georgian", "ქწერტყუიოპასდფგჰჯკლზხცვბნმ"),
                new Language("he", "Hebrew", "קראטוןםפשדגכעיחלךףזסבהנמצתץ"),
                new Language("hi", "Hindi", "ौैाीूबहगदजडोे्िुपरकतचटंॉमनवलसय"),
                new Language("ja", "Japanese", "わらやまはなたさかあをりみひにちしきいんるゆむふぬつすくうーれめへねてせけぇろよもほのとそこお"),
                new Language("ko", "Korean", "ㅂㅈㄷㄱ쇼ㅕㅑㅐㅔㅁㄴㅇㄹ호ㅓㅐㅋㅌㅊ퓨ㅜㅡ"),
                new Language("ru", "Russian", "Йцукенгшщзхфывапролджэячсмитьбюъ"),
                new Language("th", "Thai", "ๆไำพะัีรนยบลฟหกดเ้าสวงผปแอิืทมใฝฃชขจตคึุถภ"),
        };
        return Arrays.asList(list);
    }
}
