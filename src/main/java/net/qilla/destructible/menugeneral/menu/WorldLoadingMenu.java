package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DRegistry;
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
import net.qilla.destructible.player.DBlockEdit;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorldLoadingMenu extends StaticMenu {

    private static final Destructible PLUGIN = Destructible.getInstance();
    private final DPlayer dPlayer;
    private final DBlockEdit dBlockEdit = getDPlayer().getDBlockEdit();

    public WorldLoadingMenu(@NotNull DPlayer dPlayer) {
        super(dPlayer);
        this.dPlayer = dPlayer;
        this.loadSettings();
    }

    private void loadSettings() {
        super.addSocket(this.loadBlockSocket());
        if(dBlockEdit.getDblock() != null) super.addSocket(this.disableLoadBlockSocket());
        super.addSocket(this.viewBlockSocket());
        super.addSocket(this.clearLoadedBlocksSocket());
        super.addSocket(this.saveLoadedBlocksSocket());
        super.addSocket(this.reloadLoadedBlocksSocket());
    }

    private Socket loadBlockSocket() {
        String dBlock = dBlockEdit.getDblock() == null ? "<red><bold>NONE" : "<white>" + dBlockEdit.getDblock().getId();
        String size = dBlockEdit.getRecursionSize() <= 0 ? "<red><bold>DISABLED" : "<white>" + dBlockEdit.getRecursionSize();

        return new Socket(20, Slot.of(builder -> builder
                .material((dBlockEdit.getRecursionSize() > 0 && dBlockEdit.getDblock() != null) ? Material.HONEY_BOTTLE : Material.POTION)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Load block"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Loading block " + dBlock),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Recursion size " + size),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select a block to be loaded"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to set a recursion size, or nothing to disable")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), this::clickLoadBlock);
    }

    private boolean clickLoadBlock(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            CompletableFuture<DBlock> future = new CompletableFuture<>();
            new DBlockSelectMenu(dPlayer, future).open(true);
            future.thenAccept(dBlock -> {
                dBlockEdit.setDblock(dBlock);
                DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.add(dPlayer);

                dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You have enabled Destructible build mode, all place blocks will be marked as <gold>" + dBlock.getId() + "</gold>."));
                dPlayer.playSound(Sounds.ENABLE_SETTING, true);
            });
            return true;
        } else if(clickType.isRightClick()) {

            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^",
                    "Total block",
                    "recursion size");

            new SignInput(super.getDPlayer(), signText).init(result -> {
                Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                    if(!result.isBlank()) {
                        try {
                            dBlockEdit.setRecursionSize(FormatUtil.minMax(0, 128000, Integer.parseInt(result)));
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
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to disable block loading")
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
        });
    }

    private Socket viewBlockSocket() {
        return new Socket(22, Slot.of(builder -> builder
                .material(Material.OMINOUS_BOTTLE)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Loaded Block View"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to specify blocks to view")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new HighlightSelectMenu(dPlayer, dBlockEdit.getBlockHighlight().getVisibleBlocks()).open(true);
                return true;
            } else return false;
        });
    }

    private Socket clearLoadedBlocksSocket() {
        return new Socket(16, Slot.of(builder -> builder
                .material(Material.FIRE_CHARGE)
                .displayName(MiniMessage.miniMessage().deserialize("<red><bold>CLEAR</bold> Loaded Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to clear all loaded blocks within the world")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isShiftClick() && clickType.isLeftClick()) {
                PLUGIN.getLoadedBlocksFile().clear();
                PLUGIN.getLoadedBlocksGroupedFile().clear();
                DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.forEach(dPlayer -> dPlayer.getDBlockEdit().getBlockHighlight().removeHighlightsAll());
                dPlayer.sendMessage("<yellow>All loaded custom blocks have been <red><bold>CLEARED</red>!");
                dPlayer.playSound(Sounds.GENERAL_SUCCESS_2, true);
                return true;
            } else return false;
        });
    }

    private Socket saveLoadedBlocksSocket() {
        return new Socket(25, Slot.of(builder -> builder
                .material(Material.SLIME_BALL)
                .displayName(MiniMessage.miniMessage().deserialize("<green><bold>SAVE</bold> Loaded Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to save all loaded blocks within the world")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isShiftClick() && clickType.isLeftClick()) {
                PLUGIN.getLoadedBlocksFile().save();
                PLUGIN.getLoadedBlocksGroupedFile().save();
                dPlayer.sendMessage("<yellow>Loaded custom blocks have been <green><bold>SAVED</green>!");
                dPlayer.playSound(Sounds.GENERAL_SUCCESS, true);
                return true;
            } else return false;
        });
    }

    private Socket reloadLoadedBlocksSocket() {
        return new Socket(34, Slot.of(builder -> builder
                .material(Material.SNOWBALL)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua><bold>RE-LOAD</bold> Loaded Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to reload the config, undoing any unsaved changes.")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isShiftClick() && clickType.isLeftClick()) {
                PLUGIN.getLoadedBlocksFile().load();
                PLUGIN.getLoadedBlocksGroupedFile().load();
                dPlayer.sendMessage("<yellow>Loaded custom blocks have been <aqua><bold>RE-LOADED</aqua>!");
                dPlayer.playSound(Sounds.GENERAL_SUCCESS, true);
                return true;
            } else return false;
        });
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
                .menuSize(MenuSize.SIX)
                .title(Component.text("Load Blocks"))
                .menuIndex(4)
                .returnIndex(49));
    }
}