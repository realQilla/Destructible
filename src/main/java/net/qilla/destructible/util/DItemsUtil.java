package net.qilla.destructible.util;

import com.google.common.collect.ArrayListMultimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DTool;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class DItemsUtil {

    public static ItemStack getItem(DItem dItem) {
        ItemStack itemStack = new ItemStack(dItem.getMaterial());
        itemStack.editMeta(meta -> {
            meta.displayName(dItem.getDisplayName());
            meta.lore(getItemLore(dItem));
            meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING, dItem.getId());
            meta.setAttributeModifiers(ArrayListMultimap.create());
        });
        return itemStack;
    }

    public static ItemStack getTool(DTool dTool) {
        ItemStack itemStack = ItemStack.of(dTool.getMaterial());
        itemStack.editMeta(meta -> {
            meta.displayName(dTool.getDisplayName());
            meta.lore(getToolLore(dTool));
            meta.setEnchantmentGlintOverride(true);
            meta.setAttributeModifiers(ArrayListMultimap.create());
            meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING, dTool.getId());
            meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, dTool.getDurability());
        });

        if(itemStack instanceof Damageable damageable) {
            if(dTool.getDurability() == -1) damageable.resetDamage();
            else damageable.setMaxDamage(dTool.getDurability());
            itemStack.setItemMeta(damageable);
        }

        return itemStack;
    }

    private static List<Component> getItemLore(DItem dItem) {
        List<Component> lore = new ArrayList<>(dItem.getLore());
        lore.add(Component.empty());
        lore.add(dItem.getRarity().getComponent());
        return lore;
    }

    private static List<Component> getToolLore(DTool dTool) {
        List<Component> lore = new ArrayList<>(dTool.getLore());
        lore.addAll(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency " + dTool.getEfficiency()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Strength " + dTool.getStrength()),
                        Component.empty(),
                        dTool.getRarity().getComponent()
                )
        );
        return lore;
    }
}