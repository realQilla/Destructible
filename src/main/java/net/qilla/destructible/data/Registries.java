package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.player.DMiner;

import java.util.UUID;

public final class Registries {

    public static final Registry<ChunkCoord, Registry<Integer, DBlock>> CACHED_DBLOCKS = new Registry<>();

    public static final Registry<UUID, DMiner> DMINER_DATA = new Registry<>();
    public static final Registry<String, DBlock> DBLOCKS = new Registry<>();
    public static final Registry<String, DDrop> DDROPS = new Registry<>();
    public static final Registry<String, DTool> DTOOLS = new Registry<>();

    public static final Registry<UUID, DBlock> DBLOCK_EDITOR = new Registry<>();
}