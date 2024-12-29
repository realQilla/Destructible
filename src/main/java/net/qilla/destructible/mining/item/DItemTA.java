package net.qilla.destructible.mining.item;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
        for (int i = 0; i < value.getLore().size(); i++) {
            out.value(MiniMessage.miniMessage().serialize(value.getLore().get(i)));
        }
        out.endArray();
        out.name("stackSize").value(value.getStackSize());
        out.name("rarity").value(value.getRarity().name());
        out.endObject();
    }

    @Override
    public DItem read(JsonReader in) throws IOException {
        DItem.Properties properties = DItem.Properties.of();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    properties.id(in.nextString());
                    break;
                case "material":
                    properties.material(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "displayName":
                    properties.displayName(MiniMessage.miniMessage().deserialize(in.nextString()));
                    break;
                case "lore":
                    in.beginArray();
                    List<Component> lore = new ArrayList<>();
                    while (in.hasNext()) {
                        lore.add(MiniMessage.miniMessage().deserialize(in.nextString()));
                    }
                    properties.lore(lore);
                    in.endArray();
                    break;
                case "stackSize":
                    properties.stackSize(in.nextInt());
                    break;
                case "rarity":
                    properties.rarity(Rarity.valueOf(in.nextString()));
                    break;
            }
        }
        in.endObject();
        return new DItem(properties);
    }
}
