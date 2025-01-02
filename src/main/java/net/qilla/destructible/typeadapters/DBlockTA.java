package net.qilla.destructible.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBlockTA extends TypeAdapter<DBlock> {

    @Override
    public void write(JsonWriter out, DBlock value) throws IOException {
        out.beginObject();
        out.name("id").value(value.getId());
        out.name("material").value(Registry.MATERIAL.getKey(value.getMaterial()).value());
        out.name("strength").value(value.getStrength());
        out.name("durability").value(value.getDurability());
        out.name("msCooldown").value(value.getMsCooldown());
        out.name("properTools");
        out.beginArray();
        for(ToolType tool : value.getProperTools()) {
            out.value(tool.toString());
        }
        out.endArray();
        out.name("itemDrops");
        out.beginArray();
        for(DDrop drop : value.getItemDrops()) {
            out.beginObject();
            out.name("dItem").value(drop.getDItem().getId());
            out.name("minAmount").value(drop.getMinAmount());
            out.name("maxAmount").value(drop.getMaxAmount());
            out.name("dropChance").value(drop.getDropChance());
            out.endObject();
        }
        out.endArray();
        out.name("sound").value(Registry.SOUNDS.getKey(value.getSound()).value());
        out.name("particle").value(Registry.MATERIAL.getKey(value.getParticle()).value());
        out.endObject();
    }

    @Override
    public DBlock read(JsonReader in) throws IOException {
        DBlock.Builder builder = new DBlock.Builder();
        in.beginObject();
        while(in.hasNext()) {
            switch(in.nextName()) {
                case "id":
                    builder.id(in.nextString());
                    break;
                case "material":
                    builder.material(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "strength":
                    builder.strengthRequirement(in.nextInt());
                    break;
                case "durability":
                    builder.durability(in.nextInt());
                    break;
                case "msCooldown":
                    builder.msCooldown(in.nextInt());
                    break;
                case "properTools":
                    List<ToolType> tools = new ArrayList<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        tools.add(ToolType.valueOf(in.nextString()));
                    }
                    in.endArray();
                    builder.properTools(tools);
                    break;
                case "itemDrops":
                    List<DDrop> dDrops = new ArrayList<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        in.beginObject();
                        DDrop.Builder dropBuilder = new DDrop.Builder();
                        while(in.hasNext()) {
                            switch(in.nextName()) {
                                case "dItem":
                                    dropBuilder.dItem(in.nextString());
                                    break;
                                case "minAmount":
                                    dropBuilder.minAmount(in.nextInt());
                                    break;
                                case "maxAmount":
                                    dropBuilder.maxAmount(in.nextInt());
                                    break;
                                case "dropChance":
                                    dropBuilder.dropChance(in.nextDouble());
                                    break;
                            }
                        }
                        dDrops.add(dropBuilder.build());
                        in.endObject();
                        builder.itemDrops(dDrops);
                    }
                    in.endArray();
                    break;
                case "sound":
                    builder.sound(Registry.SOUNDS.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "particle":
                    builder.particle(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
            }
        }
        in.endObject();
        return builder.build();
    }

}