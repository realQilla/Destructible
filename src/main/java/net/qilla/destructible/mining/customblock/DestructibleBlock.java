package net.qilla.destructible.mining.customblock;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DestructibleBlock {

    private final Sound sound;
    private final Material blockParticle;
    private final float durability;
    private final ItemStack[] properTools;
    private final ItemDrop[] itemDrops;

    public DestructibleBlock(DestructibleBlock.Properties properties) {
        this.sound = properties.sound;
        this.blockParticle = properties.blockParticle;
        this.durability = properties.durability;
        this.properTools = properties.properTools;
        this.itemDrops = properties.itemDrops;
    }

    public Sound getSound() {
        return this.sound;
    }

    public Material getBlockParticle() {
        return this.blockParticle;
    }

    public float getDurability() {
        return this.durability;
    }

    public ItemStack[] getProperTools() {
        return this.properTools;
    }

    public ItemDrop[] getItemDrops() {
        return this.itemDrops;
    }

    public static class Properties {
        Material material;
        Sound sound;
        Material blockParticle;
        float durability;
        ItemStack[] properTools;
        ItemDrop[] itemDrops;

        public static DestructibleBlock.Properties of() {
            return new DestructibleBlock.Properties();
        }

        public DestructibleBlock.Properties material(@NotNull Material material) {
            this.material = material;
            return this;
        }

        public DestructibleBlock.Properties sound(@NotNull Sound sound) {
            this.sound = sound;
            return this;
        }

        public DestructibleBlock.Properties blockParticle(@NotNull Material blockParticle) {
            this.blockParticle = blockParticle;
            return this;
        }

        public DestructibleBlock.Properties durability(int durability) {
            this.durability = durability;
            return this;
        }

        public DestructibleBlock.Properties instaBreak() {
            return this.durability(0);
        }

        public DestructibleBlock.Properties properTools(ItemStack[] tool) {
            this.properTools = tool;
            return this;
        }

        public DestructibleBlock.Properties noTools() {
            this.properTools = new ItemStack[0];
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

        private Properties() {
            this.sound = Sound.BLOCK_STONE_BREAK;
            this.blockParticle = Material.BEDROCK;
            this.durability = -1;
            this.properTools = new ItemStack[0];
            this.itemDrops = new ItemDrop[0];
        }
    }
}
