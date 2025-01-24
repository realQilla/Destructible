package net.qilla.destructible.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.mining.item.attributes.Attribute;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.IOException;
import java.util.*;

public class DItemTA extends TypeAdapter<DItem> {
    @Override
    public void write(JsonWriter out, DItem value) throws IOException {
        out.beginObject();

        out.name("ID").value(value.getId());
        out.name("LAST_UPDATE").value(value.getVersion());
        out.name("MATERIAL").value(value.getMaterial().key().value());
        out.name("NAME").value(MiniMessage.miniMessage().serialize(value.getDisplayName()));
        out.name("LORE").beginArray();
        for(Component component : value.getLore().lines()) {
            out.value(MiniMessage.miniMessage().serialize(component));
        }
        out.endArray();
        out.name("RARITY").value(value.getRarity().toString());
        out.name("STACK_SIZE").value(value.getStackSize());
        out.name("RESOURCE").value(value.isResource());
        out.name("STATIC_ATTRIBUTES").beginArray();
        for(Attribute<?> attribute : value.getStaticAttributes().getAll()) {
            out.value(Attribute.encode(attribute));
        }
        out.endArray();

        out.endObject();
    }

    @Override
    public DItem read(JsonReader in) throws IOException {
        DItem.Builder builder = DItem.builder();
        in.beginObject();
        while(in.hasNext()) {
            switch(in.nextName()) {
                case "ID":
                    builder.id(in.nextString());
                    break;
                case "LAST_UPDATE":
                    builder.version(in.nextLong());
                    break;
                case "MATERIAL":
                    builder.material(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "NAME":
                    builder.displayName(MiniMessage.miniMessage().deserialize(in.nextString()));
                    break;
                case "LORE":
                    List<Component> componentList = new ArrayList<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        componentList.add(MiniMessage.miniMessage().deserialize(in.nextString()));
                    }
                    in.endArray();
                    builder.lore(ItemLore.lore(componentList));
                    break;
                case "RARITY":
                    builder.rarity(Rarity.valueOf(in.nextString()));
                    break;
                case "STACK_SIZE":
                    builder.stackSize(in.nextInt());
                    break;
                case "RESOURCE":
                    builder.resource(in.nextBoolean());
                    break;
                case "STATIC_ATTRIBUTES":
                    Set<Attribute<?>> staticAttributes = new HashSet<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        staticAttributes.add(Attribute.decode(in.nextString()));
                    }
                    in.endArray();
                    builder.staticAttributes(staticAttributes);
                    break;
            }
        }
        in.endObject();

        return builder.build();
    }
}