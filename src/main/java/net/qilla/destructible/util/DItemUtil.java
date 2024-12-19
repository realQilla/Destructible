package net.qilla.destructible.util;

import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DTools;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class DItemUtil {
    @NotNull
    public static DTool getDTool(final ItemStack item) {
        if(item.hasItemMeta()) {
            DTool dTool = Registries.TOOLS.get(item.getItemMeta().getPersistentDataContainer().get(DataKey.TOOL, PersistentDataType.STRING));
            if(dTool != null) return dTool;
        }
        return DTools.DEFAULT;
    }
}