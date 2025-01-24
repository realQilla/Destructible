package net.qilla.destructible.mining.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.mining.BlockInstance;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.*;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.DUtil;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MiningManager {

    private final DPlayer dPlayer;
    private final BlockMiner blockMiner;
    private volatile BlockInstance blockInstance;

    public MiningManager(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.blockMiner = new BlockMiner(dPlayer);
    }

    public void init(@NotNull BlockPos blockPos, @NotNull Direction direction) {
        if(dPlayer.getCraftPlayer().getGameMode() == GameMode.CREATIVE) return;

        if(blockInstance == null || blockPos.hashCode() != blockInstance.getBlockPos().hashCode()) {
            Optional<DBlock> optionalBlock = DUtil.getDBlock(blockPos);
            if(optionalBlock.isEmpty()) return;

            blockInstance = new BlockInstance(dPlayer.getPlayer().getWorld(),
                    blockPos, CoordUtil.getChunkKey(blockPos),
                    CoordUtil.getBlockIndexInChunk(blockPos),
                    optionalBlock.get(), direction
            );
        }
    }

    public void tickBlock(@NotNull InteractionHand interactionHand) {
        if(blockInstance == null) return;
        if(!interactionHand.equals(InteractionHand.MAIN_HAND)) return;

        ItemStack itemStack = dPlayer.getCraftPlayer().getEquipment().getItemInMainHand();

        ItemData itemData = itemStack.getPersistentDataContainer().getOrDefault(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM, ItemData.EMPTY);
        DItem dItem = DUtil.getDItem(itemData.getItemID());

        blockMiner.tickBlock(itemStack, itemData, dItem, blockInstance);
    }

    public void stop() {
        if(blockInstance == null) return;
        blockMiner.endProgress(blockInstance);
        blockInstance = null;
    }
}