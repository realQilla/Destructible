package net.qilla.destructible.player;

import java.util.EnumMap;

public class Cooldown {
    private final EnumMap<CooldownType, Long> cooldowns;

    public Cooldown() {
        this.cooldowns = new EnumMap<>(CooldownType.class);
    }

    public long get(CooldownType cooldownType) {
        return cooldowns.computeIfAbsent(cooldownType, c -> 0L);
    }

    public boolean has(CooldownType cooldownType) {
        return cooldowns.computeIfAbsent(cooldownType, c -> 0L) > System.currentTimeMillis();
    }

    public void set(CooldownType cooldownType, long ms) {
        cooldowns.put(cooldownType, System.currentTimeMillis() + ms);
    }

    public void set(CooldownType cooldownType) {
        cooldowns.put(cooldownType, System.currentTimeMillis() + cooldownType.getMs());
    }
}