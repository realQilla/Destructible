package net.qilla.destructible.util;

public class RandomUtil {

    public static int between(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public static double between(double min, double max) {
        return Math.random() * (max - min) + min;
    }
}
