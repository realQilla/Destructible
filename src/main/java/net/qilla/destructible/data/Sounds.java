package net.qilla.destructible.data;

import net.qilla.destructible.player.PlayType;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class Sounds {

    public static final SoundSettings RETURN_MENU = SoundSettings.of(Sound.BLOCK_NOTE_BLOCK_BELL, 0.25f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings MENU_ITEM_APPEAR = SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings MENU_CLAIM_ITEM = SoundSettings.of(Sound.ENTITY_HORSE_SADDLE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings SIGN_INPUT = SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings GENERAL_ERROR = SoundSettings.of(Sound.ENTITY_VILLAGER_NO, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings GENERAL_SUCCESS = SoundSettings.of(Sound.ENTITY_VILLAGER_YES, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings GENERAL_SUCCESS_2 = SoundSettings.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings RESET = SoundSettings.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings MENU_CLICK_ITEM = SoundSettings.of(Sound.BLOCK_DECORATED_POT_INSERT, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings MENU_GET_ITEM = SoundSettings.of(Sound.ITEM_BUNDLE_REMOVE_ONE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings MENU_INPUT_ITEM = SoundSettings.of(Sound.ITEM_BUNDLE_INSERT, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings ITEM_DELETE = SoundSettings.of(Sound.BLOCK_LAVA_POP, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings MENU_ROTATE_NEXT = SoundSettings.of(Sound.ENTITY_BREEZE_JUMP, 0.25f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings MENU_ROTATE_PREVIOUS = SoundSettings.of(Sound.ENTITY_BREEZE_LAND, 0.75f, 1.75f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings LARGE_OPERATION_COMPLETE = SoundSettings.of(Sound.ENTITY_PLAYER_BURP, 0.5f, 0f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings TINY_OPERATION_COMPLETE = SoundSettings.of(Sound.ENTITY_PLAYER_BURP, 0.5f, 2f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings LARGE_OPERATION_UPDATE = SoundSettings.of(Sound.BLOCK_NOTE_BLOCK_PLING, 0.1f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings ENABLE_SETTING = SoundSettings.of(Sound.BLOCK_BEACON_ACTIVATE, 0.25f, 2f, SoundCategory.PLAYERS, PlayType.PLAYER);
    public static final SoundSettings DISABLE_SETTING = SoundSettings.of(Sound.BLOCK_BEACON_DEACTIVATE, 0.25f, 2f, SoundCategory.PLAYERS, PlayType.PLAYER);
}