package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryMaster;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.menugeneral.StaticMenu;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.menu.select.DBlockSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.HighlightSelectMenu;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DBlockEdit;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WorldLoadingMenu extends StaticMenu {

    private static final Set<DPlayer> BLOCK_EDITOR_SET = DRegistry.BLOCK_EDITORS;
    private static final Map<Long, ConcurrentHashMap<Integer, String>> LOADED_BLOCK_MAP = DRegistry.LOADED_BLOCKS;
    private final DBlockEdit dBlockEdit = getDPlayer().getDBlockEdit();

    public WorldLoadingMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        super(plugin, dPlayer);
        this.loadSettings();
    }

    private void loadSettings() {
        super.addSocket(List.of(
                this.loadBlockSocket(), this.viewBlockSocket(), this.clearLoadedBlocksSocket(), this.saveLoadedBlocksSocket(),
                this.reloadLoadedBlocksSocket(), infoSocket()
        ));
        if(dBlockEdit.getDblock() != null) super.addSocket(this.disableLoadBlockSocket());
    }

    private Socket loadBlockSocket() {
        String dBlock = dBlockEdit.getDblock() == null ? "<red><bold>NONE" : "<white>" + dBlockEdit.getDblock().getId();
        String size = dBlockEdit.getRecursionSize() <= 0 ? "<red><bold>DISABLED" : "<white>" + dBlockEdit.getRecursionSize();

        return new Socket(20, Slot.of(builder -> builder
                .material((dBlockEdit.getRecursionSize() > 0 && dBlockEdit.getDblock() != null) ? Material.HONEY_BOTTLE : Material.POTION)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Load Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Loading block " + dBlock),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Recursion size " + size),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to select a block to be loaded"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.right> to set a recursion size, or nothing to disable")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), this::clickLoadBlock, CooldownType.MENU_CLICK);
    }

    private boolean clickLoadBlock(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            CompletableFuture<DBlock> future = new CompletableFuture<>();
            new DBlockSelectMenu(super.getPlugin(), super.getDPlayer(), future).open(true);
            future.thenAccept(dBlock -> {
                dBlockEdit.setDblock(dBlock);
                BLOCK_EDITOR_SET.add(super.getDPlayer());

                super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You have enabled Destructible build mode, all place blocks will be marked as <gold>" + dBlock.getId() + "</gold>."));
                super.getDPlayer().playSound(Sounds.ENABLE_SETTING, true);
            });
            return true;
        } else if(clickType.isRightClick()) {

            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^",
                    "Total block",
                    "recursion size");

            new SignInput(super.getPlugin(), super.getDPlayer(), signText).init(result -> {
                Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                    if(!result.isBlank()) {
                        try {
                            dBlockEdit.setRecursionSize(NumberUtil.minMax(0, 128000, Integer.parseInt(result)));
                        } catch(NumberFormatException ignored) {
                        }
                    } else dBlockEdit.setRecursionSize(0);
                    super.addSocket(this.loadBlockSocket());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                    super.open(false);
                });
            });
            return true;
        } else return false;
    }

    private Socket disableLoadBlockSocket() {
        return new Socket(29, Slot.of(builder -> builder
                .material(Material.GLASS_BOTTLE)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Disable Block Loading"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to disable block loading")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                DBlockEdit dBlockEdit = getDPlayer().getDBlockEdit();
                dBlockEdit.setDblock(null);
                dBlockEdit.setRecursionSize(0);
                super.addSocket(this.loadBlockSocket());
                super.removeSocket(this.disableLoadBlockSocket().index());
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket viewBlockSocket() {
        return new Socket(22, Slot.of(builder -> builder
                .material(Material.OMINOUS_BOTTLE)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Loaded Block View"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to specify blocks to view")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new HighlightSelectMenu(super.getPlugin(), super.getDPlayer(), dBlockEdit.getBlockHighlight().getVisibleDBlocks()).open(true);
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket saveLoadedBlocksSocket() {
        return new Socket(0, Slot.of(builder -> builder
                .material(Material.SLIME_BALL)
                .displayName(MiniMessage.miniMessage().deserialize("<green><bold>SAVE</bold> Loaded Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to save all loaded blocks within the world")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to save"
                );

                SignInput signInput = new SignInput(super.getPlugin(), getDPlayer(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                super.getPlugin().getLoadedBlocksFile().save();
                                super.getPlugin().getLoadedBlocksGroupedFile().save();
                            });
                            super.getDPlayer().sendMessage("<yellow>Loaded custom blocks have been <green><bold>SAVED</green>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket reloadLoadedBlocksSocket() {
        return new Socket(1, Slot.of(builder -> builder
                .material(Material.SNOWBALL)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua><bold>RELOAD</bold> Loaded Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to load the config, undoing any unsaved changes.")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to reload"
                );

                SignInput signInput = new SignInput(super.getPlugin(), getDPlayer(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                super.getPlugin().getLoadedBlocksFile().load();
                                super.getPlugin().getLoadedBlocksGroupedFile().load();
                            });
                            super.getDPlayer().sendMessage("<yellow>Loaded custom blocks have been <aqua><bold>RELOADED</aqua>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket clearLoadedBlocksSocket() {
        return new Socket(2, Slot.of(builder -> builder
                .material(Material.FIRE_CHARGE)
                .displayName(MiniMessage.miniMessage().deserialize("<red><bold>CLEAR</bold> Loaded Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to clear all loaded blocks within the world")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to clear"
                );

                SignInput signInput = new SignInput(super.getPlugin(), getDPlayer(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                super.getPlugin().getLoadedBlocksFile().clear();
                                super.getPlugin().getLoadedBlocksGroupedFile().clear();
                            });
                            BLOCK_EDITOR_SET.forEach(dPlayer -> dPlayer.getDBlockEdit().getBlockHighlight().removeHighlightsAll());
                            super.getDPlayer().sendMessage("<yellow>Loaded custom blocks have been <red><bold>CLEARED</red>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS_2, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket infoSocket() {
        return new Socket(44, Slot.of(builder -> builder
                .material(Material.ENDER_EYE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Loaded Block Info"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray><white>" + NumberUtil.numberChar(LOADED_BLOCK_MAP.values().stream().mapToInt(Map::size).sum(), false) + "</white> loaded blocks within <white>" + NumberUtil.numberChar(LOADED_BLOCK_MAP.size(), false) + "</white> chunks")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ));
    }

    @Override
    public void refreshSockets() {
        loadSettings();
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.LOAD_BLOCK_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.FIVE)
                .title(Component.text("Load Blocks"))
                .menuIndex(4)
                .returnIndex(40));
    }
}