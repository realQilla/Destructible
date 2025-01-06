package net.qilla.destructible.menus.input;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftSign;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SignInput {

    private final DPlayer dPlayer;
    private final ExecutorService executorService;

    public SignInput(DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void init(List<String> signText, Consumer<String> onComplete) {
        CompletableFuture.supplyAsync(() -> {
            dPlayer.getPlugin().addThread(Thread.currentThread());
            BlockPos blockPos = CoordUtil.locToBlockPos(dPlayer.getCraftPlayer().getLocation()).offset(0, -7, 0);

            CraftSign<SignBlockEntity> sign = new CraftSign<>(dPlayer.getCraftPlayer().getWorld(), new SignBlockEntity(blockPos, Blocks.OAK_SIGN.defaultBlockState()));
            sign.setLine(0, "");
            sign.setLine(1, signText.get(0));
            sign.setLine(2, signText.get(1));
            sign.setLine(3, signText.get(2));
            sign.update();

            Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> {
                dPlayer.sendPacket(new ClientboundBlockUpdatePacket(blockPos, sign.getHandle()));
                dPlayer.sendPacket(new ClientboundBlockEntityDataPacket(blockPos, BlockEntityType.SIGN, sign.getUpdateNBT()));
                dPlayer.sendPacket(new ClientboundOpenSignEditorPacket(blockPos, true));
            });

            return blockPos;
        }, executorService).thenCompose(blockPos ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return dPlayer.getMenuData().requestInput().get(60, TimeUnit.SECONDS);
                    } catch(TimeoutException ex) {
                        return null;
                    } catch(ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> {
                            dPlayer.sendPacket(new ClientboundBlockUpdatePacket(blockPos, dPlayer.getServerLevel().getBlockState(blockPos)));
                        });
                    }
                }, executorService)
        ).thenAccept(onComplete).thenAccept(v -> shutDown());
    }

    private void shutDown() {
        dPlayer.getPlugin().removeThread(Thread.currentThread());
        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(12, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch(InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}