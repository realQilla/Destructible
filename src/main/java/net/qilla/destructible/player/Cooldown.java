package net.qilla.destructible.player;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumMap;

public class Cooldown {
    private final EnumMap<CooldownType, Long> cooldowns = new EnumMap<>(CooldownType.class);

    public Cooldown() {
    }

    public long get(@Nullable CooldownType cooldownType) {
        if(cooldownType == null) return 0L;
        return cooldowns.computeIfAbsent(cooldownType, c -> 0L);
    }

    public boolean has(@Nullable CooldownType cooldownType) {
        if(cooldownType == null) return false;
        return cooldowns.computeIfAbsent(cooldownType, c -> 0L) > System.currentTimeMillis();
    }

    public void set(@Nullable CooldownType cooldownType, long ms) {
        if(cooldownType == null) return;
        cooldowns.put(cooldownType, System.currentTimeMillis() + ms);
    }

    public void set(@Nullable CooldownType cooldownType) {
        if(cooldownType == null) return;
        cooldowns.put(cooldownType, System.currentTimeMillis() + cooldownType.getMs());
    }
}