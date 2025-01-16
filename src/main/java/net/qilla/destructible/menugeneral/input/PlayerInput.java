package net.qilla.destructible.menugeneral.input;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Bukkit;
import java.util.concurrent.*;
import java.util.function.Consumer;

public abstract class PlayerInput {

    private final DPlayer dPlayer;
    private final ExecutorService executorService;
    private final BlockPos blockPos;

    public PlayerInput(DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.executorService = Executors.newSingleThreadExecutor();
        this.blockPos = CoordUtil.locToBlockPos(getDPlayer().getCraftPlayer().getLocation()).offset(0, -7, 0);
    }

    public String awaitResponse() {
        try {
            return dPlayer.getMenuData().requestInput().get(60, TimeUnit.SECONDS);
        } catch(TimeoutException ex) {
            return "";
        } catch(ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            Bukkit.getScheduler().runTask(getDPlayer().getPlugin(), () -> {
                dPlayer.sendPacket(new ClientboundBlockUpdatePacket(blockPos, getDPlayer().getServerLevel().getBlockState(blockPos)));
            });
        }
    }

    public void shutDown() {
        dPlayer.getPlugin().removeThread(Thread.currentThread());
        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch(InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public DPlayer getDPlayer() {
        return this.dPlayer;
    }

    public ExecutorService getExecutor() {
        return this.executorService;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public abstract void init(Consumer<String> onComplete);
}