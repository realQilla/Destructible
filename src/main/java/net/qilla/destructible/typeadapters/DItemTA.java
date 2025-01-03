package net.qilla.destructible.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.mining.item.DItem;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DItemTA extends TypeAdapter<DItem> {
    @Override
    public void write(JsonWriter out, DItem value) throws IOException {
        out.beginObject();
        out.name("ID").value(value.getId());
        out.name("ITEM_MATERIAL").value(Registry.MATERIAL.getKey(value.getMaterial()).value());
        out.name("ITEM_NAME").value(MiniMessage.miniMessage().serialize(value.getDisplayName()));
        out.name("ITEM_LORE").beginArray();
        for (Component component : value.getLore().lines()) {
            out.value(MiniMessage.miniMessage().serialize(component));
        }
        out.endArray();
        out.name("ITEM_STACK_SIZE").value(value.getStackSize());
        out.name("ITEM_RARITY").value(value.getRarity().toString());
        out.endObject();
    }

    @Override
    public DItem read(JsonReader in) throws IOException {
        DItem.Builder builder = new DItem.Builder();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "ID":
                    builder.id(in.nextString());
                    break;
                case "ITEM_MATERIAL":
                    builder.material(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "ITEM_NAME":
                    builder.displayName(MiniMessage.miniMessage().deserialize(in.nextString()));
                    break;
                case "ITEM_LORE":
                    ItemLore.Builder lore = ItemLore.lore();
                    in.beginArray();
                    while (in.hasNext()) {
                        lore.addLine(MiniMessage.miniMessage().deserialize(in.nextString()));
                    }
                    builder.lore(lore.build());
                    in.endArray();
                    break;
                case "ITEM_STACK_SIZE":
                    builder.stackSize(in.nextInt());
                    break;
                case "ITEM_RARITY":
                    builder.rarity(Rarity.valueOf(in.nextString()));
                    break;
            }
        }
        in.endObject();
        return builder.build();
    }
}
