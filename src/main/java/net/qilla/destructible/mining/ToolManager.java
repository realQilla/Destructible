package net.qilla.destructible.mining;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DToolType;
import net.qilla.destructible.mining.item.tool.DTools;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ToolManager {

    private final Player player;
    private final Equipment equipment;

    public ToolManager(@NotNull Player player, @NotNull Equipment equipment) {
        this.player = player;
        this.equipment = equipment;
    }

    public DTool getDTool() {
        String toolString = this.equipment.getHeldItem().getPersistentDataContainer().getOrDefault(DataKey.TOOL, PersistentDataType.STRING, "");
        if(toolString.isEmpty()) return DTools.DEFAULT;
        return Registries.DESTRUCTIBLE_TOOLS.getOrDefault(toolString, DTools.DEFAULT);
    }

    public boolean isToolBroken() {
        ItemStack tool = this.equipment.getHeldItem();
        if(tool.isEmpty()) return false;
        PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return false;
        return pdc.get(DataKey.DURABILITY, PersistentDataType.INTEGER) <= 0;
    }

    public boolean canMine(@NotNull DTool dTool, @NotNull DData dData) {
        if(dData.getDBlock().getStrength() > dTool.getStrength() || isToolBroken()) return false;
        return dData.getDBlock().getProperTools().stream().anyMatch(dToolType -> dToolType.equals(DToolType.ANY) || dTool.getToolType().contains(dToolType));
    }

    public boolean onCoolDown(@NotNull DData dData) {
        return Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(dData.getChunkPos(), k ->
                new DestructibleRegistry<>()).computeIfAbsent(dData.getChunkInt(), k ->
                new DBlockData()).isOnCooldown();
    }

    public void damageTool(@NotNull DTool dTool, int amount) {
        ItemStack tool = equipment.getHeldItem();
        if(tool.isEmpty() || !tool.hasItemMeta()) return;
        PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return;
        int durability = pdc.get(DataKey.DURABILITY, PersistentDataType.INTEGER) - amount;

        if(durability > 0) {
            tool.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, durability);
            });
            Damageable toolDmg = (Damageable) tool.getItemMeta();
            toolDmg.setDamage(dTool.getDurability() - durability);
            tool.setItemMeta(toolDmg);
        } else {
            tool.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, 0);
            });
            Damageable toolDmg = (Damageable) tool.getItemMeta();
            toolDmg.setDamage(dTool.getDurability());
            tool.setItemMeta(toolDmg);
            this.player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Your currently active tool has broken!"));
            this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1);
        }
    }

}
