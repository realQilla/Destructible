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
        entity.setBlock(Material.LIGHT_GRAY_CONCRETE.createBlockData());
        entity.setTransformation(new Transformation(
                new Vector3f(0.05f, 0.05f, 0.05f),
                new Quaternionf(),
                new Vector3f(0.90f, 0.90f, 0.90f),
                new Quaternionf()
        ));
        return entity;
    }

    public static CraftEntity getErrorHighlight(ServerLevel serverLevel) {
        CraftBlockDisplay entity = new CraftBlockDisplay(serverLevel.getCraftServer(), new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, serverLevel));
        entity.setInvisible(true);
        entity.setGlowing(true);
        entity.setGlowColorOverride(Color.MAROON);
        entity.setBlock(Material.RED_SHULKER_BOX.createBlockData());
        entity.setTransformation(new Transformation(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf(),
                new Vector3f(1f, 1f, 1f),
                new Quaternionf()
        ));
        return entity;
    }

    public static CraftEntity getValidHighlight(ServerLevel serverLevel) {
        CraftBlockDisplay entity = new CraftBlockDisplay(serverLevel.getCraftServer(), new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, serverLevel));
        entity.setInvisible(true);
        entity.setGlowing(true);
        entity.setGlowColorOverride(Color.WHITE);
        entity.setBlock(Material.WHITE_SHULKER_BOX.createBlockData());
        entity.setTransformation(new Transformation(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf(),
                new Vector3f(1f, 1f, 1f),
                new Quaternionf()
        ));
        return entity;
    }
}