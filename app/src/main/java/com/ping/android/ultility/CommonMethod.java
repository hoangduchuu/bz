package com.ping.android.ultility;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.model.User;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonMethod {
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PING_ID_PATTERN = "^[a-zA-Z0-9]*$";
    private static final String PHONE_PATTERN = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$";

    public static boolean isValidPingId(String pingId) {
        Pattern p = Pattern.compile(PING_ID_PATTERN);
        Matcher matcher = p.matcher(pingId);
        return matcher.matches();
    }

    public static boolean isValidEmail(String email) {
        Pattern p = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = p.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 10;
    }

    public static boolean isValidPhone(String phone) {
        Pattern p = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = p.matcher(phone);
        return matcher.matches();
    }

    public static boolean isMatchPassword(String password, String confirmPassword) {
        return password != null && confirmPassword != null && password.equals(confirmPassword);
    }

    public static String encryptPassword(String password) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String encryptedPassword = new String(messageDigest.digest());
        return encryptedPassword;
    }

    public static boolean isFilteredContact(User contact, String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        if (!StringUtils.isEmpty(contact.email) && text.equals(contact.email)) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.phone) && text.equals(contact.phone)) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.pingID) && contact.pingID.startsWith(text)) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.firstName) && contact.firstName.toUpperCase().startsWith(text.toUpperCase())) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.lastName) && contact.lastName.toUpperCase().startsWith(text.toUpperCase())) {
            return true;
        }
        String fullName = String.format("%s %s", contact.firstName, contact.lastName).trim();
        if (!StringUtils.isEmpty(fullName) && fullName.toUpperCase().startsWith(text.toUpperCase())) {
            return true;
        }
        return false;
    }

    public static boolean isFiltered(User contact, String text) {
        if (StringUtils.isEmpty(text)) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.email) && text.equals(contact.email)) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.phone) && text.equals(contact.phone)) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.pingID) && contact.pingID.startsWith(text)) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.firstName) && contact.firstName.toUpperCase().startsWith(text.toUpperCase())) {
            return true;
        }
        if (!StringUtils.isEmpty(contact.lastName) && contact.lastName.toUpperCase().startsWith(text.toUpperCase())) {
            return true;
        }
        String fullName = String.format("%s %s", contact.firstName, contact.lastName).trim();
        if (!StringUtils.isEmpty(fullName) && fullName.toUpperCase().startsWith(text.toUpperCase())) {
            return true;
        }
        return false;
    }

    public static String getStringOf(Object object) {
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    public static Long getLongOf(Object object) {
        if (object != null) {
            return Long.valueOf(object.toString());
        }
        return null;
    }

    public static long longValue(Object object, long defaultValue) {
        if (object != null) {
            return 1;
        }
        return defaultValue;
    }

    public static int intValue(DataSnapshot snapshot) {
        return intValue(snapshot, -1);
    }

    public static int intValue(DataSnapshot snapshot, int defaultValue) {
        Integer value = snapshot.getValue(Integer.class);
        return value != null ? value : defaultValue;
    }

    public static Double getDoubleOf(Object object) {
        if (object != null) {
            return Double.valueOf(object.toString());
        }
        return null;
    }

    public static Boolean getBooleanOf(Object object) {
        if (object != null) {
            return Boolean.valueOf(object.toString());
        }
        return null;
    }

    public static String convertTimestampToTime(double seconds) {
        long milliSeconds = (long) (seconds * 1000);

        String time = DateFormat.format("h:mm a", milliSeconds).toString();
        String formatDate = "";
        if (DateUtils.isToday(milliSeconds)) {
            formatDate = DateFormat.format("h:mm a", milliSeconds).toString();
        } else if(isXDateFromToday(milliSeconds, 1)) {
            formatDate = "Yesterday" + " " + time;
        } else if(isXDateFromToday(milliSeconds, 7)) {
            formatDate = DateFormat.format("EEE", milliSeconds).toString() + " " + time;
        } else {
            formatDate = DateFormat.format("MM/dd/yyyy", milliSeconds).toString() + " " + time;
        }
        return formatDate;
    }

    public static String convertTimestampToDate(double seconds) {
        long milliSeconds = (long) (seconds * 1000);

        String formatDate = "";
        if (DateUtils.isToday(milliSeconds)) {
            formatDate = DateFormat.format("h:mm a", milliSeconds).toString();
        } else if(isXDateFromToday(milliSeconds, 1)) {
            formatDate = "Yesterday";
        } else if(isXDateFromToday(milliSeconds, 7)) {
            formatDate = DateFormat.format("EEE", milliSeconds).toString();
        } else {
            formatDate = DateFormat.format("MM/dd/yyyy", milliSeconds).toString();
        }
        return formatDate;
    }

    public static boolean isXDateFromToday(long milliSeconds, int x) {
        for(int i = 0; i<= x; i++) {
            long diff = i * 24 * 3600 * 1000;
            if (DateUtils.isToday(milliSeconds + diff)) {
                return true;
            }
        }
        return false;
    }

    public static boolean compareTimestamp(double first, double second) {
        return first < second;
    }

    public static boolean createFolder(String path) {

        if (!new File(path).exists()) {
            return new File(path).mkdirs();
        } else {
            return true;
        }

    }

    public static String getFileNameFromFirebase(String url) {
//        String fileName = "";
//        String namePattern = ".*ping-android-dev.appspot.com/o/(.*)\\?alt.*";
//        Pattern pattern = Pattern.compile(namePattern);
//        Matcher matcher = pattern.matcher(url);
//        if (matcher.matches()) {
//            fileName = matcher.group(1);
//            try {
//                fileName = URLDecoder.decode(fileName, "UTF-8");
//            } catch (Exception e) {
//
//            }
//        }
        //Change from https:// to gs://
        String fileName = url.replace(Constant.URL_STORAGE_REFERENCE + "/", "");
        return fileName;
    }

    public static Bitmap puzzleImage(Bitmap bitmap, int items) {
        if (bitmap == null) {
            return null;
        }
        ArrayList<Bitmap> chunkedImages;
        int chunkNumbers = items * items;
        //For the number of rows and columns of the grid to be displayed
        int rows, cols;

        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;

        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }

        Bitmap puzzledBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Collections.shuffle(chunkedImages);
        Canvas canvas = new Canvas(puzzledBitmap);
        int count = 0;
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                canvas.drawBitmap(chunkedImages.get(count), chunkWidth * y, chunkHeight * x, null);
                count++;
            }
        }
        return puzzledBitmap;
    }

    public static ArrayList<Bitmap> splitImage(Bitmap bitmap, int items) {
        ArrayList<Bitmap> chunkedImages;
        int chunkNumbers = items * items;
        //For the number of rows and columns of the grid to be displayed
        int rows, cols;

        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;

        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }
        return chunkedImages;
    }

    public static void UpdateSearchViewLayout(SearchView searchView) {
        AutoCompleteTextView searchTextContent = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));

        searchTextContent.setTextSize(15); //Set the text size
        searchTextContent.setGravity(Gravity.BOTTOM); //Set its gravity to bottom
    }

}
