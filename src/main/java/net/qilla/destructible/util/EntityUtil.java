package net.qilla.destructible.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

public class EntityUtil {

    public static Entity getHighlight(ServerLevel serverLevel) {
        Display.BlockDisplay blockDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, serverLevel);
        blockDisplay.setGlowingTag(true);
        blockDisplay.setBlockState(Blocks.BLACK_SHULKER_BOX.defaultBlockState());
        return blockDisplay;
    }
}
