package net.qilla.destructible.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DToolTA extends TypeAdapter<DTool> {
    @Override
    public void write(JsonWriter out, DTool value) throws IOException {
        out.beginObject();
        out.name("id").value(value.getId());
        out.name("material").value(Registry.MATERIAL.getKey(value.getMaterial()).value());
        out.name("displayName").value(MiniMessage.miniMessage().serialize(value.getDisplayName()));
        out.name("lore").beginArray();
        for (Component component : value.getLore().lines()) {
            out.value(MiniMessage.miniMessage().serialize(component));
        }
        out.endArray();
        out.name("stackSize").value(value.getStackSize());
        out.name("rarity").value(value.getRarity().name());
        out.name("toolType");
        out.beginArray();
        for (ToolType toolType : value.getToolType()) {
            out.value(toolType.toString());
        }
        out.endArray();
        out.name("strength").value(value.getStrength());
        out.name("efficiency").value(value.getEfficiency());
        out.name("durability").value(value.getDurability());
        out.endObject();
    }

    @Override
    public DTool read(JsonReader in) throws IOException {
        DItem.Builder builder = new DItem.Builder();
        DTool.Builder toolBuilder = new DTool.Builder();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    builder.id(in.nextString());
                    break;
                case "material":
                    builder.material(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "displayName":
                    builder.displayName(MiniMessage.miniMessage().deserialize(in.nextString()));
                    break;
                case "lore":
                    ItemLore.Builder lore = ItemLore.lore();
                    in.beginArray();
                    while (in.hasNext()) {
                        lore.addLine(MiniMessage.miniMessage().deserialize(in.nextString()));
                    }
                    builder.lore(lore.build());
                    in.endArray();
                    break;
                case "stackSize":
                    builder.stackSize(in.nextInt());
                    break;
                case "rarity":
                    builder.rarity(Rarity.valueOf(in.nextString()));
                    break;
                case "toolType":
                    List<ToolType> toolTypes = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        toolTypes.add(ToolType.valueOf(in.nextString()));
                    }
                    toolBuilder.dToolType(toolTypes);
                    in.endArray();
                    break;
                case "strength":
                    toolBuilder.strength(in.nextInt());
                    break;
                case "efficiency":
                    toolBuilder.efficiency(in.nextDouble());
                    break;
                case "durability":
                    toolBuilder.durability(in.nextInt());
                    break;
            }
        }
        toolBuilder.dItem(builder);
        in.endObject();
        return toolBuilder.build();
    }
}
