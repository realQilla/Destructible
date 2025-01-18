package net.qilla.destructible.util;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.mining.item.Rarity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class DestructibleUtil {

    public static Optional<DBlock> getDBlock(@NotNull BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.toChunkInt(blockPos);

        var chunkIntMap = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS.get(chunkPos);
        if(chunkIntMap == null || !chunkIntMap.containsKey(chunkInt)) return Optional.empty();
        return Optional.ofNullable(DRegistry.DESTRUCTIBLE_BLOCKS.get(chunkIntMap.get(chunkInt)));
    }

    public static Vec3 getCenterFaceParticle(@NotNull final Direction dir) {
        Vec3 origin = dir.getUnitVec3();
        double offset = 0.65;
        return switch(dir) {
            case UP -> new Vec3(origin.x, offset, origin.z);
            case DOWN -> new Vec3(origin.x, -offset, origin.z);
            case EAST -> new Vec3(offset, origin.y, origin.z);
            case WEST -> new Vec3(-offset, origin.y, origin.z);
            case NORTH -> new Vec3(origin.x, origin.y, -offset);
            case SOUTH -> new Vec3(origin.x, origin.y, offset);
        };
    }

    public static Vec3 getFaceCenterItem(@NotNull final Direction dir) {
        Vec3 origin = dir.getUnitVec3();
        double offset = 0.65;
        return switch(dir) {
            case UP -> new Vec3(origin.x, offset - 0.05, origin.z);
            case DOWN -> new Vec3(origin.x, -offset - 0.2, origin.z);
            case EAST -> new Vec3(offset, origin.y - 0.2, origin.z);
            case WEST -> new Vec3(-offset, origin.y - 0.2, origin.z);
            case NORTH -> new Vec3(origin.x, origin.y - 0.2, -offset);
            case SOUTH -> new Vec3(origin.x, origin.y - 0.2, offset);
        };
    }

    public static float[] getFlatOffsetParticles(@NotNull final Direction dir) {
        return switch(dir) {
            case UP, DOWN -> new float[]{0.25f, 0.05f, 0.25f};
            case EAST, WEST -> new float[]{0.05f, 0.25f, 0.25f};
            case NORTH, SOUTH -> new float[]{0.25f, 0.25f, 0.05f};
        };
    }

    public static ItemLore getLore(DItem item) {
        List<Component> rarity = item.getRarity() == Rarity.NONE ? List.of() : List.of(
                Component.empty(),
                item.getRarity().getComponent());

        return ItemLore.lore()
                .addLines(item.getLore().lines())
                .addLines(rarity)
                .build();
    }

    public static ItemLore getLore(DTool item) {
        ItemLore.Builder builder = ItemLore.lore();
        builder.addLines(item.getLore().lines());
        builder.addLine(Component.empty());
        if(item.getEfficiency() > 0)
            builder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency " + FormatUtil.romanNumeral(item.getEfficiency())));
        builder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Strength " + FormatUtil.romanNumeral(item.getStrength())));
        if(item.getRarity() != Rarity.NONE) {
            builder.addLines(List.of(
                    Component.empty(),
                    item.getRarity().getComponent())
            );
        }
        return builder.build();
    }
}