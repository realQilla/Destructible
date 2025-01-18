package net.qilla.destructible.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    private StringUtil() {
    }

    public static @NotNull String pluralizer(@NotNull String string, int amount) {
        return amount == 1 ? string : string + "'s";
    }

    public static @NotNull String toName(@NotNull String string) {
        return Arrays.stream(string.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static @NotNull String toNameList(@NotNull List<?> list) {
        return list.stream()
                .map(Object::toString)
                .map(StringUtil::toName)
                .collect(Collectors.joining(", "));
    }
}