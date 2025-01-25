package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemData;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.mining.item.attributes.AttributeContainer;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ComponentUtil {

    private ComponentUtil() {
    }

    public static @NotNull String cleanComponent(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static @NotNull Component getItemAmountAndType(@NotNull DItem dItem, int amount) {
        Preconditions.checkNotNull(dItem, "DItem cannot be null");
        String rawName = cleanComponent(dItem.getDisplayName());
        Style style = dItem.getDisplayName().style();

        return MiniMessage.miniMessage().deserialize("<white>" + amount + "x ").append(Component.text(StringUtil.pluralize(rawName, amount), style));
    }

    public static @NotNull Component getItemAmountAndType(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");

        Component itemName = itemStack.getData(DataComponentTypes.ITEM_NAME);
        return MiniMessage.miniMessage().deserialize("<white>" + itemStack.getAmount() + "x ").append(itemName);
    }

    public static @NotNull ItemLore getLore(@NotNull DItem item) {
        Preconditions.checkNotNull(item, "DItem cannot be null");

        ItemLore.Builder builder = ItemLore.lore();
        builder.addLines(item.getLore().lines());

        AttributeContainer atCon = item.getStaticAttributes();
        if(atCon.has(AttributeTypes.MINING_EFFICIENCY))
            builder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency " + atCon.getValue(AttributeTypes.MINING_EFFICIENCY)));
        if(atCon.has(AttributeTypes.MINING_STRENGTH))
            builder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Strength " + NumberUtil.romanNumeral(atCon.getValue(AttributeTypes.MINING_STRENGTH))));
        if(atCon.has(AttributeTypes.MINING_FORTUNE))
            builder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Fortune " + NumberUtil.romanNumeral(atCon.getValue(AttributeTypes.MINING_FORTUNE))));


        if(item.getRarity() != Rarity.NONE) {
            builder.addLines(List.of(
                    Component.empty(),
                    item.getRarity().getComponent())
            );
        }
        return builder.build();
    }

    public static @NotNull ItemLore getLore(@NotNull ItemData itemData) {
        Preconditions.checkNotNull(itemData, "ItemData cannot be null");
        DItem item = DUtil.getDItem(itemData.getItemID());

        return getLore(item);
    }
}