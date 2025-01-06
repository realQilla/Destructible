package net.qilla.destructible.menus.input;

import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftSign;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SignInput extends PlayerInput {

    private final List<String> signText;

    public SignInput(DPlayer dPlayer, List<String> signText) {
        super(dPlayer);
        this.signText = signText;
    }

    @Override
    public void init(Consumer<String> onComplete) {
        CompletableFuture.runAsync(() -> openMenu(signText), getExecutor())
                .thenCompose(v -> CompletableFuture.supplyAsync(this::awaitResponse, getExecutor()))
                .thenAccept(onComplete).thenRun(super::shutDown);
    }

    public void openMenu(List<String> text) {
        getDPlayer().getPlugin().addThread(Thread.currentThread());

        CraftSign<SignBlockEntity> sign = new CraftSign<>(getDPlayer().getCraftPlayer().getWorld(), new SignBlockEntity(getBlockPos(), Blocks.OAK_SIGN.defaultBlockState()));
        for(int i = 1; i < 3; i++) {
            sign.setLine(i, text.get(i - 1));
        }
        sign.update();

        Bukkit.getScheduler().runTask(getDPlayer().getPlugin(), () -> {
            getDPlayer().sendPacket(new ClientboundBlockUpdatePacket(getBlockPos(), sign.getHandle()));
            getDPlayer().sendPacket(new ClientboundBlockEntityDataPacket(getBlockPos(), BlockEntityType.SIGN, sign.getUpdateNBT()));
            getDPlayer().sendPacket(new ClientboundOpenSignEditorPacket(getBlockPos(), true));
        });
    }
}