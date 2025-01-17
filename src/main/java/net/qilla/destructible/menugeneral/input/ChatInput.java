package net.qilla.destructible.menugeneral.input;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ChatInput extends PlayerInput {

    private final Component chatMessage;

    public ChatInput(DPlayer dPlayer, Component chatMessage) {
        super(dPlayer);
        this.chatMessage = chatMessage;
    }

    @Override
    public void init(Consumer<String> onComplete) {
        CompletableFuture.runAsync(this::openMenu, super.getExecutor())
                .thenCompose(v -> CompletableFuture.supplyAsync(this::awaitResponse, super.getExecutor()))
                .thenAccept(onComplete).thenRun(super::shutDown);
    }

    public void openMenu() {
        getDPlayer().getPlugin().addThread(Thread.currentThread());

        Bukkit.getScheduler().runTask(getDPlayer().getPlugin(), () -> {
            getDPlayer().sendPacket(new ClientboundContainerClosePacket(getDPlayer().getCraftPlayer().getHandle().containerMenu.containerId));
            getDPlayer().sendMessage(chatMessage);
        });
    }
}