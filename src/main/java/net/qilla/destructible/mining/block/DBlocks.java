package net.qilla.destructible.mining.block;


import net.qilla.destructible.data.Registries;
import java.util.function.Function;

public final class DBlocks {

    public static final DBlock NONE = new DBlock(DBlock.Properties.of()
            .noDrops()
            .noTools()
            .neverBreak()
    );

    private static DBlock register(String id, Function<DBlock.Properties, DBlock> factory, DBlock.Properties properties) {
        return Registries.DESTRUCTIBLE_BLOCKS.put(id, factory.apply(properties.id(id)));
    }
}