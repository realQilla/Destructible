package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public interface Item {
    String getId();

    Component getDisplayName();

    Material getMaterial();

    Rarity getRarity();

    int getStackSize();
}
