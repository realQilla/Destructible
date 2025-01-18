package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.menugeneral.StaticMenu;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.menu.select.BlockParticleSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.BlockSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.CorrectToolMenu;
import net.qilla.destructible.menugeneral.menu.select.SoundSelectMenu;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.NumberUtil;
import net.qilla.destructible.util.StringUtil;
import net.qilla.destructible.util.TimeUtil;
import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlockModificationMenu extends StaticMenu {

    private boolean fullMenu;
    private final DBlock dBlock;
    private String id;
    private Material material;
    private Integer strength;
    private Long durability;
    private Long cooldown;
    private Set<ToolType> correctTools;
    private List<ItemDrop> lootpool;
    private Sound breakSound;
    private Material breakParticle;

    public BlockModificationMenu(@NotNull DPlayer dPlayer, @Nullable DBlock dBlock) {
        super(dPlayer);
        this.dBlock = dBlock;

        if(dBlock != null) {
            this.fullMenu = true;
            this.id = dBlock.getId();
            this.material = dBlock.getMaterial();
            this.strength = dBlock.getStrength();
            this.durability = dBlock.getDurability();
            this.cooldown = dBlock.getCooldown();
            this.correctTools = new HashSet<>(dBlock.getCorrectTools());
            this.lootpool = new ArrayList<>(dBlock.getLootpool());
            this.breakSound = dBlock.getBreakSound();
            this.breakParticle = dBlock.getBreakParticle();
            this.populateSettings();
        }
        super.addSocket(materialSocket(), 50);
        super.finalizeMenu();
    }

    private void populateSettings() {
        List<Socket> socketList = new ArrayList<>(List.of(
                idSocket(), strengthSocket(),
                durabilitySocket(), lootpoolSocket(), correctToolSocket(),
                cooldownSocket(), breakParticleSocket(), breakSoundSocket()
        ));
        Collections.shuffle(socketList);
        socketList.add(buildSocket());
        socketList.add(removeSocket());

        addSocket(socketList, 25);
    }

    private boolean clickMaterial(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            Material cursorMaterial = event.getCursor().getType();
            if(cursorMaterial.isEmpty()) {
                CompletableFuture<Material> future = new CompletableFuture<>();
                new BlockSelectMenu(super.getDPlayer(), future).open(true);
                future.thenAccept(material -> {
                    if(material != null) this.material = material;
                });
            } else {
                if(!cursorMaterial.isSolid()) {
                    getDPlayer().playSound(Sounds.GENERAL_ERROR, true);
                    return false;
                }
                this.material = cursorMaterial;
                getDPlayer().getCraftPlayer().setItemOnCursor(null);
                this.refreshSockets();
            }
            return true;
        } else return false;
    }

    public Socket buildSocket() {
        return new Socket(31, Slots.CONFIRM, this::build);
    }

    private boolean build(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        DBlock dBlock = new DBlock.Builder()
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
            DRegistry.DESTRUCTIBLE_BLOCKS.remove(this.dBlock.getId());
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully replaced by " + id + "!"));
        } else getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully registered!"));
        DRegistry.DESTRUCTIBLE_BLOCKS.put(dBlock.getId(), dBlock);
        getDPlayer().getPlugin().getCustomBlocksFile().save();
        return super.returnMenu();
    }

    public Socket removeSocket() {
        return new Socket(53, Slot.of(builder -> builder
                .material(Material.BARRIER)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Remove!"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to permanently"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>delete this block")
                )))
        ), this::remove);
    }

    private boolean remove(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        if(this.dBlock == null) {
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>This block does not currently exist!"));
            return false;
        }

        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully unregistered."));
        DRegistry.DESTRUCTIBLE_BLOCKS.remove(dBlock.getId());
        getDPlayer().getPlugin().getCustomBlocksFile().save();
        getDPlayer().playSound(Sounds.RESET, true);
        return super.returnMenu();
    }

    public Socket materialSocket() {
        if(material == null) {
            return new Socket(13, Slot.of(builder -> builder
                    .material(Material.HOPPER_MINECART)
                    .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Material"))
                    .lore(ItemLore.lore(List.of(
                            MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <red>Empty"),
                            Component.empty(),
                            MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click with either a block or nothing to set a material")
                    )))
                    .clickSound(Sounds.MENU_CLICK_ITEM)
                    .appearSound(Sounds.MENU_ITEM_APPEAR)
            ), this::clickMaterial);
        } else return new Socket(13, Slot.of(builder -> builder
                .material(material)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(material.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click with either a block or nothing to set a material")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::clickMaterial);
    }

    public Socket idSocket() {
        if(id == null) id = UUID.randomUUID().toString();
        return new Socket(22, Slot.of(builder -> builder
                .material(Material.OAK_SIGN)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Block ID"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + id),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::inputID);
    }

    private boolean inputID(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Unique identifier",
                "for this block");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                if(!result.isEmpty()) {
                    if(DRegistry.DESTRUCTIBLE_BLOCKS.containsKey(result)) {
                        super.getDPlayer().sendMessage("<red>Block ID already exists.");
                        super.getDPlayer().playSound(Sounds.GENERAL_ERROR, true);
                    } else {
                        id = result;
                        super.addSocket(this.idSocket());
                        getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                    }
                }
                super.open(false);
            });
        });
        return true;
    }

    protected Socket durabilitySocket() {
        if(durability == null) durability = -1L;
        return new Socket(10, Slot.of(builder -> builder
                .material(Material.IRON_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Durability"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + durability),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::inputDurability);
    }

    private boolean inputDurability(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block durability",
                "value");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    durability = Math.max(-1, Long.parseLong(result));
                    super.addSocket(this.durabilitySocket());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.open(false);
            });
        });
        return true;
    }

    protected Socket strengthSocket() {
        if(strength == null) strength = 0;
        return new Socket(11, Slot.of(builder -> builder
                .material(Material.RESIN_BRICK)
                .displayName(MiniMessage.miniMessage().deserialize("<Red>Block Strength"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + strength),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::inputStrength);
    }

    private boolean inputStrength(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block strength",
                "value");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    strength = Integer.parseInt(result);
                    super.addSocket(this.strengthSocket());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.open(false);
            });
        });
        return true;
    }

    protected Socket lootpoolSocket() {
        if(lootpool == null) lootpool = new ArrayList<>();
        return new Socket(38, Slot.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Unique lootpools <white>" + lootpool.size()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            new LootpoolSetMenu(getDPlayer(), lootpool).open(true);
            return true;
        });
    }

    protected Socket correctToolSocket() {
        if(correctTools == null) correctTools = new HashSet<>();
        return new Socket(42, Slot.of(builder -> builder
                .material(Material.BLACK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Correct Tools"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current list:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + (correctTools.isEmpty() ? "<red>None" : StringUtil.toNameList(new ArrayList<>(correctTools)))),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            new CorrectToolMenu(getDPlayer(), correctTools).open(true);
            return true;
        });
    }

    protected Socket cooldownSocket() {
        if(cooldown == null) cooldown = 1000L;
        return new Socket(19, Slot.of(builder -> builder
                .material(Material.GOLD_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Block Cooldown"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + TimeUtil.getTime(cooldown, true)),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::inputCooldown);
    }

    private boolean inputCooldown(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block cooldown",
                "value");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    cooldown = TimeUtil.stringToMillis(result);
                    super.addSocket(this.cooldownSocket());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket breakParticleSocket() {
        if(breakParticle == null) breakParticle = Material.STONE;
        return new Socket(15, Slot.of(builder -> builder
                .material(Material.HEART_OF_THE_SEA)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Break Particle"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(breakParticle.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            CompletableFuture<Material> future = new CompletableFuture<>();
            new BlockParticleSelectMenu(getDPlayer(), future).open(true);
            future.thenAccept(breakParticle -> this.breakParticle = breakParticle);
            return true;
        });
    }

    public Socket breakSoundSocket() {
        if(breakSound == null) breakSound = Sound.BLOCK_STONE_BREAK;
        return new Socket(16, Slot.of(builder -> builder
                .material(Material.NAUTILUS_SHELL)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Break Sound"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + breakSound.toString()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            CompletableFuture<Sound> future = new CompletableFuture<>();
            new SoundSelectMenu(getDPlayer(), future).open(true);
            future.thenAccept(sound -> this.breakSound = sound);
            return true;
        });
    }

    @Override
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(event.getClickedInventory().getHolder() instanceof StaticMenu) {
            event.setCancelled(true);
        }
    }

    @Override
    public void refreshSockets() {
        if(!fullMenu && material != null) {
            super.addSocket(materialSocket(), 100);
            this.populateSettings();
            fullMenu = true;
        } else if(material != null) {
            super.addSocket(List.of(
                    materialSocket(), idSocket(), strengthSocket(), durabilitySocket(),
                    lootpoolSocket(), correctToolSocket(), cooldownSocket(), breakParticleSocket(),
                    breakSoundSocket(), buildSocket(), removeSocket()
            ));
        }
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.BLOCK_MODIFICATION_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Block Modification"))
                .menuIndex(4)
                .returnIndex(49));
    }
}