package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import net.qilla.qlibrary.util.tools.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class BlockOverviewMenu extends DynamicMenu<DBlock> {

    private static final Collection<DBlock> DBLOCK_COLLECTION = DRegistry.BLOCKS.values();

    public BlockOverviewMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        super(plugin, dPlayer, DBLOCK_COLLECTION);
        super.addSocket(new Socket(6, Slots.CREATE_NEW, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new BlockModificationMenu(super.getPlugin(), super.getDPlayer(), null).open(true);
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK));
        super.addSocket(this.clearlocksSocket());
        super.addSocket(this.saveBlocksSocket());
        super.addSocket(this.reloadBlocksSocket());
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, DBlock item) {
        String toolList = item.getCorrectTools().isEmpty() ? "<red>None" : StringUtil.toNameList(item.getCorrectTools().stream().toList(), ", ");

        return new Socket(index, Slot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(Component.text(item.getId()))
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
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to view lootpool"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><yellow><key:key.swapOffhand> to make modifications")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new BlockLootpoolOverview(super.getPlugin(), super.getDPlayer(), item).open(true);
                return true;
            } else if(clickType == ClickType.SWAP_OFFHAND) {
                new BlockModificationMenu(super.getPlugin(), super.getDPlayer(), item).open(true);
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU);
    }

    private Socket saveBlocksSocket() {
        return new Socket(0, Slot.of(builder -> builder
                .material(Material.SLIME_BALL)
                .displayName(MiniMessage.miniMessage().deserialize("<green><bold>SAVE</bold> Custom Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to save custom block changes")
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
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> super.getPlugin().getCustomBlocksFile().save());
                            super.getDPlayer().sendMessage("<yellow>Custom blocks have been <green><bold>SAVED</green>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket reloadBlocksSocket() {
        return new Socket(1, Slot.of(builder -> builder
                .material(Material.SNOWBALL)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua><bold>RELOAD</bold> Custom Blocks"))
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
                                super.getPlugin().getCustomBlocksFile().load();
                                Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                            });
                            super.getDPlayer().sendMessage("<yellow>Custom blocks have been <aqua><bold>RELOADED</aqua>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket clearlocksSocket() {
        return new Socket(2, Slot.of(builder -> builder
                .material(Material.FIRE_CHARGE)
                .displayName(MiniMessage.miniMessage().deserialize("<red><bold>CLEAR</bold> Custom Blocks"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to clear custom blocks")
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
                                super.getPlugin().getCustomBlocksFile().clear();
                                Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                            });
                            super.getDPlayer().sendMessage("<yellow>All custom blocks have been <red><bold>CLEARED</red>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS_2, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.BLOCK_OVERVIEW_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Custom Block Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public DynamicConfig dynamicConfig() {
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
}