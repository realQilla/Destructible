package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import net.qilla.destructible.util.FormatUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public class BlockMenuModify extends DestructibleMenu {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Destructible Block Modification");

    private Slot build;

    private boolean fullMenu;

    private DBlock dBlock;

    private Slot idSlot;
    private Slot materialSlot;
    private Slot strengthSlot;
    private Slot durabilitySlot;
    private Slot cooldownSlot;
    private Slot toolTypeSlot;
    private Slot lootpoolSlot;
    private Slot soundSlot;
    private Slot particleSlot;

    private String id = null;
    private Material material = null;
    private Integer strength = null;
    private Long durability = null;
    private Long cooldown = null;
    private Set<ToolType> correctTools = new HashSet<>();
    private List<ItemDrop> lootpool = List.of();
    private Sound breakSound = null;
    private Material breakParticle = null;

    public BlockMenuModify(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);

        super.register(Slot.of(4, Displays.BLOCK_MODIFICATION_MENU));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> returnToPreviousMenu())));

        this.fullMenu = false;

        this.materialSlot = super.register(Slot.of(31, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.HOPPER_MINECART)
                                .displayName(MiniMessage.miniMessage().deserialize("<gray>Click with block"))
                                .lore(ItemLore.lore(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to search for a material")
                                )))
                        ))
                        .action(this::clickMainBlock)
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                5);
    }

    public BlockMenuModify(DPlayer dPlayer, DBlock dBlock) {
        super(dPlayer, SIZE, TITLE);
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null");

        super.register(Slot.of(4, Displays.BLOCK_MODIFICATION_MENU));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> returnToPreviousMenu())));

        this.dBlock = dBlock;
        this.id = dBlock.getId();
        this.material = dBlock.getMaterial();
        this.strength = dBlock.getStrength();
        this.durability = dBlock.getDurability();
        this.cooldown = dBlock.getCooldown();
        this.correctTools = dBlock.getCorrectTools();
        this.lootpool = dBlock.getLootpool();
        this.breakSound = dBlock.getBreakSound();
        this.breakParticle = dBlock.getBreakParticle();
        this.fullMenu = true;

        this.materialSlot = super.register(Slot.of(31, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(dBlock.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Material"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>" + dBlock.getMaterial()),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click with block to change material"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to search for a material")
                        )))
                ))
                .action(this::clickMainBlock)
        ));

        populateSettings();
    }

    private void populateSettings() {
        this.build = super.register(Slot.of(35, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.BARRIER)
                        .displayName(MiniMessage.miniMessage().deserialize("<red><bold>NOT COMPLETED"))
                ))
        ), 15, 25);

        populateID();
        populateMaterial();
        populateStrength();
        populateDurability();
        populateLootpool();
        populateCorrectTools();
        populateCooldown();
        populateParticle();
        populateSound();
    }

    private void clickMainBlock(Slot slot, InventoryClickEvent event) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            Material cursorMaterial = event.getCursor().getType();
            if(!cursorMaterial.isSolid()) return;

            this.material = cursorMaterial;
            getDPlayer().getCraftPlayer().setItemOnCursor(null);
            populateMaterial();

        } else if(clickType.isRightClick()) {
            new SetMaterial(super.getDPlayer(), this).openInventory(true);
        }
    }

    public void populateID() {
        StringBuilder stringBuilder = new StringBuilder("<!italic><gray>Current value: <white>");
        if(this.id == null) stringBuilder.append("<red>None");
        else stringBuilder.append(this.id);

        this.idSlot = super.register(Slot.of(13, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.OAK_SIGN)
                                .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Block ID"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize(stringBuilder.toString()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify the block's ID")
                                )))
                        ))
                        .action((event, slot) -> setID())
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);

        if(isCompleted()) unlockBuild();
    }

    public void populateMaterial() {
        if(!this.fullMenu) {
            this.fullMenu = true;
            populateSettings();
        }

        StringBuilder stringBuilder = new StringBuilder("<!italic><gray>Current value: <white>");
        if(this.material == null) stringBuilder.append("<red>None");
        else stringBuilder.append(FormatUtil.toName(this.material.toString()));

        super.register(materialSlot.modify(builder -> builder
                .display(materialSlot.getDisplay().ofNew(builder2 -> builder2
                        .material(this.material)
                        .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Material"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize(stringBuilder.toString()),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click with block to change material"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to search for a material")
                        )))))
                .appearSound(SoundSettings.of(Sound.BLOCK_LAVA_EXTINGUISH, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        ));

        if(isCompleted()) unlockBuild();
    }

    protected void populateStrength() {
        StringBuilder stringBuilder = new StringBuilder("<!italic><gray>Current value: <white>");
        if(this.strength == null) stringBuilder.append("<red>None");
        else stringBuilder.append(FormatUtil.romanNumeral(this.strength));

        this.strengthSlot = super.register(Slot.of(29, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.RESIN_BRICK)
                                .displayName(MiniMessage.miniMessage().deserialize("<Red>Block Strength"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize(stringBuilder.toString()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify the block's strength")
                                )))
                        ))
                        .action((event, slot) -> setStrength())
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);

        if(isCompleted()) unlockBuild();
    }

    protected void populateDurability() {
        StringBuilder stringBuilder = new StringBuilder("<!italic><gray>Current value: <white>");
        if(this.durability == null) stringBuilder.append("<red>None");
        else stringBuilder.append(this.durability);

        this.durabilitySlot = super.register(Slot.of(33, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.GOLDEN_PICKAXE)
                                .displayName(MiniMessage.miniMessage().deserialize("<gold>Block Durability"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize(stringBuilder.toString()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify the block's durability")
                                )))
                        ))
                        .action((event, slot) -> setDurability())
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);

        if(isCompleted()) unlockBuild();
    }

    protected void populateLootpool() {
        this.lootpoolSlot = super.register(Slot.of(16, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.BROWN_BUNDLE)
                                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to view or modify")
                                )))
                        ))
                        .action((event, slot) -> new SetLootpool(getDPlayer(), this.lootpool).openInventory(true))
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);
    }

    protected void populateCorrectTools() {
        StringBuilder stringBuilder = new StringBuilder();
        if(correctTools.isEmpty()) stringBuilder.append("<!italic><red>None");
        else stringBuilder.append("<!italic><white>").append(FormatUtil.toNameList(this.correctTools.stream().toList()));

        this.toolTypeSlot = super.register(Slot.of(52, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.WOODEN_AXE)
                                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Correct Tools"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value:"),
                                        MiniMessage.miniMessage().deserialize(stringBuilder.toString())
                                )))
                        ))
                        .action((Slot, event) -> new SetToolType(getDPlayer(), this, this.correctTools).openInventory(true))
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);
    }

    protected void populateCooldown() {
        StringBuilder stringBuilder = new StringBuilder("<!italic><gray>Current value: <white>");
        if(this.cooldown == null) stringBuilder.append("<red>None");
        else stringBuilder.append(FormatUtil.getTime(this.cooldown, true));

        this.cooldownSlot = super.register(Slot.of(10, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.PACKED_ICE)
                                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Cooldown"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize(stringBuilder.toString()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify the block's cooldown")
                                )))
                        ))
                        .action((event, slot) -> setCooldown())
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);

        if(isCompleted()) unlockBuild();
    }

    public void populateParticle() {
        StringBuilder stringBuilder = new StringBuilder("<!italic><gray>Current value: <white>");
        if(this.breakParticle == null) stringBuilder.append("<red>None");
        else stringBuilder.append(FormatUtil.toName(this.breakParticle.toString()));

        this.particleSlot = super.register(Slot.of(46, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.NAUTILUS_SHELL)
                                .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Break Particle"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize(stringBuilder.toString()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify the block's destruction particle")
                                )))
                        ))
                        .action((event, slot) -> new SetParticle(getDPlayer(), this).openInventory(true))
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);
    }

    public void populateSound() {
        StringBuilder soundString = new StringBuilder("<!italic><gray>Current value: <white>");
        if(this.breakSound == null) soundString.append("<red>None");
        else soundString.append(this.breakSound);

        this.particleSlot = super.register(Slot.of(27, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.NOTE_BLOCK)
                                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Break Sound"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize(soundString.toString()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify the block's destruction sound")
                                )))
                        ))
                        .action((slot, event) -> new SetSound(getDPlayer(), this).openInventory(true))
                        .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))),
                6, 20);
    }

    private void setID() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block ID",
                "value");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                if(!result.isEmpty()) {
                    if(Registries.DESTRUCTIBLE_BLOCKS.containsKey(result)) {
                        super.getDPlayer().sendMessage("<red>Block ID already exists.");
                        super.getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_NO, 0.5f, RandomUtil.between(0.5f, 1.5f), SoundCategory.PLAYERS, PlayType.PLAYER), true);
                    } else {
                        this.id = result;
                        this.populateID();
                    }
                }
                super.openInventory(false);
            });
        });
    }

    private boolean isCompleted() {
        return this.id != null &&
                this.material != null &&
                this.strength != null &&
                this.durability != null &&
                this.cooldown != null &&
                this.breakSound != null &&
                this.breakParticle != null;
    }

    private void unlockBuild() {
        this.register(this.build.modify(builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.END_CRYSTAL)
                        .displayName(MiniMessage.miniMessage().deserialize("<green><bold>COMPLETE"))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Finalize the creation or modification"),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>of this destructible block")
                        )))
                ))
                .action((slot, event) -> finalizeBlock())
        ), 10, 20);
    }

    private void finalizeBlock() {
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

        if(this.dBlock != null) {
            Registries.DESTRUCTIBLE_BLOCKS.computeIfPresent(this.dBlock.getId(), (key, value) -> null);
        }
        Registries.DESTRUCTIBLE_BLOCKS.put(dBlock.getId(), dBlock);
        getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_YES, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() +" has been successfully registered."));
        super.closeInventory();
    }

    private void setStrength() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block strength",
                "value");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.strength = Integer.parseInt(result);
                    this.populateStrength();
                } catch(NumberFormatException ignored) {
                }
                super.openInventory(false);
            });
        });
    }

    private void setDurability() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block durability",
                "value");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.durability = Long.parseLong(result);
                    this.populateDurability();
                } catch(NumberFormatException ignored) {
                }
                super.openInventory(false);
            });
        });
    }

    private void setCooldown() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block cooldown",
                "value");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.cooldown = FormatUtil.stringToMs(result);
                    this.populateCooldown();
                } catch(NumberFormatException ignored) {
                }
                super.openInventory(false);
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
    }

    public void setCorrectTools(Set<ToolType> correctTools) {
        this.correctTools = correctTools;
    }

    public void setBreakParticle(Material material) {
        this.breakParticle = material;
    }

    public void setBreakSound(Sound breakSound) {
        this.breakSound = breakSound;
    }
}

class SetMaterial extends SearchMenu<Material> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Block Search");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );
    private final BlockMenuModify menu;

    public SetMaterial(DPlayer dPlayer, BlockMenuModify menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS, Registry.MATERIAL.stream()
                .filter(Material::isSolid)
                .toList());

        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> menu.returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));

        super.register(Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, clickType) -> super.searchFor())
                .clickSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))));

        this.menu = menu;
    }

    @Override
    public Slot createSlot(int index, Material item) {
        Display display = Display.of(builder -> builder
                .material(item)
                .displayName(MiniMessage.miniMessage().deserialize(FormatUtil.toName(item.toString())))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select material")
                        )))
        );
        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> {
                    menu.setMaterial(item);
                    menu.populateMaterial();
                    super.returnToPreviousMenu();
                }));
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> rotatePrevious(slot, clickType, 9)));
    }

    @Override
    public String getString(Material item) {
        return item.toString();
    }
}

class SetSound extends SearchMenu<Sound> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Sound Search");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );
    private final BlockMenuModify menu;

    public SetSound(DPlayer dPlayer, BlockMenuModify menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                Registry.SOUNDS.stream()
                        .toList());

        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> menu.returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));
        super.register(Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, clickType) -> super.searchFor())
                .clickSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))));

        this.menu = menu;
    }

    @Override
    public Slot createSlot(int index, Sound item) {
        Display display = Display.of(builder -> builder
                .material(Material.NOTE_BLOCK)
                .displayName(MiniMessage.miniMessage().deserialize(item.toString()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select sound"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to listen")
                )))
        );
        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        menu.setBreakSound(item);
                        menu.populateSound();
                        super.returnToPreviousMenu();
                    } else if(clickType.isRightClick()) {
                        getDPlayer().getCraftPlayer().stopAllSounds();
                        getDPlayer().playSound(SoundSettings.of(item, 0.5f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER), true);
                    }
                }));
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> rotatePrevious(slot, clickType, 9)));
    }

    @Override
    public String getString(Sound item) {
        return item.toString();
    }
}

class SetParticle extends SearchMenu<Material> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Block Search");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );
    private final BlockMenuModify menu;

    public SetParticle(DPlayer dPlayer, BlockMenuModify menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS, Registry.MATERIAL.stream()
                .filter(Material::isSolid)
                .toList());

        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> menu.returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));

        super.register(Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, clickType) -> super.searchFor())
                .clickSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))));

        this.menu = menu;
    }

    @Override
    public Slot createSlot(int index, Material item) {
        Display display = Display.of(builder -> builder
                .material(item)
                .displayName(MiniMessage.miniMessage().deserialize(FormatUtil.toName(item.toString())))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select particle")
                )))
        );
        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> {
                    menu.setBreakParticle(item);
                    menu.populateParticle();
                    super.returnToPreviousMenu();
                }));
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> rotatePrevious(slot, clickType, 9)));
    }

    @Override
    public String getString(Material item) {
        return item.toString();
    }
}

class SetToolType extends ModularMenu<ToolType> {

    private static final MenuSize SIZE = MenuSize.FOUR;
    private static final Component TITLE = Component.text("Tool Types");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
    );

    private final Set<ToolType> toolTypes;

    public SetToolType(DPlayer dPlayer, BlockMenuModify menu, Set<ToolType> toolTypes) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                Arrays.stream(ToolType.values())
                .toList());
        this.toolTypes = toolTypes;

        super.register(Slot.of(31, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> {
                    menu.populateCorrectTools();
                    returnToPreviousMenu();
                })
                .uniqueSlot(UniqueSlot.RETURN)));

        populateModular();
    }

    @Override
    protected Slot createSlot(int index, ToolType toolType) {
        boolean contains = this.toolTypes.contains(toolType);
        Display display = Display.of(builder -> builder
                .material(contains ? Material.OAK_SIGN : Material.DARK_OAK_SIGN)
                .displayName(MiniMessage.miniMessage().deserialize(FormatUtil.toName(toolType.toString())))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize(contains ?"<!italic><yellow><bold>SELECTED" : "<!italic><red><bold>NOT SELECTED")
                )))
        );

        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> {
                    if(toolTypes.contains(toolType)) this.toolTypes.remove(toolType);
                    else this.toolTypes.add(toolType);
                    getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER), false);
                    refresh();
                }));
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(33, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(27, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> rotatePrevious(slot, clickType, 9)));
    }
}

class SetLootpool extends ModularMenu<ItemDrop> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Destructible Lootpool");

    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    );

    public SetLootpool(DPlayer dPlayer, List<ItemDrop> itemDrops) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                itemDrops == null ? List.of() : itemDrops.stream()
                        .sorted((Comparator.comparingDouble(ItemDrop::getChance).reversed()))
                        .toList());

        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.BROWN_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Make lootpool modifications to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the selected destructible block")
                ))))));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));

        populateModular();
    }

    public Slot createSlot(int index, ItemDrop itemDrop) {
        DItem dItem = itemDrop.getDItem();
        Display display = Display.of(builder -> builder
                .material(dItem.getMaterial())
                .displayName(Component.text(dItem.getId()))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Amount <white>" + itemDrop.getMinAmount() + " - " + itemDrop.getMaxAmount()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                FormatUtil.decimalTruncation(itemDrop.getChance() * 100, 17) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(1 / itemDrop.getChance())) + ")"))))
        );
        return Slot.of(index, builder -> builder
                .display(display)
        );
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> rotatePrevious(slot, clickType, 9)));
    }
}