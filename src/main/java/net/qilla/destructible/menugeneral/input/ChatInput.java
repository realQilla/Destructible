package net.qilla.destructible.menugeneral.input;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ChatInput extends PlayerInput {

    private final Component chatMessage;

    public ChatInput(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull Component chatMessage) {
        super(plugin, dPlayer);
        this.chatMessage = chatMessage;
    }

    @Override
    public void init(Consumer<String> onComplete) {
        this.openMenu();
        CompletableFuture.supplyAsync(super::awaitResponse, super.getExecutor())
                .thenAccept(onComplete);
    }

    public void openMenu() {
        Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
            getDPlayer().getCraftPlayer().closeInventory();
            getDPlayer().sendMessage(chatMessage);
        });
    }
}