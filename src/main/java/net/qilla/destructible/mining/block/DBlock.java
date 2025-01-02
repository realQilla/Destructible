package net.qilla.destructible.mining.block;

import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DBlock {
    private final String id;
    private final Material material;
    private final int strength;
    private final float durability;
    private final int msCooldown;
    private final List<ToolType> properTools;
    private final List<DDrop> itemDrops;
    private final Sound sound;
    private final Material particle;

    public DBlock(Builder builder) {
        this.id = builder.id;
        this.material = builder.material;
        this.strength = builder.strengthRequirement;
        this.durability = builder.durability;
        this.msCooldown = builder.msCooldown;
        this.properTools = builder.properTools;
        this.itemDrops = builder.itemDrops;
        this.sound = builder.sound;
        this.particle = builder.particle;
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

    public float getDurability() {
        return this.durability;
    }

    public int getMsCooldown() {
        return this.msCooldown;
    }

    @NotNull
    public List<ToolType> getProperTools() {
        return this.properTools;
    }

    public boolean hasProperTool() {
        return !this.properTools.isEmpty();
    }

    @NotNull
    public List<DDrop> getItemDrops() {
        return this.itemDrops;
    }

    @NotNull
    public Sound getSound() {
        return this.sound;
    }

    @NotNull
    public Material getParticle() {
        return this.particle;
    }

    public static class Builder {
        private String id;
        private Material material;
        private int strengthRequirement;
        private float durability;
        private int msCooldown;
        private List<ToolType> properTools;
        private List<DDrop> itemDrops;
        private Sound sound;
        private Material particle;

        public Builder() {
            this.material = Material.AIR;
            this.strengthRequirement = 0;
            this.durability = -1;
            this.msCooldown = 1000;
            this.properTools = List.of();
            this.itemDrops = List.of();
            this.sound = Sound.BLOCK_STONE_BREAK;
            this.particle = Material.BEDROCK;
        }

        public Builder id(@NotNull String id) {
            this.id = id;
            return this;
        }

        public Builder material(@NotNull Material material) {
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
        public Builder strengthRequirement(int strength) {
            this.strengthRequirement = strength;
            return this;
        }

        /**
         * Ticks taken to destroy block, base damage efficiency is 1
         *
         * @param durability
         *
         * @return
         */
        public Builder durability(int durability) {
            this.durability = Math.max(1, durability);
            return this;
        }

        /**
         * Milliseconds cooldown the block will have after being destroyed
         *
         * @param msCooldown
         *
         * @return
         */
        public Builder msCooldown(int msCooldown) {
            this.msCooldown = Math.max(1000, msCooldown);
            return this;
        }

        /**
         * Flags block to be instantly broken
         *
         * @return
         */
        public Builder instaBreak() {
            this.durability = 0;
            return this;
        }

        /**
         * Flags block to never break
         *
         * @return
         */
        public Builder neverBreak() {
            this.durability = -1;
            return this;
        }

        /**
         * Array of tools that can destroy this block
         *
         * @param tool
         *
         * @return
         */
        public Builder properTools(@NotNull List<ToolType> tool) {
            this.properTools = tool;
            return this;
        }

        /**
         * Flags that block has no tools to destroy it(becomes unbreakable)
         *
         * @return
         */
        public Builder noTools() {
            this.properTools = List.of();
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
        public Builder noDrops() {
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
        public Builder sound(@NotNull Sound sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Block particle played when block is destroyed
         *
         * @param particle
         *
         * @return
         */
        public Builder particle(@NotNull Material particle) {
            this.particle = particle;
            return this;
        }

        public DBlock build() {
            return new DBlock(this);
        }
    }
}