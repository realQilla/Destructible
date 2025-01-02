package net.qilla.destructible.mining.logic;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.BlockInstance;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ToolManager {

    private final DPlayer dPlayer;

    public ToolManager(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
    }

    public boolean isToolBroken() {
        ItemStack itemStack = this.dPlayer.getEquipment().getItemInMainHand();
        if(itemStack.isEmpty()) return false;
        return itemStack.getPersistentDataContainer().get(DataKey.DURABILITY, PersistentDataType.INTEGER) == -1;
    }

    public boolean canMine(@NotNull DTool dTool, @NotNull BlockInstance blockInstance) {
        if(blockInstance.getDBlockData().isLocked() ||
                blockInstance.getDBlockData().isOnCooldown() ||
                blockInstance.getDBlock().getStrength() > dTool.getStrength()) return false;
        return blockInstance.getDBlock().getProperTools().stream().anyMatch(dToolType -> dToolType.equals(ToolType.HAND) || dTool.getToolType().contains(dToolType));
    }

    public void damageTool(@NotNull DTool dTool, int amount) {
        ItemStack itemStack = this.dPlayer.getEquipment().getItemInMainHand();
        if(!itemStack.getPersistentDataContainer().has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return;
        int durability = itemStack.getPersistentDataContainer().get(DataKey.DURABILITY, PersistentDataType.INTEGER) - amount;

        Bukkit.getScheduler().runTask(this.dPlayer.getPlugin(), () -> {
            if(durability > 0) {
                itemStack.editMeta(meta -> {
                    meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, durability);
                });
                itemStack.setData(DataComponentTypes.DAMAGE, dTool.getDurability() - durability);
            } else if(durability == 0) {
                itemStack.editMeta(meta -> {
                    meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, -1);
                });

                itemStack.setData(DataComponentTypes.DAMAGE, dTool.getDurability());
                this.dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<red>Your currently active tool has broken!"));
                this.dPlayer.getWorld().playSound(this.dPlayer.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.2f, 1);
            }
        });
    }
}