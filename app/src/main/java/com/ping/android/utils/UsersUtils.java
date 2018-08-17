package com.ping.android.utils;

import android.content.Context;
import android.text.TextUtils;

import com.ping.android.data.db.QbUsersDbManager;
import com.ping.android.model.Transphabet;
import com.quickblox.users.model.QBUser;
import com.vanniktech.emoji.EmojiRange;
import com.vanniktech.emoji.EmojiUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        MAPING_VALUE_CHARACTERS = new String[] {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
                "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
                "0","1","2","3","4","5","6","7","8","9","!","@","#","$","%","^","&","*","(",")","-","_","+","=",",",".","/","?","|","{","[","]","}","<",">","~"};
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

    public static Map<String, String> randomizeTransphabet() {
        Map<String, String> mappings = new HashMap<>();
        List<String> values = Arrays.asList(UsersUtils.MAPING_VALUE_CHARACTERS);
        Collections.shuffle(values);
        String[] keys = UsersUtils.MAPING_KEY_CHARACTERS;
        for(int i = 0; i < keys.length; i++) {
            mappings.put(keys[i], values.get(i));
        }
        return mappings;
    }

    public static Map<String, String> randomizeTransphabet(Transphabet transphabet) {
        Map<String, String> mappings = new HashMap<>();
        String[] keys = UsersUtils.MAPING_KEY_CHARACTERS;
        char[] values = transphabet.characters.toCharArray();
        long count = values.length;
        Random random = new Random();
        for (String key : keys) {
            int index = random.nextInt((int) count);
            mappings.put(key, Character.toString(values[index]));
        }
        return mappings;
    }

    public static Map<String, String> randomizeEmojiTransphabet(Transphabet transphabet) {
        Map<String, String> mappings = new HashMap<>();
        String[] keys = UsersUtils.MAPING_KEY_CHARACTERS;
        List<EmojiRange> emojis = EmojiUtils.emojis(transphabet.characters);
        int count = emojis.size();
        Random random = new Random();
        for (String key : keys) {
            int index = random.nextInt(count);
            mappings.put(key, emojis.get(index).emoji.getUnicode());
        }
        return mappings;
    }

    public static Map<String, String> randomizeEmoji() {
        Transphabet transphabet = new Transphabet("sp", "Smileys & People", "\uD83D\uDE00\uD83D\uDE01\uD83D\uDE02\uD83E\uDD23\uD83D\uDE03\uD83D\uDE04\uD83D\uDE05\uD83D\uDE06\uD83D\uDE09\uD83D\uDE0A\uD83D\uDE0B\uD83D\uDE0E\uD83D\uDE0D\uD83D\uDE18\uD83D\uDE17\uD83D\uDE19\uD83D\uDE1A☺\uD83D\uDE42\uD83E\uDD17\uD83E\uDD14\uD83D\uDE10\uD83D\uDE11\uD83D\uDE36\uD83D\uDE44\uD83D\uDE0F\uD83D\uDE23\uD83D\uDE25\uD83D\uDE2E\uD83E\uDD10\uD83D\uDE2F\uD83D\uDE2A\uD83D\uDE2B\uD83D\uDE34\uD83D\uDE0C\uD83D\uDE1B\uD83D\uDE1C\uD83D\uDE1D\uD83E\uDD24\uD83D\uDE12\uD83D\uDE13\uD83D\uDE14\uD83D\uDE15\uD83D\uDE43\uD83E\uDD11\uD83D\uDE32☹\uD83D\uDE41\uD83D\uDE16\uD83D\uDE1E\uD83D\uDE1F\uD83D\uDE24\uD83D\uDE22\uD83D\uDE2D\uD83D\uDE26\uD83D\uDE27\uD83D\uDE28\uD83D\uDE29\uD83D\uDE2C\uD83D\uDE30\uD83D\uDE31\uD83D\uDE33\uD83D\uDE35\uD83D\uDE21\uD83D\uDE20\uD83D\uDE37\uD83E\uDD12\uD83E\uDD15\uD83E\uDD22\uD83E\uDD27\uD83D\uDE07\uD83E\uDD20\uD83E\uDD21\uD83E\uDD25\uD83E\uDD13\uD83D\uDE08\uD83D\uDC7F\uD83D\uDC79\uD83D\uDC7A\uD83D\uDC80☠\uD83D\uDC7B\uD83D\uDC7D\uD83D\uDC7E\uD83E\uDD16\uD83D\uDCA9\uD83D\uDE3A\uD83D\uDE38\uD83D\uDE39\uD83D\uDE3B\uD83D\uDE3C\uD83D\uDE3D\uD83D\uDE40\uD83D\uDE3F\uD83D\uDE3E\uD83D\uDE48\uD83D\uDE49\uD83D\uDE4A\uD83D\uDC76\uD83D\uDC66\uD83D\uDC67\uD83D\uDC68\uD83D\uDC69\uD83D\uDC74\uD83D\uDC75\uD83D\uDC68\u200D⚕️\uD83D\uDC69\u200D⚕️\uD83D\uDC68\u200D\uD83C\uDF93\uD83D\uDC69\u200D\uD83C\uDF93\uD83D\uDC68\u200D\uD83C\uDFEB\uD83D\uDC69\u200D\uD83C\uDFEB\uD83D\uDC68\u200D⚖️\uD83D\uDC69\u200D⚖️\uD83D\uDC68\u200D\uD83C\uDF3E\uD83D\uDC69\u200D\uD83C\uDF3E\uD83D\uDC68\u200D\uD83C\uDF73\uD83D\uDC69\u200D\uD83C\uDF73\uD83D\uDC68\u200D\uD83D\uDD27\uD83D\uDC69\u200D\uD83D\uDD27\uD83D\uDC68\u200D\uD83C\uDFED\uD83D\uDC69\u200D\uD83C\uDFED\uD83D\uDC68\u200D\uD83D\uDCBC\uD83D\uDC69\u200D\uD83D\uDCBC\uD83D\uDC68\u200D\uD83D\uDD2C\uD83D\uDC69\u200D\uD83D\uDD2C\uD83D\uDC68\u200D\uD83D\uDCBB\uD83D\uDC69\u200D\uD83D\uDCBB\uD83D\uDC68\u200D\uD83C\uDFA4\uD83D\uDC69\u200D\uD83C\uDFA4\uD83D\uDC68\u200D\uD83C\uDFA8\uD83D\uDC69\u200D\uD83C\uDFA8\uD83D\uDC68\u200D✈️\uD83D\uDC69\u200D✈️\uD83D\uDC68\u200D\uD83D\uDE80\uD83D\uDC69\u200D\uD83D\uDE80\uD83D\uDC68\u200D\uD83D\uDE92\uD83D\uDC69\u200D\uD83D\uDE92\uD83D\uDC6E\uD83D\uDC6E\u200D♂️\uD83D\uDC6E\u200D♀️\uD83D\uDD75\uD83D\uDD75️\u200D♂️\uD83D\uDD75️\u200D♀️\uD83D\uDC82\uD83D\uDC82\u200D♂️\uD83D\uDC82\u200D♀️\uD83D\uDC77\uD83D\uDC77\u200D♂️\uD83D\uDC77\u200D♀️\uD83E\uDD34\uD83D\uDC78\uD83D\uDC73\uD83D\uDC73\u200D♂️\uD83D\uDC73\u200D♀️\uD83D\uDC72\uD83D\uDC71\uD83D\uDC71\u200D♂️\uD83D\uDC71\u200D♀️\uD83E\uDD35\uD83D\uDC70\uD83E\uDD30\uD83D\uDC7C\uD83C\uDF85\uD83E\uDD36\uD83D\uDE4D\uD83D\uDE4D\u200D♂️\uD83D\uDE4D\u200D♀️\uD83D\uDE4E\uD83D\uDE4E\u200D♂️\uD83D\uDE4E\u200D♀️\uD83D\uDE45\uD83D\uDE45\u200D♂️\uD83D\uDE45\u200D♀️\uD83D\uDE46\uD83D\uDE46\u200D♂️\uD83D\uDE46\u200D♀️\uD83D\uDC81\uD83D\uDC81\u200D♂️\uD83D\uDC81\u200D♀️\uD83D\uDE4B\uD83D\uDE4B\u200D♂️\uD83D\uDE4B\u200D♀️\uD83D\uDE47\uD83D\uDE47\u200D♂️\uD83D\uDE47\u200D♀️\uD83E\uDD26\uD83E\uDD26\u200D♂️\uD83E\uDD26\u200D♀️\uD83E\uDD37\uD83E\uDD37\u200D♂️\uD83E\uDD37\u200D♀️\uD83D\uDC86\uD83D\uDC86\u200D♂️\uD83D\uDC86\u200D♀️\uD83D\uDC87\uD83D\uDC87\u200D♂️\uD83D\uDC87\u200D♀️\uD83D\uDEB6\uD83D\uDEB6\u200D♂️\uD83D\uDEB6\u200D♀️\uD83C\uDFC3\uD83C\uDFC3\u200D♂️\uD83C\uDFC3\u200D♀️\uD83D\uDC83\uD83D\uDD7A\uD83D\uDC6F\uD83D\uDC6F\u200D♂️\uD83D\uDC6F\u200D♀️\uD83D\uDEC0\uD83D\uDECC\uD83D\uDD74\uD83D\uDDE3\uD83D\uDC64\uD83D\uDC65\uD83E\uDD3A\uD83C\uDFC7⛷\uD83C\uDFC2\uD83C\uDFCC\uD83C\uDFCC️\u200D♂️\uD83C\uDFCC️\u200D♀️\uD83C\uDFC4\uD83C\uDFC4\u200D♂️\uD83C\uDFC4\u200D♀️\uD83D\uDEA3\uD83D\uDEA3\u200D♂️\uD83D\uDEA3\u200D♀️\uD83C\uDFCA\uD83C\uDFCA\u200D♂️\uD83C\uDFCA\u200D♀️⛹⛹️\u200D♂️⛹️\u200D♀️\uD83C\uDFCB\uD83C\uDFCB️\u200D♂️\uD83C\uDFCB️\u200D♀️\uD83D\uDEB4\uD83D\uDEB4\u200D♂️\uD83D\uDEB4\u200D♀️\uD83D\uDEB5\uD83D\uDEB5\u200D♂️\uD83D\uDEB5\u200D♀️\uD83C\uDFCE\uD83C\uDFCD\uD83E\uDD38\uD83E\uDD38\u200D♂️\uD83E\uDD38\u200D♀️\uD83E\uDD3C\uD83E\uDD3C\u200D♂️\uD83E\uDD3C\u200D♀️\uD83E\uDD3D\uD83E\uDD3D\u200D♂️\uD83E\uDD3D\u200D♀️\uD83E\uDD3E\uD83E\uDD3E\u200D♂️\uD83E\uDD3E\u200D♀️\uD83E\uDD39\uD83E\uDD39\u200D♂️\uD83E\uDD39\u200D♀️\uD83D\uDC6B\uD83D\uDC6C\uD83D\uDC6D\uD83D\uDC8F\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC8B\u200D\uD83D\uDC68\uD83D\uDC68\u200D❤️\u200D\uD83D\uDC8B\u200D\uD83D\uDC68\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC8B\u200D\uD83D\uDC69\uD83D\uDC91\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC68\uD83D\uDC68\u200D❤️\u200D\uD83D\uDC68\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC69\uD83D\uDC6A\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC66\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC68\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC67\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC69\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC66\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC67\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83E\uDD33\uD83D\uDCAA\uD83D\uDC48\uD83D\uDC49☝\uD83D\uDC46\uD83D\uDD95\uD83D\uDC47✌\uD83E\uDD1E\uD83D\uDD96\uD83E\uDD18\uD83E\uDD19\uD83D\uDD90✋\uD83D\uDC4C\uD83D\uDC4D\uD83D\uDC4E✊\uD83D\uDC4A\uD83E\uDD1B\uD83E\uDD1C\uD83E\uDD1A\uD83D\uDC4B✍\uD83D\uDC4F\uD83D\uDC50\uD83D\uDE4C\uD83D\uDE4F\uD83E\uDD1D\uD83D\uDC85\uD83D\uDC42\uD83D\uDC43\uD83D\uDC63\uD83D\uDC40\uD83D\uDC41\uD83D\uDC41️\u200D\uD83D\uDDE8️\uD83D\uDC45\uD83D\uDC44\uD83D\uDC8B\uD83D\uDC98❤\uD83D\uDC93\uD83D\uDC94\uD83D\uDC95\uD83D\uDC96\uD83D\uDC97\uD83D\uDC99\uD83D\uDC9A\uD83D\uDC9B\uD83D\uDC9C\uD83D\uDDA4\uD83D\uDC9D\uD83D\uDC9E\uD83D\uDC9F❣\uD83D\uDC8C\uD83D\uDCA4\uD83D\uDCA2\uD83D\uDCA3\uD83D\uDCA5\uD83D\uDCA6\uD83D\uDCA8\uD83D\uDCAB\uD83D\uDCAC\uD83D\uDDE8\uD83D\uDDEF\uD83D\uDCAD\uD83D\uDD73\uD83D\uDC53\uD83D\uDD76\uD83D\uDC54\uD83D\uDC55\uD83D\uDC56\uD83D\uDC57\uD83D\uDC58\uD83D\uDC59\uD83D\uDC5A\uD83D\uDC5B\uD83D\uDC5C\uD83D\uDC5D\uD83D\uDECD\uD83C\uDF92\uD83D\uDC5E\uD83D\uDC5F\uD83D\uDC60\uD83D\uDC61\uD83D\uDC62\uD83D\uDC51\uD83D\uDC52\uD83C\uDFA9\uD83C\uDF93⛑\uD83D\uDCFF\uD83D\uDC84\uD83D\uDC8D\uD83D\uDC8E");
        return randomizeEmojiTransphabet(transphabet);
    }
}