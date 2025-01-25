package net.qilla.destructible.menugeneral.input;

import com.google.common.base.Preconditions;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.player.DPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.function.Consumer;

public abstract class PlayerInput {

    private static final int SEC_TIMEOUT = 60;
    private final Destructible plugin;
    private final DPlayer dPlayer;
    private final ExecutorService executorService;

    public PlayerInput(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        Preconditions.checkNotNull(plugin, "Plugin cannot be null");
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        this.plugin = plugin;
        this.dPlayer = dPlayer;
        this.executorService = Destructible.getInstance().getdExecutor().getExecutor();}

    public String awaitResponse() {
        try {
            return dPlayer.getMenuHolder().requestInput().get(SEC_TIMEOUT, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            return "";
        } catch(ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public DPlayer getDPlayer() {
        return this.dPlayer;
    }

    public Destructible getPlugin() {
        return this.plugin;
    }

    public ExecutorService getExecutor() {
        return this.executorService;
    }

    public abstract void init(Consumer<String> onComplete);
}