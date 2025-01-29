package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.registry.DPlayerDataRegistry;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.DSounds;
import net.qilla.destructible.files.LoadedDestructibleBlocksFile;
import net.qilla.destructible.files.LoadedDestructibleBlocksGroupedFile;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.menugeneral.menu.select.DBlockSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.HighlightSelectMenu;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.BlockEdit;
import net.qilla.destructible.player.DPlayerData;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QStaticMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.sound.QSounds.Menu;
import net.qilla.qlibrary.util.tools.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WorldLoadingMenu extends QStaticMenu {

    private static final Set<UUID> BLOCK_EDITOR_SET = DRegistry.BLOCK_EDITORS;
    private static final Map<Long, Map<Integer, String>> LOADED_BLOCK_MAP = DRegistry.LOADED_BLOCKS;

    private final DPlayerData playerData;
    private final BlockEdit blockEdit;

    public WorldLoadingMenu(@NotNull Plugin plugin, @NotNull PlayerData playerData) {
        super(plugin, playerData);
        this.playerData = (DPlayerData) playerData;
        this.blockEdit = this.playerData.getBlockEdit();
        this.loadSettings();
    }

    private void loadSettings() {
        super.addSocket(List.of(
                this.loadBlockSocket(), this.viewBlockSocket(), this.clearLoadedBlocksSocket(), this.saveLoadedBlocksSocket(),
                this.reloadLoadedBlocksSocket(), infoSocket()
        ));
        if(playerData.getBlockEdit().getDblock() != null) super.addSocket(this.disableLoadBlockSocket());
    }

    private Socket loadBlockSocket() {
        String dBlock = blockEdit.getDblock() == null ? "<red><bold>NONE" : "<white>" + blockEdit.getDblock().getId();
        String size = blockEdit.getRecursionSize() <= 0 ? "<red><bold>DISABLED" : "<white>" + NumberUtil.numberComma(blockEdit.getRecursionSize());

        return new QSocket(20, QSlot.of(builder -> builder
                .material((blockEdit.getRecursionSize() > 0 && blockEdit.getDblock() != null) ? Material.HONEY_BOTTLE : Material.POTION)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Load Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Loading block " + dBlock),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Recursion size " + size),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to select a block to be loaded"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to set a recursion size, or nothing to disable")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), this::clickLoadBlock, CooldownType.MENU_CLICK);
    }

    private boolean clickLoadBlock(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            CompletableFuture<DBlock> future = new CompletableFuture<>();
            new DBlockSelectMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
            future.thenAccept(dBlock -> {
                blockEdit.setDblock(dBlock);
                BLOCK_EDITOR_SET.add(super.getPlayer().getUniqueId());

                super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You have enabled Destructible build mode, all place blocks will be marked as <gold>" + dBlock.getId() + "</gold>."));
                super.getPlayer().playSound(DSounds.ENABLE_SETTING, true);
            });
            return true;
        } else if(clickType.isRightClick()) {

            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^",
                    "Total block",
                    "recursion size");

            new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
                Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                    if(!result.isBlank()) {
                        try {
                            blockEdit.setRecursionSize(NumberUtil.minMax(0, 128000, Integer.parseInt(result)));
                        } catch(NumberFormatException ignored) {
                        }
                    } else blockEdit.setRecursionSize(0);
                    super.addSocket(this.loadBlockSocket());
                    getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                    super.open(false);
                });
            });
            return true;
        } else return false;
    }

    private Socket disableLoadBlockSocket() {
        return new QSocket(29, DSlots.DISABLE_LOADED_BLOCK_VIEW, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                blockEdit.setDblock(null);
                blockEdit.setRecursionSize(0);
                super.addSocket(this.loadBlockSocket());
                super.removeSocket(this.disableLoadBlockSocket().index());
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket viewBlockSocket() {
        return new QSocket(22, DSlots.VIEW_LOADED_BLOCKS, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new HighlightSelectMenu(super.getPlugin(), super.getPlayerData(), blockEdit.getBlockHighlight().getVisibleDBlocks()).open(true);
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket saveLoadedBlocksSocket() {
        return new QSocket(0, DSlots.SAVED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to save"
                );

                SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                LoadedDestructibleBlocksFile.getInstance().save();
                                LoadedDestructibleBlocksGroupedFile.getInstance().save();
                            });
                            super.getPlayer().sendMessage("<yellow>Loaded custom blocks have been <green><bold>SAVED</green>!");
                            super.getPlayer().playSound(DSounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket reloadLoadedBlocksSocket() {
        return new QSocket(1, DSlots.RELOADED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to reload"
                );

                SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                LoadedDestructibleBlocksFile.getInstance().load();
                                LoadedDestructibleBlocksGroupedFile.getInstance().load();
                            });
                            super.getPlayer().sendMessage("<yellow>Loaded custom blocks have been <aqua><bold>RELOADED</aqua>!");
                            super.getPlayer().playSound(DSounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket clearLoadedBlocksSocket() {
        return new QSocket(2, DSlots.CLEAR_SAVED, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to clear"
                );

                SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                LoadedDestructibleBlocksFile.getInstance().clear();
                                LoadedDestructibleBlocksGroupedFile.getInstance().clear();
                            });
                            for(UUID uuid : BLOCK_EDITOR_SET) {
                                DPlayerData playerData = DPlayerDataRegistry.getInstance().getData(uuid);
                                if(playerData == null) continue;
                                playerData.getBlockEdit().getBlockHighlight().removeHighlightsAll();
                            }
                            super.getPlayer().sendMessage("<yellow>Loaded custom blocks have been <red><bold>CLEARED</red>!");
                            super.getPlayer().playSound(DSounds.GENERAL_SUCCESS_2, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket infoSocket() {
        return new QSocket(44, QSlot.of(builder -> builder
                .material(Material.ENDER_EYE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Loaded Block Info"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray><white>" + NumberUtil.numberChar(LOADED_BLOCK_MAP.values().stream().mapToInt(Map::size).sum(), false) + "</white> loaded blocks within <white>" + NumberUtil.numberChar(LOADED_BLOCK_MAP.size(), false) + "</white> chunks")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ));
    }

    @Override
    public void refreshSockets() {
        loadSettings();
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.LOAD_BLOCK_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.FIVE)
                .title(Component.text("Loading Blocks"))
                .menuIndex(4)
                .returnIndex(40));
    }
}