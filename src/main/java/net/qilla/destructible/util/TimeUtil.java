package net.qilla.destructible.util;

import com.google.common.base.Preconditions;

public class TimeUtil {

    private TimeUtil() {
    }

    private static final int MS_IN_SECOND = 1000;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int HOURS_IN_DAY = 24;
    private static final int DAYS_IN_WEEK = 7;
    private static final int DAYS_IN_YEAR = 365;

    public static String getTime(long ms, boolean shortForm) {
        if(ms < 0) ms = 0;

        long seconds = ms / MS_IN_SECOND;
        long minutes = seconds / SECONDS_IN_MINUTE;
        long hours = minutes / MINUTES_IN_HOUR;
        long days = hours / HOURS_IN_DAY;
        long weeks = days / DAYS_IN_WEEK;
        long years = days / DAYS_IN_YEAR;
        seconds %= SECONDS_IN_MINUTE;
        minutes %= MINUTES_IN_HOUR;
        hours %= HOURS_IN_DAY;
        days %= DAYS_IN_WEEK;

        StringBuilder result = new StringBuilder();
        if(shortForm) {
            if(years > 0) return years + "y";
            if(weeks > 0) return weeks + "w";
            if(days > 0) return days + "d";
            if(hours > 0) return hours + "h";
            if(minutes > 0) return minutes + "m";
            return seconds + "s";
        }

        appendTime(result, years, "year");
        appendTime(result, weeks, "week");
        appendTime(result, days, "day");
        appendTime(result, hours, "hour");
        appendTime(result, minutes, "minute");
        appendTime(result, seconds, "second");
        return result.toString().trim();
    }

    private static void appendTime(StringBuilder builder, long time, String unit) {
        if(time > 0) {
            builder.append(time).append(" ").append(unit).append(time > 1 ? "s" : "").append(" ");
        }
    }

    public static long stringToMillis(String string) {
        Preconditions.checkNotNull(string, "String cannot be null.");
        if(string.isEmpty()) return 0;

        char timeType = string.charAt(string.length() - 1);
        if(!Character.isLetter(timeType)) return Long.parseLong(string);
        long value = Long.parseLong(string.substring(0, string.length() - 1));

        return switch(timeType) {
            case 's' -> value * 1000;
            case 'm' -> value * 60 * 1000;
            case 'h' -> value * 60 * 60 * 1000;
            case 'd' -> value * 24 * 60 * 60 * 1000;
            case 'w' -> value * 7 * 24 * 60 * 60 * 1000;
            case 'y' -> value * 365 * 24 * 60 * 60 * 1000;
            default -> value;
        };
    }

}