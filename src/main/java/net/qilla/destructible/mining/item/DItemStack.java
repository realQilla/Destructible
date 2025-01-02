package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.Registries;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DItemStack extends ItemStack {

    private final DItem dItem;

    private DItemStack(DItem dItem, int amount) {
        super(ItemStack.of(dItem.getMaterial(), amount));
        this.dItem = dItem;
        create();
    }

    @NotNull
    public static DItemStack of(DItem dItem) {
        Preconditions.checkArgument(dItem != null, "DItem cannot be null");
        return new DItemStack(dItem, 1);
    }

    @NotNull
    public static DItemStack of(DItem dItem, int amount) {
        Preconditions.checkArgument(dItem != null, "DItem cannot be null");
        return new DItemStack(dItem, amount);
    }

    @NotNull
    public static DItemStack of(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "ItemStack cannot be null");
        return of(Registries.DESTRUCTIBLE_ITEMS.get(itemStack.getItemMeta().getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING)), itemStack.getAmount());
    }

    @NotNull
    public static DItemStack of(String id) {
        Preconditions.checkArgument(id != null, "ID cannot be null");
        return of(Registries.DESTRUCTIBLE_ITEMS.get(id));
    }

    public static boolean isDItem(ItemStack itemStack) {
        if(itemStack == null) return false;
        String id = itemStack.getItemMeta().getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING);
        return id != null && Registries.DESTRUCTIBLE_ITEMS.containsKey(id);
    }

    @Nullable
    public static DItem getDItem(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "ItemStack is not a DItem");
        String id = itemStack.getItemMeta().getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING);
        if(id == null) return null;
        return Registries.DESTRUCTIBLE_ITEMS.get(id);
    }

    private void create() {
        this.editMeta(meta -> {
            meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING, dItem.getId());
        });

        this.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        this.setData(DataComponentTypes.ITEM_NAME, dItem.getDisplayName());
        this.setData(DataComponentTypes.LORE, createLore());
        this.setData(DataComponentTypes.MAX_STACK_SIZE, dItem.getStackSize());

        if(this.dItem instanceof DTool dTool) {
            if(dTool.getDurability() != -1)
                this.editMeta(meta -> {
                    meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, dTool.getDurability());
                });
            this.setData(DataComponentTypes.MAX_DAMAGE, dTool.getDurability());
        }
    }

    @NotNull
    private ItemLore createLore() {
        ItemLore.Builder lore = ItemLore.lore();
        lore.addLines(dItem.getLore());

        if(this.dItem instanceof DTool dTool) {
            lore.addLines(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency " + dTool.getEfficiency()),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Strength " + dTool.getStrength())
            ));
        }

        lore.addLines(List.of(
                Component.empty(),
                dItem.getRarity().getComponent()
        ));
        return lore.build();
    }

    @NotNull
    public DItem getDItem() {
        return this.dItem;
    }

    @Override
    public @NotNull DItemStack clone() {
        return of(this.dItem, this.getAmount());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DItemStack dItemStack)) return false;
        return super.equals(obj) && dItem.getId().equals(dItemStack.dItem.getId());
    }
}