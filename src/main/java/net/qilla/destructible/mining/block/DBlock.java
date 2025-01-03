package net.qilla.destructible.mining.block;

import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DBlock {
    private final String id;
    private final Material blockMaterial;
    private final int blockStrength;
    private final int blockDurability;
    private final long blockCooldown;
    private final List<ToolType> correctTools;
    private final List<DDrop> itemDrops;
    private final Sound breakSound;
    private final Material breakParticle;

    public DBlock(Builder builder) {
        this.id = builder.id;
        this.blockMaterial = builder.blockMaterial;
        this.blockStrength = builder.blockStrength;
        this.blockDurability = builder.blockDurability;
        this.blockCooldown = builder.blockCooldown;
        this.correctTools = builder.correctTools;
        this.itemDrops = builder.itemDrops;
        this.breakSound = builder.breakSound;
        this.breakParticle = builder.breakParticle;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public Material getBlockMaterial() {
        return this.blockMaterial;
    }

    public int getBlockStrength() {
        return this.blockStrength;
    }

    public int getBlockDurability() {
        return this.blockDurability;
    }

    public long getBlockCooldown() {
        return this.blockCooldown;
    }

    @NotNull
    public List<ToolType> getCorrectTools() {
        return this.correctTools;
    }

    public boolean hasProperTool() {
        return !this.correctTools.isEmpty();
    }

    @NotNull
    public List<DDrop> getItemDrops() {
        return this.itemDrops;
    }

    @NotNull
    public Sound getBreakSound() {
        return this.breakSound;
    }

    @NotNull
    public Material getBreakParticle() {
        return this.breakParticle;
    }

    public static class Builder {
        private String id;
        private Material blockMaterial;
        private int blockStrength;
        private int blockDurability;
        private long blockCooldown;
        private List<ToolType> correctTools;
        private List<DDrop> itemDrops;
        private Sound breakSound;
        private Material breakParticle;

        public Builder() {
            this.blockMaterial = Material.AIR;
            this.blockStrength = 0;
            this.blockDurability = -1;
            this.blockCooldown = 1000;
            this.correctTools = List.of();
            this.itemDrops = List.of();
            this.breakSound = Sound.BLOCK_STONE_BREAK;
            this.breakParticle = Material.BEDROCK;
        }

        public Builder id(@NotNull String id) {
            this.id = id;
            return this;
        }

        public Builder blockMaterial(@NotNull Material material) {
            this.blockMaterial = material;
            return this;
        }

        /**
         * Tool strength requirement to destroy block
         *
         * @param strength
         *
         * @return
         */
        public Builder blockStrength(int strength) {
            this.blockStrength = strength;
            return this;
        }

        /**
         * Ticks taken to destroy block, base damage efficiency is 1
         *
         * @param durability
         *
         * @return
         */
        public Builder blockDurability(int durability) {
            this.blockDurability = Math.max(0, durability);
            return this;
        }

        /**
         * Flags block to be instantly broken
         *
         * @return
         */
        public Builder noBlockDurability() {
            this.blockDurability = 0;
            return this;
        }

        /**
         * Flags block to never break
         *
         * @return
         */
        public Builder infiniteDurability() {
            this.blockDurability = -1;
            return this;
        }

        /**
         * Milliseconds cooldown the block will have after being destroyed
         *
         * @param msValue
         *
         * @return
         */
        public Builder blockCooldown(long msValue) {
            this.blockCooldown = Math.max(1000, msValue);
            return this;
        }

        /**
         * Array of tools that can destroy this block
         *
         * @param toolTypes
         *
         * @return
         */
        public Builder correctTools(@NotNull List<ToolType> toolTypes) {
            this.correctTools = toolTypes;
            return this;
        }

        /**
         * Flags that block has no tools to destroy it(becomes unbreakable)
         *
         * @return
         */
        public Builder noCorrectTools() {
            this.correctTools = List.of();
            return this;
        }

        /**
         * Array of ItemDrop objects that will drop when block is destroyed
         *
         * @param itemDrops
         *
         * @return
         */
        public Builder itemDrops(@NotNull List<DDrop> itemDrops) {
            this.itemDrops = itemDrops;
            return this;
        }

        /**
         * Flags block to have no drops
         *
         * @return
         */
        public Builder noItemDrops() {
            this.itemDrops = List.of();
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
            this.breakParticle = particle;
            return this;
        }

        public DBlock build() {
            return new DBlock(this);
        }
    }
}