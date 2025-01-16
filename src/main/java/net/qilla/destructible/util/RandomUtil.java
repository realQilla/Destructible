package net.qilla.destructible.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class RandomUtil {

    public static int between(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public static double between(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static float between(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static long between(long min, long max) {
        return (long) (Math.random() * (max - min + 1) + min);
    }

    public static long offset(long value, long offset) {
        return between(value - offset, value + offset);
    }

    public static <T> Optional<T> getRandomItem(Collection<T> collection) {
        if(collection.isEmpty()) return Optional.empty();
        return collection.stream().skip(between(0, collection.size() - 1)).findFirst();
    }
}
