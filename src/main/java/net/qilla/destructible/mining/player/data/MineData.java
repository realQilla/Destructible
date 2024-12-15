package net.qilla.destructible.mining.player.data;

import net.minecraft.core.Direction;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DTools;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Player data specifically related to the current block being mined.
 */
public final class MineData {
    private final Location location;
    private final Direction direction;
    private final Equipment equipment;
    private DBlock dBlock;
    private DTool dTool;
    private Durability durability;
    private int crackStage = 0;

    public MineData(@NotNull final Equipment equipment, @NotNull final Location location, @NotNull final Direction dir) {
        this.location = location;
        this.direction = dir;
        this.equipment = equipment;
        updateBlock();
        updateTool();
    }

    /**
     * Damages the block by a specified amount
     * @param amount
     * @return Returns true when the block durability threshold has been passed.
     */
    public boolean damage(float amount) {
        this.crackStage = Math.round(((durability.getTotal() - durability.getCurrent()) * 9 / durability.getTotal()));
        return this.durability.damage(amount) <= 0;
    }

    /**
     * Updates the currently cached block.
     */
    public DBlock updateBlock() {
        DBlock dBlock = Registries.BLOCKS.get(this.location.getWorld().getBlockAt(this.location).getType());
        dBlock = dBlock == null ? DBlocks.NONE : dBlock;
        this.durability = new Durability(dBlock.getDurability());
        return this.dBlock = dBlock;
    }

    public DTool updateTool() {
        String toolId = this.equipment.getHeldItem().getPersistentDataContainer().get(DataKey.TOOL, PersistentDataType.STRING);
        return this.dTool = toolId == null ? DTools.DEFAULT : Registries.TOOLS.get(toolId);
    }

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    @NotNull
    public Direction getDirection() {
        return this.direction;
    }

    @NotNull
    public DBlock getDBlock() {
        return this.dBlock;
    }

    @NotNull
    public DTool getDTool() {
        return this.dTool;
    }

    public Durability getDurabilityTotal() {
        return this.durability;
    }

    public int getCrackStage() {
        return crackStage;
    }
}