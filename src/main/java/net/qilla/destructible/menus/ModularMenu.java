package net.qilla.destructible.menus;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.SlotType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public abstract class ModularMenu extends DestructibleMenu {

    private final List<Integer> modularSlots;
    private final int populationSize;
    private int shiftIndex = 0;
    public ModularMenu(DPlayer dPlayer, MenuSize size, Component title, List<Integer> modularSlots, int populationSize) {
        super(dPlayer, size, title);
        this.modularSlots = modularSlots;
        this.populationSize = populationSize;
    }

    public void rotateNext(Slot slot, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex += modularSlots.size();
        else this.shiftIndex += amount;

        do {
            this.shiftIndex -= amount;
        } while(shiftIndex + modularSlots.size() > populationSize);
        this.shiftIndex += amount;

        this.refresh();
        super.getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_BREEZE_JUMP, 0.25f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER), true);
    }

    public void rotatePrevious(Slot slot, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex -= populationSize;
        else this.shiftIndex -= amount;

        if(shiftIndex < 0) shiftIndex = 0;

        this.refresh();
        super.getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_BREEZE_LAND, 0.75f, 1.75f, SoundCategory.PLAYERS, PlayType.PLAYER), true);
    }

    public int getShiftIndex() {
        return shiftIndex;
    }

    public abstract void populateModular();
    public abstract void refresh();
}