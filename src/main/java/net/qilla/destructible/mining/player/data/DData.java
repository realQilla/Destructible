package net.qilla.destructible.mining.player.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.player.DMiner;
import net.qilla.destructible.util.DBlockUtil;
import net.qilla.destructible.util.DItemUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class DData {
    private final ServerboundPlayerActionPacket packet;
    private final DMiner dMiner;
    private final World world;
    private DBlock dBlock;
    private final BlockPos blockPos;
    private final Location blockLoc;
    private final int posHashCode;
    private final Direction direction;
    private DTool dTool;
    private Durability blockDurability;
    private int blockStage = 0;

    public DData(@NotNull final DMiner dMiner, @NotNull final ServerboundPlayerActionPacket packet, @NotNull DBlock dBlock) {
        this.packet = packet;
        this.dMiner = dMiner;
        this.world = dMiner.getPlayer().getWorld();
        this.dBlock = dBlock;
        this.blockPos = packet.getPos();
        this.blockLoc = DBlockUtil.blockPosToLoc(packet.getPos(), this.world);
        this.posHashCode = this.blockPos.hashCode();
        this.direction = packet.getDirection();
        this.blockDurability = new Durability(dBlock.getDurability());
        updateDTool();
    }

    public boolean damage(float amount) {
        this.blockStage = Math.round(((blockDurability.getTotal() - blockDurability.getCurrent()) * 9 / blockDurability.getTotal()));
        return this.blockDurability.damage(amount) <= 0;
    }

    @NotNull
    public DData refresh(DBlock dBlock) {
        return new DData(this.dMiner, this.packet, dBlock);
    }

    public DTool updateDTool() {
        return this.dTool = DItemUtil.getDTool(this.dMiner.getEquipment().getHeldItem());
    }

    @NotNull
    public World getWorld() {
        return this.world;
    }

    @NotNull
    public DBlock getDBlock() {
        return this.dBlock;
    }

    @NotNull
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @NotNull
    public Location getBlockLoc() {
        return this.blockLoc;
    }

    public int getPosHashCode() {
        return this.posHashCode;
    }

    @NotNull
    public Direction getDirection() {
        return this.direction;
    }

    @NotNull
    public DTool getDTool() {
        return this.dTool;
    }

    public Durability getDurabilityTotal() {
        return this.blockDurability;
    }

    public int getBlockStage() {
        return blockStage;
    }
}