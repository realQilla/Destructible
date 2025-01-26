package net.qilla.destructible.data;

import net.qilla.qlibrary.util.sound.PlayType;
import net.qilla.qlibrary.util.sound.QSound;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.jetbrains.annotations.Nullable;

public class DSounds {

    /**
     * General Sounds
     */

    public static final QSound GENERAL_ERROR = QSound.of(Sound.ENTITY_VILLAGER_NO, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final QSound GENERAL_SUCCESS = QSound.of(Sound.ENTITY_VILLAGER_YES, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final QSound GENERAL_SUCCESS_2 = QSound.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final QSound LARGE_OPERATION_COMPLETE = QSound.of(Sound.ENTITY_PLAYER_BURP, 0.5f, 0f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final QSound TINY_OPERATION_COMPLETE = QSound.of(Sound.ENTITY_PLAYER_BURP, 0.5f, 2f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final QSound LARGE_OPERATION_UPDATE = QSound.of(Sound.BLOCK_NOTE_BLOCK_PLING, 0.1f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final QSound ENABLE_SETTING = QSound.of(Sound.BLOCK_BEACON_ACTIVATE, 0.25f, 2f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final QSound DISABLE_SETTING = QSound.of(Sound.BLOCK_BEACON_DEACTIVATE, 0.25f, 2f, SoundCategory.PLAYERS, PlayType.PLAYER);

    /**
     * In-game Interaction Sounds
     */

    public static final QSound ITEM_BREAK = QSound.of(Sound.ENTITY_ITEM_BREAK, 0.5f, 1f, SoundCategory.PLAYERS, PlayType.BROADCAST_CUR_LOC);
    public static final QSound BLOCK_RETURN = QSound.of(Sound.ENTITY_CHICKEN_EGG, 0.33f, 1f, SoundCategory.BLOCKS, PlayType.BROADCAST_CUR_LOC);
}