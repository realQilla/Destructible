package net.qilla.destructible.mining.item.attributes;

import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * A set of predefined attributes that can be utilized by items.
 */

public class AttributeTypes {

    private AttributeTypes() {
    }

    /**
     * An item's total durability.
     */

    public static final AttributeType<Integer> ITEM_MAX_DURABILITY = of("item_max_durability", 0, Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, int.class);

    /**
     * The amount of durability that an item has lost.
     */

    public static final AttributeType<Integer> ITEM_DURABILITY_LOST = of("item_durability_lost", 0, Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, int.class);

    /**
     * The efficiency at which an item can break a block.
     */

    public static final AttributeType<Integer> MINING_EFFICIENCY = of("mining_efficiency", 0,  Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, int.class);

    /**
     * An items strength, this value is used by bocks to determine if they can be damaged/mined.
     */

    public static final AttributeType<Integer> MINING_STRENGTH = of("mining_strength", 0, Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, int.class);

    /**
     * A value used by blocks to determine bonus drops
     */

    public static final AttributeType<Integer> MINING_FORTUNE = of("mining_fortune", 0, Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, int.class);

    /**
     * The classification of a tool's type used by block to determine if it is the correct type to damage said block.
     */

    public static final AttributeType<ToolType> TOOL_TYPE = of("tool_type", null, Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, ToolType.class);

    private static <T> AttributeType<T> of(@NotNull String key, @Nullable T defaultValue, @NotNull Material material, @NotNull Type type) {
        AttributeType<T> attributeType = new AttributeType<>(key, defaultValue, material, type);
        DRegistry.ATTRIBUTES.put(key, attributeType);
        return attributeType;
    }
}