package net.qilla.destructible.mining.block;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class DBlock {
    private final String id;
    private final Material material;
    private final int strength;
    private final long durability;
    private final long cooldown;
    private final Set<ToolType> correctTools;
    private final List<ItemDrop> lootpool;
    private final Sound breakSound;
    private final Material breakParticle;

    private DBlock(Builder builder) {
        Preconditions.checkNotNull(builder, "Builder cannot be null");
        Preconditions.checkNotNull(builder.id, "ID cannot be null");
        Preconditions.checkNotNull(builder.material, "Material cannot be null");
        Preconditions.checkNotNull(builder.correctTools, "Set cannot be null");
        Preconditions.checkNotNull(builder.lootpool, "List cannot be null");
        Preconditions.checkNotNull(builder.breakSound, "Sound cannot be null");
        Preconditions.checkNotNull(builder.breakParticle, "Particle cannot be null");

        this.id = builder.id;
        this.material = builder.material;
        this.strength = builder.strength;
        this.durability = builder.durability;
        this.cooldown = builder.cooldown;
        this.correctTools = builder.correctTools;
        this.lootpool = builder.lootpool;
        this.breakSound = builder.breakSound;
        this.breakParticle = builder.breakParticle;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DBlock of(Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return newBuilder.build();
    }

    public @NotNull String getID() {
        return this.id;
    }

    public @NotNull Material getMaterial() {
        return this.material;
    }

    public int getStrength() {
        return this.strength;
    }

    public long getDurability() {
        return this.durability;
    }

    public long getCooldown() {
        return this.cooldown;
    }

    public @NotNull Set<ToolType> getCorrectTools() {
        return Collections.unmodifiableSet(this.correctTools);
    }

    public boolean hasProperTool() {
        return !this.correctTools.isEmpty();
    }

    public @NotNull List<ItemDrop> getLootpool() {
        return Collections.unmodifiableList(this.lootpool);
    }

    public @NotNull Sound getBreakSound() {
        return this.breakSound;
    }

    public @NotNull Material getBreakParticle() {
        return this.breakParticle;
    }

    public static @NotNull Builder getBuilder(DBlock dBlock) {
        return new Builder()
                .id(dBlock.getID())
                .material(dBlock.getMaterial())
                .strength(dBlock.getStrength())
                .durability(dBlock.getDurability())
                .cooldown(dBlock.getCooldown())
                .correctTools(dBlock.getCorrectTools())
                .lootpool(dBlock.getLootpool())
                .breakSound(dBlock.getBreakSound())
                .breakParticle(dBlock.getBreakParticle());
    }

    public static final class Builder {
        private String id = StringUtil.uniqueIdentifier(8);
        private Material material = Material.AIR;
        private int strength = 0;
        private long durability = -1;
        private long cooldown = 5000;
        private Set<ToolType> correctTools = Set.of();
        private List<ItemDrop> lootpool = List.of();
        private Sound breakSound = Sound.BLOCK_STONE_BREAK;
        private Material breakParticle = Material.STONE;

        private Builder() {
        }

        public Builder id(@NotNull String id) {
            this.id = id;
            return this;
        }

        public Builder material(@NotNull Material material) {
            this.material = material;
            return this;
        }

        public Builder strength(int strength) {
            this.strength = strength;
            return this;
        }

        public Builder durability(long durability) {
            this.durability = Math.max(0, durability);
            return this;
        }

        public Builder cooldown(long ms) {
            this.cooldown = Math.max(1000, ms);
            return this;
        }

        public Builder correctTools(@NotNull Set<ToolType> toolTypes) {
            this.correctTools = toolTypes;
            return this;
        }

        public Builder lootpool(@NotNull List<ItemDrop> lootpool) {
            this.lootpool = lootpool;
            return this;
        }

        public Builder breakSound(@NotNull Sound sound) {
            this.breakSound = sound;
            return this;
        }

        public Builder breakParticle(@NotNull Material particle) {
            this.breakParticle = particle;
            return this;
        }

        public DBlock build() {
            return new DBlock(this);
        }
    }
}