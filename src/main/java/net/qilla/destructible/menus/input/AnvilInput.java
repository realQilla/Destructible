package net.qilla.destructible.menus.input;

import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class AnvilInput extends PlayerInput {

    private final String anvilText;

    public AnvilInput(DPlayer dPlayer, String anvilText) {
        super(dPlayer);
        this.anvilText = anvilText;
    }

    @Override
    public void init(Consumer<String> onComplete) {
        CompletableFuture.runAsync(() -> openMenu(anvilText), getExecutor())
                .thenCompose(v -> CompletableFuture.supplyAsync(this::awaitResponse, getExecutor()))
                .thenAccept(onComplete).thenRun(super::shutDown);
    }

    public void openMenu(String text) {
        getDPlayer().getPlugin().addThread(Thread.currentThread());

        Bukkit.getScheduler().runTask(getDPlayer().getPlugin(), () -> {
            //getDPlayer().sendPacket(new ClientboundBlockUpdatePacket(getBlockPos(), sign.getHandle()));
            //getDPlayer().sendPacket(new ClientboundBlockEntityDataPacket(getBlockPos(), BlockEntityType.SIGN, sign.getUpdateNBT()));

            Display display = Display.of(consumer -> consumer
                    .displayName(MiniMessage.miniMessage().deserialize(text))
            );
            ItemStack itemStack = CraftItemStack.asNMSCopy(display.get());

            AnvilMenu anvilMenu = new AnvilMenu(getDPlayer().getCraftPlayer().getHandle().nextContainerCounter(), getDPlayer().getServerPlayer().getInventory(), ContainerLevelAccess.NULL);
            anvilMenu.setItem(0, 1, itemStack);
            anvilMenu.setTitle(Component.literal("Anvil123"));
            anvilMenu.checkReachable = false;

            getDPlayer().sendPacket(new ClientboundOpenScreenPacket(anvilMenu.containerId, MenuType.ANVIL, Component.empty()));
        });
    }
}