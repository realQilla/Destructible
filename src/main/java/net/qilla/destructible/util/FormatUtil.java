package net.qilla.destructible.util;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

public class FormatUtil {

    private static final char[] SUFFIXES = {'k', 'm', 'b', 't', 'q'};

    public static String numberChar(double number, boolean capitalize) {
        if(number < 1000) return String.valueOf((int) number);

        int exp = (int) (Math.log(number) / Math.log(1000));
        char suffix = SUFFIXES[exp - 1];
        if(capitalize) suffix = Character.toUpperCase(suffix);

        return decimalTruncation(number / Math.pow(1000, exp), 1) + suffix;
    }

    public static String numberComma(double number) {
        return String.format("%,d", (int) number);
    }

    public static String decimalTruncation(double number, int decimals) {
        Format format = new DecimalFormat("#." + "#".repeat(decimals));
        return format.format(number);
    }

    public static String numberPercentage(double total, double current) {
        return ((int) (((total - current) / total) * 100)) + "%";
    }

    public static String romanNumeral(int number) {
        if(number < 0 || number > 3999) return "Invalid Roman Numeral";
        if(number == 0) return "";
        String[] romanNumerals = {"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M"};
        int[] values = {1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000};
        StringBuilder roman = new StringBuilder();
        int i = 12;
        while(number > 0) {
            int div = number / values[i];
            number %= values[i];
            while(div-- > 0) roman.append(romanNumerals[i]);
            i--;
        }
        return roman.toString();
    }

    public static String getList(List<?> list) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if(i < list.size() - 1) builder.append(", ");
        }
        return builder.toString();
    }

    public static String getTime(long ms, boolean shortForm) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        days %= 7;
        weeks %= 4;
        months %= 12;

        if (shortForm) {
            if (years > 0) return years + "y";
            if (months > 0) return months + "mo";
            if (weeks > 0) return weeks + "w";
            if (days > 0) return days + "d";
            if (hours > 0) return hours + "h";
            if (minutes > 0) return minutes + "m";
            return seconds + "s";
        }

        StringBuilder builder = new StringBuilder();
        if (years > 0) builder.append(years).append(" year").append(years > 1 ? "s" : "").append(" ");
        if (months > 0) builder.append(months).append(" month").append(months > 1 ? "s" : "").append(" ");
        if (weeks > 0) builder.append(weeks).append(" week").append(weeks > 1 ? "s" : "").append(" ");
        if (days > 0) builder.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        if (hours > 0) builder.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        if (minutes > 0) builder.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ");
        if (seconds > 0) builder.append(seconds).append(" second").append(seconds > 1 ? "s" : "");
        return builder.toString().trim();
    }
}