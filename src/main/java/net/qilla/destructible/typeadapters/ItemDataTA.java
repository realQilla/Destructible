package net.qilla.destructible.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.qilla.destructible.mining.item.ItemData;
import net.qilla.destructible.mining.item.attributes.Attribute;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ItemDataTA extends TypeAdapter<ItemData> {

    @Override
    public void write(JsonWriter out, ItemData value) throws IOException {
        out.beginObject();

        out.name("ITEM_ID").value(value.getItemID());
        out.name("VERSION").value(value.getVersion());
        out.name("ATTRIBUTES").beginArray();
        for(Attribute<?> attribute : value.getAttributes().getAll()) {
            out.value(Attribute.encode(attribute));
        }
        out.endArray();

        out.endObject();
    }

    @Override
    public ItemData read(JsonReader in) throws IOException {
        ItemData.Builder builder = ItemData.builder();

        in.beginObject();
        while(in.hasNext()) {
            switch(in.nextName()) {
                case "ITEM_ID":
                    builder.itemID(in.nextString());
                    break;
                case "VERSION":
                    builder.version(in.nextLong());
                    break;
                case "ATTRIBUTES":
                    Set<Attribute<?>> attributes = new HashSet<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        attributes.add(Attribute.decode(in.nextString()));
                    }
                    in.endArray();
                    builder.dynamicAttributes(attributes);
                    break;
            }
        }
        in.endObject();

        return builder.build();
    }
}