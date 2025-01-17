package net.qilla.destructible.mining.logic;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.BlockInstance;
import net.qilla.destructible.mining.block.DBlock;
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

    public boolean isToolBroken(ItemStack itemStack) {
        if(!itemStack.getPersistentDataContainer().has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return false;
        return itemStack.getPersistentDataContainer().get(DataKey.DURABILITY, PersistentDataType.INTEGER) == -1;
    }

    public boolean canMine(@NotNull DTool dTool, @NotNull BlockInstance blockInstance) {
        DBlock dBlock = blockInstance.getDBlock();
        if(blockInstance.getDBlockData().isLocked() ||
                blockInstance.getDBlockData().isOnCooldown() ||
                dBlock.getStrength() > dTool.getStrength() ||
                dBlock.getDurability() < 0) return false;
        if(dBlock.getStrength() <= 0) return true;
        return dBlock.getCorrectTools().stream().anyMatch(dToolType -> dTool.getToolType().contains(dToolType));
    }

    public void damageTool(@NotNull DTool dTool, int amount) {
        ItemStack itemStack = dPlayer.getCraftPlayer().getEquipment().getItemInMainHand();
        if(!itemStack.getPersistentDataContainer().has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return;
        int durability = itemStack.getPersistentDataContainer().get(DataKey.DURABILITY, PersistentDataType.INTEGER) - amount;

        Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> {
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
                dPlayer.sendMessage("<red>Your currently active tool has broken!");
                dPlayer.getCraftPlayer().getWorld().playSound(dPlayer.getCraftPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 0.2f, 1);
            }
        });
    }
}