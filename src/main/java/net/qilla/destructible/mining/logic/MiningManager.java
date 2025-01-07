package net.qilla.destructible.mining.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.mining.BlockInstance;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.DBlockUtil;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MiningManager {

    private final DPlayer dPlayer;
    private final BlockMiner blockMiner;
    private final ToolManager toolManager;
    private volatile BlockInstance blockInstance;

    public MiningManager(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.blockMiner = new BlockMiner(dPlayer);
        this.toolManager = new ToolManager(dPlayer);
    }

    public void init(@NotNull BlockPos blockPos, @NotNull Direction direction) {
        if(dPlayer.getCraftPlayer().getGameMode() == GameMode.CREATIVE) return;

        if(blockInstance == null || blockPos.hashCode() != blockInstance.getBlockPos().hashCode()) {
            Optional<DBlock> optional = DBlockUtil.getDBlock(blockPos);

            if(optional.isEmpty()) return;

            blockInstance = new BlockInstance(dPlayer.getCraftPlayer().getWorld(), blockPos, new ChunkPos(blockPos), CoordUtil.posToChunkLocalPos(blockPos), optional.get(), direction);
        }
    }

    public void tickBlock(@NotNull InteractionHand interactionHand) {
        if(blockInstance == null || !interactionHand.equals(InteractionHand.MAIN_HAND)) return;

        ItemStack itemStack = dPlayer.getCraftPlayer().getEquipment().getItemInMainHand();
        Optional<DItem> optional = DItemStack.getDItem(itemStack);

        if(optional.isEmpty()) return;
        if(!(optional.get() instanceof DTool dTool)) return;

        if(!toolManager.canMine(dTool, blockInstance)) return;
        if(toolManager.isToolBroken(itemStack)) return;
        blockMiner.tickBlock(blockInstance, dTool, toolManager);
    }

    public void stop() {
        if(blockInstance == null) return;
        blockMiner.endProgress(blockInstance);
        blockInstance = null;
    }
}