package net.qilla.destructible.menus;

import net.qilla.destructible.player.PlayType;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class SoundSettings {

    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final SoundCategory category;
    private final PlayType playType;

    public SoundSettings(Sound sound, float volume, float pitch, PlayType playType) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.category = null;
        this.playType = playType;
    }

    public SoundSettings(Sound sound, float volume, float pitch, SoundCategory category, PlayType playType) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.category = category;
        this.playType = playType;
    }

    public static SoundSettings of(Sound sound, float volume, float pitch, PlayType playType) {
        return new SoundSettings(sound, volume, pitch, playType);
    }

    public static SoundSettings of(Sound sound, float volume, float pitch, SoundCategory soundCategory, PlayType playType) {
        return new SoundSettings(sound, volume, pitch, soundCategory, playType);
    }


    public Sound getSound() {
        return this.sound;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public SoundCategory getCategory() {
        return this.category;
    }

    public PlayType getPlayType() {
        return this.playType;
    }
}
