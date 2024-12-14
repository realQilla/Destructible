package net.qilla.destructible.mining.block;

import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

public class DestructibleBlock {
    private final int strengthRequirement;
    private final float durability;
    private final ToolType[] properTools;
    private final ItemDrop[] itemDrops;
    private final Sound sound;
    private final Material particle;

    public DestructibleBlock(DestructibleBlock.Properties properties) {
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

    public ToolType[] getProperTools() {
        return this.properTools;
    }

    public boolean hasProperTool() {
        return this.properTools.length != 0;
    }

    public ItemDrop[] getItemDrops() {
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
        private ToolType[] properTools;
        private ItemDrop[] itemDrops;
        private Sound sound;
        private Material particle;

        public static DestructibleBlock.Properties of() {
            return new DestructibleBlock.Properties();
        }

        public DestructibleBlock.Properties strengthRequirement(int strength) {
            this.strengthRequirement = strength;
            return this;
        }

        public DestructibleBlock.Properties durability(int durability) {
            this.durability = durability;
            return this;
        }

        public DestructibleBlock.Properties instaBreak() {
            return this.durability(0);
        }

        public DestructibleBlock.Properties properTools(ToolType[] tool) {
            this.properTools = tool;
            return this;
        }

        public DestructibleBlock.Properties noTools() {
            this.properTools = new ToolType[0];
            return this;
        }

        public DestructibleBlock.Properties itemDrops(ItemDrop[] itemDrop) {
            this.itemDrops = itemDrop;
            return this;
        }

        public DestructibleBlock.Properties noDrops() {
            this.itemDrops = new ItemDrop[0];
            return this;
        }

        public DestructibleBlock.Properties sound(@NotNull Sound sound) {
            this.sound = sound;
            return this;
        }

        public DestructibleBlock.Properties particle(@NotNull Material particle) {
            this.particle = particle;
            return this;
        }

        private Properties() {
            this.strengthRequirement = -1;
            this.durability = -1;
            this.properTools = new ToolType[0];
            this.itemDrops = new ItemDrop[0];
            this.sound = Sound.BLOCK_STONE_BREAK;
            this.particle = Material.BEDROCK;
        }
    }
}