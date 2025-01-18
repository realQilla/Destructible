package net.qilla.destructible.util;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.Format;

public class NumberUtil {

    private NumberUtil() {
    }

    public static int minMax(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public static @NotNull String numberComma(double number) {
        return String.format("%,d", (int) number);
    }

    public static @NotNull String numberComma(long number) {
        return String.format("%,d", number);
    }

    public static @NotNull String numberPercentage(double total, double current) {
        return ((int) (((total - current) / total) * 100)) + "%";
    }

    private static final char[] SUFFIXES = {'k', 'm', 'b', 't', 'q'};

    public static @NotNull String numberChar(double number, boolean capitalize) {
        if (number < 1) return String.valueOf((int) number); // Handle numbers < 1 upfront
        if (number < 1000) return String.valueOf((int) number);

        int exp = (int) (Math.log10(number) / 3); // Faster: use log base 10 divided by 3
        if (exp > SUFFIXES.length) return "NaN"; // Prevent index out of bounds

        char suffix = SUFFIXES[exp - 1];
        if (capitalize) suffix = Character.toUpperCase(suffix);

        double scaledNumber = number / Math.pow(1000, exp);
        return String.format("%.1f%c", scaledNumber, suffix).replace(".0", ""); // Remove unnecessary decimals
    }

    public static @NotNull String decimalTruncation(double number, int decimals) {
        Format format = new DecimalFormat("#." + "#".repeat(decimals));
        return format.format(number);
    }

    public static String romanNumeral(int number) {
        if(number <= 0 || number > 9999) return String.valueOf(number);

        String[] romanNumerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

        StringBuilder roman = new StringBuilder();
        for(int i = 0; i < values.length; i++) {
            while(number >= values[i]) {
                roman.append(romanNumerals[i]);
                number -= values[i];
            }
        }
        return roman.toString();
    }
}
