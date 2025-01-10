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

    private boolean fullMenu = false;

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

    public BlockMenuModify(DPlayer dPlayer, DBlock dBlock) {
        super(dPlayer, SIZE, TITLE);

        this.dBlock = dBlock;

        this.populateMenu();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Displays.BLOCK_MODIFICATION_MENU));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> returnToPreviousMenu())));

        if(this.dBlock == null) {
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
                    .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
            ), 5, 10);
        } else {
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

            super.register(materialSlot());
            populateSettings();
        }
    }

    private void populateSettings() {
        List<Slot> list = new ArrayList<>();
        list.add(idSlot());
        list.add(materialSlot());
        list.add(strengthSlot());
        list.add(durabilitySlot());
        list.add(lootpoolSlot());
        list.add(correctToolsSlot());
        list.add(cooldownSlot());
        list.add(particleSlot());
        list.add(soundSlot());

        Collections.shuffle(list);
        super.register(list, 1, 2);
        super.register(buildSlot(), 20);
    }

    private void clickMainBlock(Slot slot, InventoryClickEvent event) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            Material cursorMaterial = event.getCursor().getType();
            if(!cursorMaterial.isSolid()) return;

            this.material = cursorMaterial;
            getDPlayer().getCraftPlayer().setItemOnCursor(null);
            super.register(materialSlot());
            unlockMenu();

        } else if(clickType.isRightClick()) {
            new SetMaterial(super.getDPlayer(), this).openInventory(true);
        }
    }

    private void unlockMenu() {
        if(!this.fullMenu) {
            this.populateSettings();
            this.fullMenu = true;
        }
    }

    public Slot buildSlot() {
        return Slot.of(31, builder -> builder
                .display(Displays.CONFIRM)
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) confirm();
                })
                .appearSound(SoundSettings.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 0, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
    }

    private void confirm() {
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

        if (this.dBlock != null) {
            Registries.DESTRUCTIBLE_BLOCKS.remove(this.dBlock.getId());
        }
        Registries.DESTRUCTIBLE_BLOCKS.put(dBlock.getId(), dBlock);
        getDPlayer().getPlugin().getCustomBlocksFile().save();
        getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_YES, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dBlock.getId() + " has been successfully registered."));
        super.returnToPreviousMenu();
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
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
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
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to changeh")
                                )))
                        ))
                        .action((event, slot) -> setID())
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
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
                .action((event, slot) -> setDurability())
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
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
                .action((event, slot) -> setStrength())
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))

        );
    }

    protected Slot lootpoolSlot() {
        if(this.lootpool == null) this.lootpool = List.of();

        return Slot.of(38, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.PINK_BUNDLE)
                                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                                .lore(ItemLore.lore(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                                )))
                        ))
                        .action((event, slot) -> new SetLootpool(getDPlayer(), this, this.lootpool).openInventory(true))
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
    }

    protected Slot correctToolsSlot() {
        if(this.correctTools == null) this.correctTools = new HashSet<>();

        StringBuilder stringBuilder = new StringBuilder();
        if(correctTools.isEmpty()) stringBuilder.append("<!italic><red>None");
        else stringBuilder.append("<!italic><white>").append(FormatUtil.toNameList(this.correctTools.stream().toList()));

        return Slot.of(42, builder -> builder
                        .display(Display.of(builder2 -> builder2
                                .material(Material.BLACK_BUNDLE)
                                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Correct Tools"))
                                .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value:"),
                                        MiniMessage.miniMessage().deserialize("<!italic><white>" + stringBuilder),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to change")
                                )))
                        ))
                        .action((Slot, event) -> new SetToolType(getDPlayer(), this, this.correctTools).openInventory(true))
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
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
                        .action((event, slot) -> setCooldown())
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
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
                        .action((event, slot) -> new SetParticle(getDPlayer(), this).openInventory(true))
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
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
                        .action((slot, event) -> new SetSound(getDPlayer(), this).openInventory(true))
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
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
                        super.register(this.idSlot());
                    }
                }
                super.openInventory(false);
            });
        });
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
                    super.register(this.strengthSlot());
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
                    super.register(this.durabilitySlot());
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
                    super.register(this.cooldownSlot());
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
        this.register(materialSlot());
        unlockMenu();
    }

    public void setLootpool(List<ItemDrop> lootpool) {
        this.lootpool = lootpool;
        this.register(lootpoolSlot());
    }

    public void setCorrectTools(Set<ToolType> correctTools) {
        this.correctTools = correctTools;
        this.register(correctToolsSlot());
    }

    public void setBreakParticle(Material material) {
        this.breakParticle = material;
        this.register(particleSlot());
    }

    public void setBreakSound(Sound breakSound) {
        this.breakSound = breakSound;
        this.register(soundSlot());
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

        this.menu = menu;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.COARSE_DIRT)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Search"))
        )));

        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> menu.returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));
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
                    this.menu.setMaterial(item);
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
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }

    @Override
    public String getString(Material item) {
        return item.toString();
    }

    @Override
    protected Slot getSearchSlot() {
        return Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> super.searchFor())
        );
    }

    @Override
    protected Slot getResetSearchSlot() {
        return Slot.of(45, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.BARRIER)
                        .displayName(MiniMessage.miniMessage().deserialize("<red>Reset Search"))
                        .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Resets your currently searched term")
                        )))
                ))
                .uniqueSlot(UniqueSlot.RESET_SEARCH)
                .action((slot, event) -> resetItemPopulation())
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
                .clickSound(SoundSettings.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
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

        this.menu = menu;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.NAUTILUS_SHELL)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Sound Search"))
        )));

        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> menu.returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));
        super.register(Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> super.searchFor())
                .clickSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))));
    }

    @Override
    public Slot createSlot(int index, Sound item) {
        Display display = Display.of(builder -> builder
                .material(Material.MUSIC_DISC_RELIC)
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
                        this.menu.setBreakSound(item);
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
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }

    @Override
    public String getString(Sound item) {
        return item.toString();
    }

    @Override
    protected Slot getSearchSlot() {
        return Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> super.searchFor())
                .clickSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER)));
    }

    @Override
    protected Slot getResetSearchSlot() {
        return Slot.of(45, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.BARRIER)
                        .displayName(MiniMessage.miniMessage().deserialize("<red>Reset Search"))
                        .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Resets your currently searched term")
                        )))
                ))
                .uniqueSlot(UniqueSlot.RESET_SEARCH)
                .action((slot, event) -> resetItemPopulation())
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
                .clickSound(SoundSettings.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
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
                .filter(Material::isBlock)
                .toList());

        this.menu = menu;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.HEART_OF_THE_SEA)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Particle Search"))
        )));

        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> menu.returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));

        super.register(Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> super.searchFor())
                .clickSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))));
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
                    this.menu.setBreakParticle(item);
                    this.menu.particleSlot();
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
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }

    @Override
    public String getString(Material item) {
        return item.toString();
    }

    @Override
    protected Slot getSearchSlot() {
        return Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> super.searchFor())
                .clickSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER)));
    }

    @Override
    protected Slot getResetSearchSlot() {
        return Slot.of(45, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.BARRIER)
                        .displayName(MiniMessage.miniMessage().deserialize("<red>Reset Search"))
                        .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Resets your currently searched term")
                        )))
                ))
                .uniqueSlot(UniqueSlot.RESET_SEARCH)
                .action((slot, event) -> resetItemPopulation())
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
                .clickSound(SoundSettings.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
    }
}

class SetToolType extends ModularMenu<ToolType> {

    private static final MenuSize SIZE = MenuSize.FOUR;
    private static final Component TITLE = Component.text("Tool Types");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
    );

    private final BlockMenuModify menu;
    private final Set<ToolType> toolTypes;

    public SetToolType(DPlayer dPlayer, BlockMenuModify menu, Set<ToolType> toolTypes) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                Arrays.stream(ToolType.values())
                .toList());

        this.menu = menu;
        this.toolTypes = toolTypes;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.BLACK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Tool Types"))
        )));
        super.register(Slot.of(31, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> {
                    super.returnToPreviousMenu();
                })
                .uniqueSlot(UniqueSlot.RETURN)));
        super.register(Slot.of(33, builder -> builder
                .display(Displays.CONFIRM)
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        this.menu.setCorrectTools(this.toolTypes);
                        this.menu.returnToPreviousMenu();
                    }
                })));

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
                        MiniMessage.miniMessage().deserialize(contains ? "<!italic><yellow><bold>SELECTED" : "<!italic><red><bold>NOT SELECTED")
                )))
        );

        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> {
                    if(toolTypes.contains(toolType)) this.toolTypes.remove(toolType);
                    else this.toolTypes.add(toolType);
                    getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER), false);
                    refreshModular();
                }));
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(34, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(27, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
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

    private final BlockMenuModify menu;

    public SetLootpool(DPlayer dPlayer, BlockMenuModify menu, List<ItemDrop> itemDrops) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                itemDrops == null ? List.of() : new ArrayList<>(itemDrops.stream()
                        .sorted((Comparator.comparingDouble(ItemDrop::getChance).reversed()))
                        .toList()));

        this.menu = menu;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Make lootpool modifications to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the selected destructible block")
                ))))));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));
        super.register(Slot.of(51, builder -> builder
                .display(Displays.CONFIRM)
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        this.menu.setLootpool(super.getItemPopulation());
                        this.menu.returnToPreviousMenu();
                        getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_YES, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
                    }
                })));
        super.register(Slot.of(47, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.LIME_BUNDLE)
                        .displayName(MiniMessage.miniMessage().deserialize("<green>Add new item drop"))
                ))
                .action((slot, event) -> {
                    new ModifyItemDrop(getDPlayer(), this).openInventory(true);
                })));

        populateModular();
    }

    public Slot createSlot(int index, ItemDrop item) {
        DItem dItem = item.getDItem();
        Display display = Display.of(builder -> builder
                .material(dItem.getMaterial())
                .displayName(Component.text(dItem.getId()))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Amount <white>" + item.getMinAmount() + " - " + item.getMaxAmount()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                FormatUtil.decimalTruncation(item.getChance() * 100, 17) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(1 / item.getChance())) + ")"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click to modify"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to remove"))
                ))
        );
        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        new ModifyItemDrop(getDPlayer(), this, item).openInventory(true);
                    } else if(clickType.isRightClick()) {
                        this.removeItemDrop(item);
                    }
                })
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
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }

    public void addItemDrop(ItemDrop itemDrop) {
        super.getItemPopulation().add(itemDrop);
        this.refreshModular();
    }

    public void removeItemDrop(ItemDrop itemDrop) {
        super.getItemPopulation().remove(itemDrop);
        this.refreshModular();
    }
}

class ModifyItemDrop extends DestructibleMenu {

    private static final MenuSize SIZE = MenuSize.FIVE;
    private static final Component TITLE = Component.text("Destructible Lootpool");

    private final SetLootpool menu;
    private boolean fullMenu;

    private final ItemDrop itemDrop;
    private DItem dItem;
    private Integer minAmount;
    private Integer maxAmount;
    private Double chance;

    public ModifyItemDrop(DPlayer dPlayer, SetLootpool menu, ItemDrop itemDrop) {
        super(dPlayer, SIZE, TITLE);
        Preconditions.checkNotNull(itemDrop, "ItemDrop cannot be null");

        this.menu = menu;
        this.itemDrop = itemDrop;
        this.dItem = itemDrop.getDItem();
        this.minAmount = itemDrop.getMinAmount();
        this.maxAmount = itemDrop.getMaxAmount();
        this.chance = itemDrop.getChance() * 100;

        this.fullMenu = true;

        this.populateMenu();
    }

    public ModifyItemDrop(DPlayer dPlayer, SetLootpool menu) {
        super(dPlayer, SIZE, TITLE);

        this.menu = menu;
        this.itemDrop = null;

        this.populateMenu();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Make lootpool modifications to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the selected destructible block")
                ))))));
        super.register(Slot.of(40, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));

        if(!this.fullMenu) {
            super.register(Slot.of(22, builder -> builder
                    .display(Display.of(builder2 -> builder2
                            .material(Material.HOPPER_MINECART)
                            .displayName(MiniMessage.miniMessage().deserialize("<gray>Click with block"))
                            .lore(ItemLore.lore(List.of(
                                    Component.empty(),
                                    MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to search for a material")
                            )))
                    ))
                    .action(((slot, event) -> new SetDItem(getDPlayer(), this).openInventory(false)))
                    .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
            ), 3, 7);
        } else {
            super.register(this.itemSlot(), 8, 15);
            populateSettings();
        }
    }

    private void populateSettings() {
        List<Slot> list = new ArrayList<>();
        list.add(this.amountSlot());
        list.add(this.chanceSlot());
        super.register(this.buildSlot(), 15);

        Collections.shuffle(list);

        super.register(list, 3, 5);
    }

    private Slot buildSlot() {
        return Slot.of(42, builder -> builder
                .display(Displays.CONFIRM)
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        this.menu.removeItemDrop(this.itemDrop);
                        this.menu.addItemDrop(new ItemDrop.Builder()
                                .dItem(this.dItem)
                                .minAmount(this.minAmount)
                                .maxAmount(Math.max(this.minAmount, this.maxAmount))
                                .chance(this.chance / 100)
                                .build());
                        this.menu.returnToPreviousMenu();
                        getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_YES, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
                    }
                }));
    }

    private Slot itemSlot() {
        return Slot.of(22, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(dItem.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize("<gold>Drop amount"))
                        .lore(ItemLore.lore()
                                .addLines(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(dItem.getDisplayName()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                                ))
                                .addLines(dItem.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(dItem.getRarity().getComponent())
                                )).build()
                        )
                ))
                .action(((slot, event) -> {
                    new SetDItem(getDPlayer(), this).openInventory(false);
                }))
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
    }

    private Slot amountSlot() {
        if(minAmount == null) this.minAmount = 1;
        if(maxAmount == null) this.maxAmount = 1;
        String string = "<!italic><gray>Drop amount: <white>" + this.minAmount + " <white>- " + this.maxAmount;

        return Slot.of(20, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.BROWN_BUNDLE)
                        .displayName(MiniMessage.miniMessage().deserialize("<gold>Drop amount"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize(string),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to set a minimum amount"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to set a maximum amount")
                        )))
                ))
                .action(((slot, event) -> {
                    ClickType clickType = event.getClick();

                    if(clickType.isLeftClick()) setMinAmount();
                    else if(clickType.isRightClick()) setMaxAmount();
                }))
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
    }

    private Slot chanceSlot() {
        if(chance == null) this.chance = 100.0;
        String string = (FormatUtil.decimalTruncation(this.chance, 17)) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(100 / this.chance)) + ")";

        return Slot.of(24, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.ENCHANTED_BOOK)
                        .displayName(MiniMessage.miniMessage().deserialize("<aqua>Drop Chance"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop chance: <white>" + string + ")"),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to set a drop chance")
                        )))
                ))
                .action(((slot, event) -> {
                    setChance();
                }))
                .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
        );
    }

    private void setMinAmount() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Minimum amount",
                "that can drop");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                try {
                    this.minAmount = Math.max(1, Integer.parseInt(result));
                    super.register(this.amountSlot());
                    getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
                } catch(NumberFormatException ignore) {
                }
                super.openInventory(false);
            });
        });
    }

    private void setMaxAmount() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Maximum amount",
                "that can drop");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                try {
                    this.maxAmount = Math.max(1, Integer.parseInt(result));
                    super.register(this.amountSlot());
                    getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
                } catch(NumberFormatException ignore) {
                }
                super.openInventory(false);
            });
        });
    }

    private void setChance() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Chance that this",
                "item can drop");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                try {
                    this.chance = Math.max(0, Math.min(100, Double.parseDouble(result)));
                    super.register(this.chanceSlot());
                    getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
                } catch(NumberFormatException ignore) {
                }
                super.openInventory(false);
            });
        });
    }

    public void setDItem(DItem dItem) {
        this.dItem = dItem;
        this.register(itemSlot());
        if(!fullMenu) {
            populateSettings();
            fullMenu = true;
        }
    }
}

class SetDItem extends ModularMenu<DItem> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Destructible Lootpool");

    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    );

    private final ModifyItemDrop menu;

    public SetDItem(DPlayer dPlayer, ModifyItemDrop menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
        Registries.DESTRUCTIBLE_ITEMS.values().stream().toList());

        this.menu = menu;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Make lootpool modifications to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the selected destructible block")
                ))))));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));
    }

    public Slot createSlot(int index, DItem item) {
        Display display = Display.of(builder -> builder
                .material(item.getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize(item.getId()))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(item.getDisplayName()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                        ))
                        .addLines(item.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(item.getRarity().getComponent())
                        )).build()
                )
        );
        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> {
                    this.menu.setDItem(item);
                    this.menu.openInventory(false);
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
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }
}