package net.qilla.destructible.util;

import com.mojang.math.Transformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityUtil {

    public static Entity getHighlight(ServerLevel serverLevel) {
        Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, serverLevel);
        entity.setGlowingTag(true);
        entity.setBlockState(Blocks.CONDUIT.defaultBlockState());
        entity.setTransformation(new Transformation(
                new Vector3f(-0.75f, -0.75f, -0.75f),
                new Quaternionf(),
                new Vector3f(2.5f, 2.5f, 2.5f),
                new Quaternionf()
        ));
        return entity;
    }
}