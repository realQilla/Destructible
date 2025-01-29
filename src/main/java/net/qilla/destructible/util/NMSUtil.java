package net.qilla.destructible.util;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.qilla.destructible.Destructible;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftBlockDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class NMSUtil {

    private static final Plugin PLUGIN = Destructible.getInstance();

    public static @NotNull CraftBlockDisplay createHighlightEntity(@NotNull World world) {
        CraftBlockDisplay display = new CraftBlockDisplay((CraftServer) PLUGIN.getServer(),
                EntityType.BLOCK_DISPLAY.create(((CraftWorld) world).getHandle(), EntitySpawnReason.COMMAND));
        display.setGlowing(true);
        display.setGlowColorOverride(Color.SILVER);
        display.setBlock(Material.LIGHT_GRAY_CONCRETE.createBlockData());
        display.setTransformation(new Transformation(
                new Vector3f(0.05f, 0.05f, 0.05f),
                new Quaternionf(),
                new Vector3f(0.90f, 0.90f, 0.90f),
                new Quaternionf()
        ));
        return display;
    }
}