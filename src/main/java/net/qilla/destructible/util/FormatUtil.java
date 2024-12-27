package net.qilla.destructible.util;

import java.text.DecimalFormat;
import java.text.Format;

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
}
