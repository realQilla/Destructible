package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.files.CustomBlocksFile;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.DynamicConfig;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QDynamicMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Slots;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.registry.RegistrySubscriber;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import net.qilla.qlibrary.util.tools.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockOverviewMenu extends QDynamicMenu<DBlock> implements RegistrySubscriber {

    public BlockOverviewMenu(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData) {
        super(plugin, playerData, List.copyOf(DRegistry.BLOCKS.values()));

        super.addSocket(this.blockCreationSocket());
        super.addSocket(this.clearlocksSocket());
        super.addSocket(this.saveBlocksSocket());
        super.addSocket(this.reloadBlocksSocket());
        super.populateModular();
        super.finalizeMenu();

        DRegistry.BLOCKS.subscribe(this);
    }

    @Override
    public void onUpdate() {
        super.updateItemPopulation(List.copyOf(DRegistry.BLOCKS.values()));
    }

    @Override
    public @Nullable Socket createSocket(int index, DBlock item) {
        String toolList = item.getCorrectTools().isEmpty() ? "<red>None" : StringUtil.toNameList(item.getCorrectTools().stream().toList(), ", ");

        return new QSocket(index, QSlot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(Component.text(item.getID()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + NumberUtil.romanNumeral(item.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + item.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + TimeUtil.getTime(item.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + toolList),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <white>" + item.getLootpool().size()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + item.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particle <white>" + StringUtil.toName(item.getBreakParticle().toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to view make modifications"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><yellow><gold>② <key:key.mouse.right></gold> to view lootpool")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new BlockModificationMenu(super.getPlugin(), super.getPlayerData(), item).open(true);
                return true;
            } else if(clickType.isRightClick()) {
                new BlockLootpoolOverview(super.getPlugin(), super.getPlayerData(), item).open(true);
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU);
    }

    private Socket blockCreationSocket() {
        return new QSocket(46, DSlots.MODIFICATION_CREATE, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new BlockModificationMenu(super.getPlugin(), super.getPlayerData()).open(true);
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket saveBlocksSocket() {
        return new QSocket(0, Slots.SAVED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            List<String> signText = List.of("^^^^^^^^^^^^^^^", "Type CONFIRM", "to save");
            super.requestSignInput(signText, result -> {
                    if(result.equals("CONFIRM")) {
                        Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> CustomBlocksFile.getInstance().save());
                        super.getPlayer().sendMessage("<yellow>Custom blocks have been <green><bold>SAVED</green>!");
                        super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
                    }
                    super.open(false);
                });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    private Socket reloadBlocksSocket() {
        return new QSocket(1, Slots.RELOADED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            List<String> signText = List.of("^^^^^^^^^^^^^^^", "Type CONFIRM", "to reload");
            super.requestSignInput(signText, result -> {
                    if(result.equals("CONFIRM")) {
                        Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                            CustomBlocksFile.getInstance().load();
                            Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                        });
                        super.getPlayer().sendMessage("<yellow>Custom blocks have been <aqua><bold>RELOADED</aqua>!");
                        super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
                    }
                    super.open(false);
                });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    private Socket clearlocksSocket() {
        return new QSocket(2, Slots.CLEAR_SAVED, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            List<String> signText = List.of("^^^^^^^^^^^^^^^", "Type CONFIRM", "to clear");
            super.requestSignInput(signText, result -> {
                if(result.equals("CONFIRM")) {
                    Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                        CustomBlocksFile.getInstance().clear();
                        Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                    });
                    super.getPlayer().sendMessage("<yellow>All custom blocks have been <red><bold>CLEARED</red>!");
                    super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS_2, true);
                }
                super.open(false);
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.BLOCK_OVERVIEW_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Custom Block Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
        return DynamicConfig.of(
                builder -> builder
                        .dynamicSlots(List.of(
                                9, 10, 11, 12, 13, 14, 15, 16, 17,
                                18, 19, 20, 21, 22, 23, 24, 25, 26,
                                27, 28, 29, 30, 31, 32, 33, 34, 35,
                                36, 37, 38, 39, 40, 41, 42, 43, 44
                        ))
                        .nextIndex(52)
                        .previousIndex(7)
                        .shiftAmount(9)
        );
    }

    @Override
    public void shutdown() {
        this.clearSockets();
        super.getInventory().close();
        DRegistry.BLOCKS.unsubscribe(this);
    }
}