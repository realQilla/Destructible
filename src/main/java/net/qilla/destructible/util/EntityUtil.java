package net.qilla.destructible.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityUtil {

    public static CraftEntity getHighlight(ServerLevel serverLevel) {
        CraftBlockDisplay entity = new CraftBlockDisplay(serverLevel.getCraftServer(), new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, serverLevel));
        entity.setGlowing(true);
        entity.setGlowColorOverride(Color.SILVER);
        entity.setInvisible(true);
        entity.setBlock(Material.LIGHT_GRAY_CONCRETE.createBlockData());
        entity.setTransformation(new Transformation(
                new Vector3f(0.05f, 0.05f, 0.05f),
                new Quaternionf(),
                new Vector3f(0.90f, 0.90f, 0.90f),
                new Quaternionf()
        ));
        return entity;
    }
}