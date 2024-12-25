package net.qilla.destructible.mining.block;

import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.tool.DToolType;
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
    private final List<DToolType> properTools;
    private final List<DDrop> itemDrops;
    private final Sound sound;
    private final Material particle;

    public DBlock(DBlock.Properties properties) {
        this.id = properties.id;
        this.material = properties.material;
        this.strength = properties.strengthRequirement;
        this.durability = properties.durability;
        this.msCooldown = properties.msCooldown;
        this.properTools = properties.properTools;
        this.itemDrops = properties.itemDrops;
        this.sound = properties.sound;
        this.particle = properties.particle;
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
    public List<DToolType> getProperTools() {
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

    public static class Properties {
        private String id;
        private Material material;
        private int strengthRequirement;
        private float durability;
        private int msCooldown;
        private List<DToolType> properTools;
        private List<DDrop> itemDrops;
        private Sound sound;
        private Material particle;

        public static DBlock.Properties of() {
            return new DBlock.Properties();
        }

        public DBlock.Properties id(@NotNull String id) {
            this.id = id;
            return this;
        }

        public DBlock.Properties material(@NotNull Material material) {
            this.material = material;
            return this;
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
         * Milliseconds cooldown the block will have after being destroyed
         * @param msCooldown
         * @return
         */
        public DBlock.Properties msCooldown(int msCooldown) {
            this.msCooldown = Math.max(100, msCooldown);
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
        public DBlock.Properties properTools(@NotNull List<DToolType> tool) {
            this.properTools = tool;
            return this;
        }

        /**
         * Flags that block has no tools to destroy it(becomes unbreakable)
         * @return
         */
        public DBlock.Properties noTools() {
            this.properTools = List.of();
            return this;
        }

        /**
         * Array of ItemDrop objects that will drop when block is destroyed
         * @param itemDrop
         * @return
         */
        public DBlock.Properties itemDrops(@NotNull List<DDrop> itemDrop) {
            this.itemDrops = itemDrop;
            return this;
        }

        /**
         * Flags block to have no drops
         * @return
         */
        public DBlock.Properties noDrops() {
            this.itemDrops = List.of();
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
            this.material = Material.BEDROCK;
            this.strengthRequirement = 0;
            this.durability = -1;
            this.msCooldown = 100;
            this.properTools = List.of();
            this.itemDrops = List.of();
            this.sound = Sound.BLOCK_STONE_BREAK;
            this.particle = Material.BEDROCK;
        }
    }}
