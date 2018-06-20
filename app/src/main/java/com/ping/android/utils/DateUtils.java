package com.ping.android.utils;

import com.ping.android.utils.configs.Constant;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a", Locale.getDefault());

    public static String toString(String format, Date date) {
        dateFormat.applyPattern(format);
        return dateFormat.format(date);
    }

    public static String toString(String format, double timestamp) {
        long finalTimestamp = (long)timestamp * 1000;
        return toString(format, new Date(finalTimestamp));
    }

    public static String toHeaderString(Date date) {
        if (isSameDay(date.getTime(), new Date().getTime())) {
            return "Today, " + toString("h:mm a", date);
        } else if (isYesterday(date)) {
            return "Yesterday, " + toString("h:mm a", date);
        } else {
            if (withinOneWeek(date)) {
                return toString("EEEE, h:mm a", date);
            } else {
                return toString("MMMM d, yyyy", date);
            }
        }
    }

    public static boolean withinOneWeek(Date date) {
        Date oneWeekBefore = xDaysBefore(7);
        return date.getTime() > oneWeekBefore.getTime();
    }

    public static Date xDaysBefore(int x) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -x);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static boolean isYesterday(Date date) {
        long currentDays = (long)(new Date().getTime() / Constant.MILLISECOND_PER_DAY);
        long days = (long)(date.getTime() / Constant.MILLISECOND_PER_DAY);
        return currentDays - days == 1;
    }

    public static boolean isSameDay(double first, double second) {
        return (long) (first / Constant.MILLISECOND_PER_DAY) == (long) (second / Constant.MILLISECOND_PER_DAY);
    }
}