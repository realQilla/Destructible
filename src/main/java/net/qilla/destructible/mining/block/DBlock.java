package net.qilla.destructible.mining.block;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DBlock {
    private final String id;
    private final Material blockMaterial;
    private final int blockStrength;
    private final long blockDurability;
    private final long blockCooldown;
    private final Set<ToolType> correctTools;
    private final List<ItemDrop> lootpool;
    private final Sound breakSound;
    private final Material breakParticle;

    public DBlock(Builder builder) {
        this.id = builder.id;
        this.blockMaterial = builder.blockMaterial;
        this.blockStrength = builder.blockStrength;
        this.blockDurability = builder.blockDurability;
        this.blockCooldown = builder.blockCooldown;
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
        return this.blockMaterial;
    }

    public int getStrength() {
        return this.blockStrength;
    }

    public long getDurability() {
        return this.blockDurability;
    }

    public long getCooldown() {
        return this.blockCooldown;
    }

    @NotNull
    public Set<ToolType> getCorrectTools() {
        return this.correctTools;
    }

    public boolean hasProperTool() {
        return !this.correctTools.isEmpty();
    }

    @NotNull
    public List<ItemDrop> getLootpool() {
        return this.lootpool;
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
                .blockMaterial(dBlock.getMaterial())
                .blockStrength(dBlock.getStrength())
                .blockDurability(dBlock.getDurability())
                .blockCooldown(dBlock.getCooldown())
                .correctTools(dBlock.getCorrectTools())
                .lootpool(dBlock.getLootpool())
                .breakSound(dBlock.getBreakSound())
                .breakParticle(dBlock.getBreakParticle());
    }

    public static class Builder {
        private String id;
        private Material blockMaterial;
        private int blockStrength;
        private long blockDurability;
        private long blockCooldown;
        private Set<ToolType> correctTools;
        private List<ItemDrop> lootpool;
        private Sound breakSound;
        private Material breakParticle;

        public Builder() {
            this.blockMaterial = Material.AIR;
            this.blockStrength = 0;
            this.blockDurability = -1;
            this.blockCooldown = 1000;
            this.correctTools = new HashSet<>();
            this.lootpool = List.of();
            this.breakSound = Sound.BLOCK_STONE_BREAK;
            this.breakParticle = Material.BEDROCK;
        }

        public Builder id(@NotNull String id) {
            Preconditions.checkNotNull(id, "ID cannot be null");
            this.id = id;
            return this;
        }

        public Builder blockMaterial(@NotNull Material material) {
            Preconditions.checkNotNull(material, "Material cannot be null");
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
        public Builder blockDurability(long durability) {
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
         * @param ms
         *
         * @return
         */
        public Builder blockCooldown(long ms) {
            this.blockCooldown = Math.max(1000, ms);
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
         * Set of tools that can destroy this block
         *
         * @param toolTypes
         *
         * @return
         */
        public Builder correctTools(@NotNull List<ToolType> toolTypes) {
            Preconditions.checkNotNull(toolTypes, "ToolTypes cannot be null");
            this.correctTools = new HashSet<>(toolTypes);
            return this;
        }

        /**
         * Flags that block has no tools to destroy it(becomes unbreakable)
         *
         * @return
         */
        public Builder noCorrectTools() {
            this.correctTools = new HashSet<>();
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
         * Flags block to have no drops
         *
         * @return
         */
        public Builder noItemDrops() {
            this.lootpool = List.of();
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