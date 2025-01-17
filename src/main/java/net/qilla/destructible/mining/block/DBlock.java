package net.qilla.destructible.mining.block;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    public DBlock(Builder builder) {
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

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public Material getMaterial() {
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

    @NotNull
    public Set<ToolType> getCorrectTools() {
        return Collections.unmodifiableSet(this.correctTools);
    }

    public boolean hasProperTool() {
        return !this.correctTools.isEmpty();
    }

    @NotNull
    public List<ItemDrop> getLootpool() {
        return Collections.unmodifiableList(this.lootpool);
    }

    @NotNull
    public Sound getBreakSound() {
        return this.breakSound;
    }

    @NotNull
    public Material getBreakParticle() {
        return this.breakParticle;
    }

    @NotNull
    public static Builder getBuilder(DBlock dBlock) {
        return new Builder()
                .id(dBlock.getId())
                .material(dBlock.getMaterial())
                .strength(dBlock.getStrength())
                .durability(dBlock.getDurability())
                .cooldown(dBlock.getCooldown())
                .correctTools(dBlock.getCorrectTools())
                .lootpool(dBlock.getLootpool())
                .breakSound(dBlock.getBreakSound())
                .breakParticle(dBlock.getBreakParticle());
    }

    public static class Builder {
        private String id;
        private Material material;
        private int strength;
        private long durability;
        private long cooldown;
        private Set<ToolType> correctTools;
        private List<ItemDrop> lootpool;
        private Sound breakSound;
        private Material breakParticle;

        public Builder() {
            this.material = Material.AIR;
            this.strength = 0;
            this.durability = -1;
            this.cooldown = 1000;
            this.correctTools = Set.of();
            this.lootpool = List.of();
            this.breakSound = Sound.BLOCK_STONE_BREAK;
            this.breakParticle = Material.BEDROCK;
        }

        public Builder id(@NotNull String id) {
            Preconditions.checkNotNull(id, "ID cannot be null");
            this.id = id;
            return this;
        }

        public Builder material(@NotNull Material material) {
            Preconditions.checkNotNull(material, "Material cannot be null");
            this.material = material;
            return this;
        }

        /**
         * Tool strength requirement to destroy block
         *
         * @param strength
         *
         * @return
         */
        public Builder strength(int strength) {
            this.strength = strength;
            return this;
        }

        /**
         * Ticks taken to destroy block, base damage efficiency is 1
         *
         * @param durability
         *
         * @return
         */
        public Builder durability(long durability) {
            this.durability = Math.max(0, durability);
            return this;
        }


        /**
         * Milliseconds cooldown the block will have after being destroyed
         *
         * @param ms
         *
         * @return
         */
        public Builder cooldown(long ms) {
            this.cooldown = Math.max(1000, ms);
            return this;
        }

        /**
         * Set of tools that can destroy this block
         *
         * @param toolTypes
         *
         * @return
         */
        public Builder correctTools(@NotNull Set<ToolType> toolTypes) {
            Preconditions.checkNotNull(toolTypes, "ToolTypes cannot be null");

            this.correctTools = toolTypes;
            return this;
        }

        /**
         * Array of ItemDrop objects that will drop when block is destroyed
         *
         * @param lootpool
         *
         * @return
         */
        public Builder lootpool(@NotNull List<ItemDrop> lootpool) {
            Preconditions.checkNotNull(lootpool, "Lootpool cannot be null");
            this.lootpool = lootpool;
            return this;
        }

        /**
         * Sound played when block is destroyed
         *
         * @param sound
         *
         * @return
         */
        public Builder breakSound(@NotNull Sound sound) {
            Preconditions.checkNotNull(sound, "Sound cannot be null");
            this.breakSound = sound;
            return this;
        }

        /**
         * Block particle played when block is destroyed
         *
         * @param particle
         *
         * @return
         */
        public Builder breakParticle(@NotNull Material particle) {
            Preconditions.checkNotNull(particle, "Particle cannot be null");
            this.breakParticle = particle;
            return this;
        }

        public DBlock build() {
            return new DBlock(this);
        }
    }
}