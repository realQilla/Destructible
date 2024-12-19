package net.qilla.destructible.mining.block;

import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.tool.DToolType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

public final class DBlock {
    private final int strengthRequirement;
    private final float durability;
    private final DToolType[] properTools;
    private final DDrop[] itemDrops;
    private final Sound sound;
    private final Material particle;

    public DBlock(DBlock.Properties properties) {
        this.strengthRequirement = properties.strengthRequirement;
        this.durability = properties.durability;
        this.properTools = properties.properTools;
        this.itemDrops = properties.itemDrops;
        this.sound = properties.sound;
        this.particle = properties.particle;
    }

    public int getStrengthRequirement() {
        return this.strengthRequirement;
    }

    public float getDurability() {
        return this.durability;
    }

    public DToolType[] getProperTools() {
        return this.properTools;
    }

    public boolean hasProperTool() {
        return this.properTools.length != 0;
    }

    public DDrop[] getItemDrops() {
        return this.itemDrops;
    }

    public Sound getSound() {
        return this.sound;
    }

    public Material getParticle() {
        return this.particle;
    }

    public static class Properties {
        private int strengthRequirement;
        private float durability;
        private DToolType[] properTools;
        private DDrop[] itemDrops;
        private Sound sound;
        private Material particle;

        public static DBlock.Properties of() {
            return new DBlock.Properties();
        }

        /**
         * Tool strength requirement to destroy block
         * @param strength
         * @return
         */
        public DBlock.Properties strengthRequirement(int strength) {
            this.strengthRequirement = strength;
            return this;
        }

        /**
         * Ticks taken to destroy block, base damage efficiency is 1
         * @param durability
         * @return
         */
        public DBlock.Properties durability(int durability) {
            this.durability = Math.max(1, durability);
            return this;
        }

        /**
         * Flags block to be instantly broken
         * @return
         */
        public DBlock.Properties instaBreak() {
            this.durability = 0;
            return this;
        }

        /**
         * Flags block to never break
         * @return
         */
        public DBlock.Properties neverBreak() {
            this.durability = -1;
            return this;
        }

        /**
         * Array of tools that can destroy this block
         * @param tool
         * @return
         */
        public DBlock.Properties properTools(DToolType[] tool) {
            this.properTools = tool;
            return this;
        }

        /**
         * Flags that block has no tools to destroy it(becomes unbreakable)
         * @return
         */
        public DBlock.Properties noTools() {
            this.properTools = new DToolType[0];
            return this;
        }

        /**
         * Array of ItemDrop objects that will drop when block is destroyed
         * @param itemDrop
         * @return
         */
        public DBlock.Properties itemDrops(DDrop[] itemDrop) {
            this.itemDrops = itemDrop;
            return this;
        }

        /**
         * Flags block to have no drops
         * @return
         */
        public DBlock.Properties noDrops() {
            this.itemDrops = new DDrop[0];
            return this;
        }

        /**
         * Sound played when block is destroyed
         * @param sound
         * @return
         */
        public DBlock.Properties sound(@NotNull Sound sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Block particle played when block is destroyed
         * @param particle
         * @return
         */
        public DBlock.Properties particle(@NotNull Material particle) {
            this.particle = particle;
            return this;
        }

        private Properties() {
            this.strengthRequirement = -1;
            this.durability = -1;
            this.properTools = new DToolType[0];
            this.itemDrops = new DDrop[0];
            this.sound = Sound.BLOCK_STONE_BREAK;
            this.particle = Material.BEDROCK;
        }
    }}
