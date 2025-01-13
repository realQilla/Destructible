package net.qilla.destructible.menus.blockmodify;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.BlockMenu;
import net.qilla.destructible.menus.DestructibleMenu;
import net.qilla.destructible.menus.MenuSize;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockMenuModify extends DestructibleMenu {

    private boolean fullMenu;
    private final BlockMenu blockMenu;
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

    public BlockMenuModify(@NotNull DPlayer dPlayer, @NotNull BlockMenu blockMenu, @NotNull DBlock dBlock) {
        super(dPlayer);
        Preconditions.checkNotNull(dBlock, "Block cannot be null");
        this.fullMenu = true;
        this.blockMenu = blockMenu;
        this.dBlock = dBlock;
        this.id = dBlock.getId();
        this.material = dBlock.getMaterial();
        this.strength = dBlock.getStrength();
        this.durability = dBlock.getDurability();
        this.cooldown = dBlock.getCooldown();
        this.correctTools = new HashSet<>(dBlock.getCorrectTools());
        this.lootpool = new ArrayList<>(dBlock.getLootpool());
        this.breakSound = dBlock.getBreakSound();
        this.breakParticle = dBlock.getBreakParticle();

        super.register(materialSlot());
        super.register(removeSlot());

        this.populateSettings();
    }

    public BlockMenuModify(@NotNull DPlayer dPlayer, @NotNull BlockMenu blockMenu) {
        super(dPlayer);
        this.fullMenu = false;
        this.blockMenu = blockMenu;
        this.dBlock = null;

        super.register(Slot.of(13, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.HOPPER_MINECART)
                        .displayName(MiniMessage.miniMessage().deserialize("<gray>Click with block"))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to search for a material")
                        )))
                ))
                .action((this::clickMainBlock))
                .appearSound(Sounds.ITEM_APPEAR)
        ), 3);
    }

    private void populateSettings() {
        List<Slot> list = new ArrayList<>(List.of(idSlot(), materialSlot(), strengthSlot(),
                durabilitySlot(), lootpoolSlot(), correctToolsSlot(),
                cooldownSlot(), particleSlot(), soundSlot()));
        Collections.shuffle(list);
        list.add(buildSlot());

        super.register(list, 1);
    }

    private void clickMainBlock(Slot slot, InventoryClickEvent event) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            Material cursorMaterial = event.getCursor().getType();
            if(!cursorMaterial.isSolid()) return;

            this.setMaterial(cursorMaterial);
            getDPlayer().getCraftPlayer().setItemOnCursor(null);

        } else if(clickType.isRightClick()) {
            new SetMaterial(super.getDPlayer(), this).openMenu(true);
        }
    }

    public Slot buildSlot() {
        return Slot.of(31, builder -> builder
                .display(Displays.CONFIRM)
                .action(this::build)
                .appearSound(Sounds.IMPORTANT_ITEM_APPEAR)
                .clickSound(Sounds.SUCCESS)
        );
    }

    public Slot removeSlot() {
        return Slot.of(53, builder -> builder
                .display(Display.of(consumer -> consumer
                        .material(Material.BARRIER)
                        .displayName(MiniMessage.miniMessage().deserialize("<red>Remove!"))
                        .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to permanently"),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>delete this block")
                                ))
                        )
                ))
                .action(this::remove)
                .appearSound(Sounds.IMPORTANT_ITEM_APPEAR)
                .clickSound(Sounds.SUCCESS)
        );
    }

    private void build(Slot slot, InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            DBlock dBlock = new DBlock.Builder()
                    .id(this.id)
                    .blockMaterial(this.material)
                    .blockStrength(this.strength)
                    .blockDurability(this.durability)
                    .blockCooldown(this.cooldown)
                    .correctTools(this.correctTools)
                    .lootpool(this.lootpool)
                    .breakSound(this.breakSound)
                    .breakParticle(this.breakParticle)
                    .build();

            if(this.dBlock != null) Registries.DESTRUCTIBLE_BLOCKS.remove(this.dBlock.getId());
            Registries.DESTRUCTIBLE_BLOCKS.put(dBlock.getId(), dBlock);
            getDPlayer().getPlugin().getCustomBlocksFile().save();
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully registered."));
            super.returnToPrevious();
        }
    }

    private void remove(Slot slot, InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            if(this.dBlock == null) {
                getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>This block does not currently exist!"));
                return;
            }

            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully unregistered."));
            Registries.DESTRUCTIBLE_BLOCKS.remove(dBlock.getId());
            getDPlayer().getPlugin().getCustomBlocksFile().save();
            super.pullPreviousMenu();
            this.blockMenu.updateModular();
        }
    }

    public Slot materialSlot() {
        if(this.material == null) this.material = Material.STONE;
        return Slot.of(13, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(this.material)
                        .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Material"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + FormatUtil.toName(this.material.toString())),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click with block to change material"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to search for a material")
                        )))))
                .action(this::clickMainBlock)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    public Slot idSlot() {
        if(this.id == null) this.id = UUID.randomUUID().toString();
        return Slot.of(22, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.OAK_SIGN)
                        .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Block ID"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + this.id),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((event, slot) -> inputID())
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    protected Slot durabilitySlot() {
        if(this.durability == null) this.durability = 0L;

        return Slot.of(10, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.IRON_INGOT)
                        .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Durability"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + this.durability),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((event, slot) -> inputDurability())
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    protected Slot strengthSlot() {
        if(this.strength == null) this.strength = 0;
        return Slot.of(11, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.RESIN_BRICK)
                        .displayName(MiniMessage.miniMessage().deserialize("<Red>Block Strength"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + this.strength),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((event, slot) -> inputStrength())
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    protected Slot lootpoolSlot() {
        if(this.lootpool == null) this.lootpool = new ArrayList<>();
        return Slot.of(38, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.PINK_BUNDLE)
                        .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((event, slot) -> new SetLootpool(getDPlayer(), this.lootpool).openMenu(true))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    protected Slot correctToolsSlot() {
        if(this.correctTools == null) this.correctTools = new HashSet<>();
        return Slot.of(42, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.BLACK_BUNDLE)
                        .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Correct Tools"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value:"),
                                MiniMessage.miniMessage().deserialize("<!italic><white>" + (this.correctTools.isEmpty() ?
                                        "<red>None" : FormatUtil.toNameList(this.correctTools.stream().toList()))),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((Slot, event) -> new SetToolType(getDPlayer(), this).openMenu(true))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    protected Slot cooldownSlot() {
        if(this.cooldown == null) this.cooldown = 0L;
        return Slot.of(19, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.GOLD_INGOT)
                        .displayName(MiniMessage.miniMessage().deserialize("<gold>Block Cooldown"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + FormatUtil.getTime(this.cooldown, true)),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((slot, event) -> inputCooldown())
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    public Slot particleSlot() {
        if(this.breakParticle == null) this.breakParticle = Material.STONE;
        return Slot.of(15, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.HEART_OF_THE_SEA)
                        .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Break Particle"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + FormatUtil.toName(this.breakParticle.toString())),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((slot, event) -> new SetParticle(getDPlayer(), this).openMenu(true))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    public Slot soundSlot() {
        if(this.breakSound == null) this.breakSound = Sound.BLOCK_STONE_BREAK;
        return Slot.of(16, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.NAUTILUS_SHELL)
                        .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Break Sound"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + this.breakSound.toString()),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                        )))
                ))
                .action((slot, event) -> new SetSound(getDPlayer(), this).openMenu(true))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    private void inputID() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block ID",
                "value");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                if(!result.isEmpty()) {
                    if(Registries.DESTRUCTIBLE_BLOCKS.containsKey(result)) {
                        super.getDPlayer().sendMessage("<red>Block ID already exists.");
                        super.getDPlayer().playSound(Sounds.ERROR, true);
                    } else {
                        this.id = result;
                        super.register(this.idSlot());
                        getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                    }
                }
                super.openMenu(false);
            });
        });
    }

    private void inputStrength() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block strength",
                "value");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.strength = Integer.parseInt(result);
                    super.register(this.strengthSlot());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.openMenu(false);
            });
        });
    }

    private void inputDurability() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block durability",
                "value");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.durability = Long.parseLong(result);
                    super.register(this.durabilitySlot());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.openMenu(false);
            });
        });
    }

    private void inputCooldown() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block cooldown",
                "value");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.cooldown = FormatUtil.stringToMs(result);
                    super.register(this.cooldownSlot());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.openMenu(false);
            });
        });
    }

    @Override
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(event.getClickedInventory().getHolder() instanceof BlockMenuModify) {
            event.setCancelled(true);
        }
    }

    public void setMaterial(Material material) {
        this.material = material;
        this.register(materialSlot());
        if(!this.fullMenu) {
            this.populateSettings();
            this.fullMenu = true;
        }
    }

    public List<ItemDrop> getLootpool() {
        return this.lootpool;
    }

    public Set<ToolType> getToolTypes() {
        return this.correctTools;
    }

    public void setBreakParticle(Material material) {
        this.breakParticle = material;
        this.register(particleSlot());
    }

    public void setBreakSound(Sound breakSound) {
        this.breakSound = breakSound;
        this.register(soundSlot());
    }

    @Override
    public Component tile() {
        return Component.text("Block Modification");
    }

    @Override
    public MenuSize menuSize() {
        return MenuSize.SIX;
    }

    @Override
    public Slot menuSlot() {
        return Slot.of(4, Displays.BLOCK_MODIFICATION_MENU);
    }

    @Override
    public int returnIndex() {
        return 49;
    }
}