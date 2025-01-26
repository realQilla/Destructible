package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DSounds;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.menugeneral.menu.select.BlockParticleSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.BlockSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.MultiToolTypeSelectionMenu;
import net.qilla.destructible.menugeneral.menu.select.SoundSelectMenu;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QStaticMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.StaticMenu;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.MenuSound;
import net.qilla.qlibrary.util.tools.StringUtil;
import net.qilla.qlibrary.util.tools.TimeUtil;
import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlockModificationMenu extends QStaticMenu {

    private static final Map<String, DBlock> DBLOCK_MAP = DRegistry.BLOCKS;

    private boolean lockedMenu = true;
    private final DBlock dBlock;
    private String id = UUID.randomUUID().toString();
    private Material material = Material.STONE;
    private int strength = 0;
    private long durability = -1;
    private long cooldown = 5000;
    private Set<ToolType> correctTools = new HashSet<>();
    private List<ItemDrop> lootpool = new ArrayList<>();
    private Sound breakSound = Sound.BLOCK_STONE_BREAK;
    private Material breakParticle = Material.STONE;

    public BlockModificationMenu(@NotNull Plugin plugin, @NotNull PlayerData playerData, @NotNull DBlock dBlock) {
        super(plugin, playerData);
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null");

        this.dBlock = dBlock;

        this.lockedMenu = false;
        this.id = dBlock.getId();
        this.material = dBlock.getMaterial();
        this.strength = dBlock.getStrength();
        this.durability = dBlock.getDurability();
        this.cooldown = dBlock.getCooldown();
        this.correctTools = new HashSet<>(dBlock.getCorrectTools());
        this.lootpool = new ArrayList<>(dBlock.getLootpool());
        this.breakSound = dBlock.getBreakSound();
        this.breakParticle = dBlock.getBreakParticle();

        super.addSocket(getSettingsSockets(), 25);
        super.addSocket(removeSocket());
        super.finalizeMenu();
    }

    public BlockModificationMenu(@NotNull Plugin plugin, @NotNull PlayerData playerData) {
        super(plugin, playerData);
        this.dBlock = null;

        super.addSocket(emptyMaterialSocket(), 250);
        super.finalizeMenu();
    }

    private List<Socket> getSettingsSockets() {
        List<Socket> socketList = new ArrayList<>(List.of(
                materialSocket(), idSocket(), strengthSocket(), durabilitySocket(),
                lootpoolSocket(), correctToolSocket(), cooldownSocket(), breakParticleSocket(),
                breakSoundSocket()
        ));
        Collections.shuffle(socketList);
        socketList.add(buildSocket());
        socketList.add(removeSocket());

        return socketList;
    }

    private boolean clickMaterial(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            Material cursorMaterial = event.getCursor().getType();
            if(cursorMaterial.isEmpty()) {
                CompletableFuture<Material> future = new CompletableFuture<>();
                new BlockSelectMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
                future.thenAccept(material -> {
                    if(material == null) return;
                    this.material = material;
                    lockedMenu = false;
                    super.addSocket(getSettingsSockets());
                });
            } else {
                if(!cursorMaterial.isSolid()) {
                    super.getPlayer().playSound(DSounds.GENERAL_ERROR, true);
                    return false;
                }
                this.material = cursorMaterial;
                lockedMenu = false;
                super.addSocket(getSettingsSockets());
                super.getPlayer().setItemOnCursor(null);
            }
            return true;
        } else return false;
    }

    public Socket buildSocket() {
        return new QSocket(31, DSlots.CONFIRM, this::build, CooldownType.MENU_CLICK);
    }

    private boolean build(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        DBlock dBlock = DBlock.builder()
                .id(id)
                .material(material)
                .strength(strength)
                .durability(durability)
                .cooldown(cooldown)
                .correctTools(correctTools)
                .lootpool(lootpool)
                .breakSound(breakSound)
                .breakParticle(breakParticle)
                .build();
        if(this.dBlock != null) {
            DBLOCK_MAP.remove(this.dBlock.getId());
            super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully replaced by " + id + "!"));
        } else super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully registered!"));
        DBLOCK_MAP.put(dBlock.getId(), dBlock);
        return super.returnMenu();
    }

    public Socket removeSocket() {
        return new QSocket(53, DSlots.MODIFICATION_REMOVE, this::remove, CooldownType.MENU_CLICK);
    }

    private boolean remove(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully unregistered."));
        DBLOCK_MAP.remove(dBlock.getId());
        super.getPlayer().playSound(MenuSound.RESET, true);
        return super.returnMenu();
    }

    public Socket emptyMaterialSocket() {
        return new QSocket(13, QSlot.of(builder -> builder
                .material(Material.HOPPER_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <red>Empty"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a material or use an item to quickset")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::clickMaterial, CooldownType.MENU_CLICK);
    }

    public Socket materialSocket() {
        return new QSocket(13, QSlot.of(builder -> builder
                .material(material)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(material.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a material or use an item to quickset")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::clickMaterial, CooldownType.MENU_CLICK);
    }

    public Socket idSocket() {
        return new QSocket(22, QSlot.of(builder -> builder
                .material(Material.OAK_SIGN)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Block ID"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + id),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputID, CooldownType.MENU_CLICK);
    }

    private boolean inputID(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Unique identifier",
                "for this block");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    if(DBLOCK_MAP.containsKey(result)) {
                        super.getPlayer().sendMessage("<red>Block ID already exists.");
                        super.getPlayer().playSound(DSounds.GENERAL_ERROR, true);
                    } else {
                        id = result;
                        super.addSocket(this.idSocket());
                        super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                    }
                }
                super.open(false);
            });
        });
        return true;
    }

    protected Socket durabilitySocket() {
        return new QSocket(10, QSlot.of(builder -> builder
                .material(Material.IRON_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Durability"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + durability),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputDurability, CooldownType.MENU_CLICK);
    }

    private boolean inputDurability(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block durability",
                "value");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                try {
                    durability = Math.max(-1, Long.parseLong(result));
                    super.addSocket(this.durabilitySocket());
                    super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.open(false);
            });
        });
        return true;
    }

    protected Socket strengthSocket() {
        return new QSocket(11, QSlot.of(builder -> builder
                .material(Material.RESIN_BRICK)
                .displayName(MiniMessage.miniMessage().deserialize("<Red>Block Strength"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + strength),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputStrength, CooldownType.MENU_CLICK);
    }

    private boolean inputStrength(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block strength",
                "value");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                try {
                    strength = Integer.parseInt(result);
                    super.addSocket(this.strengthSocket());
                    super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.open(false);
            });
        });
        return true;
    }

    protected Socket lootpoolSocket() {
        return new QSocket(38, QSlot.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Unique lootpools <white>" + lootpool.size()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            new LootpoolModificationMenu(super.getPlugin(), super.getPlayerData(), lootpool).open(true);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    protected Socket correctToolSocket() {
        return new QSocket(42, QSlot.of(builder -> builder
                .material(Material.BLACK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Correct Tools"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current list:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + (correctTools.isEmpty() ? "<red>None" : StringUtil.toNameList(new ArrayList<>(correctTools), ", "))),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            new MultiToolTypeSelectionMenu(super.getPlugin(), super.getPlayerData(), correctTools).open(true);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    protected Socket cooldownSocket() {
        return new QSocket(19, QSlot.of(builder -> builder
                .material(Material.GOLD_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Block Cooldown"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + TimeUtil.getTime(cooldown, true)),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputCooldown, CooldownType.MENU_CLICK);
    }

    private boolean inputCooldown(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block cooldown",
                "value");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                try {
                    cooldown = TimeUtil.stringToMillis(result);
                    super.addSocket(this.cooldownSocket());
                    super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket breakParticleSocket() {
        return new QSocket(15, QSlot.of(builder -> builder
                .material(Material.HEART_OF_THE_SEA)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Break Particle"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(breakParticle.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            CompletableFuture<Material> future = new CompletableFuture<>();
            new BlockParticleSelectMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
            future.thenAccept(breakParticle -> this.breakParticle = breakParticle);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    public Socket breakSoundSocket() {
        return new QSocket(16, QSlot.of(builder -> builder
                .material(Material.NAUTILUS_SHELL)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Break Sound"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + breakSound.toString()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            CompletableFuture<Sound> future = new CompletableFuture<>();
            new SoundSelectMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
            future.thenAccept(sound -> this.breakSound = sound);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    @Override
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(event.getClickedInventory().getHolder() instanceof StaticMenu) {
            event.setCancelled(true);
            this.handleClick(event);
        }
    }

    @Override
    public void refreshSockets() {
        if(!lockedMenu) super.addSocket(this.getSettingsSockets());
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.BLOCK_MODIFICATION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Block Modification"))
                .menuIndex(4)
                .returnIndex(49));
    }
}