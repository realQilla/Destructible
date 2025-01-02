package net.qilla.destructible.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
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
        out.name("id").value(value.getId());
        out.name("material").value(Registry.MATERIAL.getKey(value.getMaterial()).value());
        out.name("displayName").value(MiniMessage.miniMessage().serialize(value.getDisplayName()));
        out.name("lore").beginArray();
        for (Component component : value.getLore()) {
            out.value(MiniMessage.miniMessage().serialize(component));
        }
        out.endArray();
        out.name("stackSize").value(value.getStackSize());
        out.name("rarity").value(value.getRarity().toString());
        out.endObject();
    }

    @Override
    public DItem read(JsonReader in) throws IOException {
        DItem.Builder builder = new DItem.Builder();
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
                    List<Component> lore = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        lore.add(MiniMessage.miniMessage().deserialize(in.nextString()));
                    }
                    builder.lore(lore);
                    in.endArray();
                    break;
                case "stackSize":
                    builder.stackSize(in.nextInt());
                    break;
                case "rarity":
                    builder.rarity(Rarity.valueOf(in.nextString()));
                    break;
            }
        }
        in.endObject();
        return builder.build();
    }
}
