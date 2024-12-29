package net.qilla.destructible.mining.block;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.DToolType;
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
        for(DToolType tool : value.getProperTools()) {
            out.value(tool.name());
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
        DBlock.Properties properties = DBlock.Properties.of();
        in.beginObject();
        while(in.hasNext()) {
            switch(in.nextName()) {
                case "id":
                    properties.id(in.nextString());
                    break;
                case "material":
                    properties.material(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "strength":
                    properties.strengthRequirement(in.nextInt());
                    break;
                case "durability":
                    properties.durability(in.nextInt());
                    break;
                case "msCooldown":
                    properties.msCooldown(in.nextInt());
                    break;
                case "properTools":
                    List<DToolType> tools = new ArrayList<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        tools.add(DToolType.valueOf(in.nextString()));
                    }
                    in.endArray();
                    properties.properTools(tools);
                    break;
                case "itemDrops":
                    List<DDrop> dDrops = new ArrayList<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        in.beginObject();
                        DDrop.Properties dropProperties = DDrop.Properties.of();
                        while(in.hasNext()) {
                            switch(in.nextName()) {
                                case "dItem":
                                    dropProperties.dItem(in.nextString());
                                    break;
                                case "minAmount":
                                    dropProperties.minAmount(in.nextInt());
                                    break;
                                case "maxAmount":
                                    dropProperties.maxAmount(in.nextInt());
                                    break;
                                case "dropChance":
                                    dropProperties.dropChance(in.nextDouble());
                                    break;
                            }
                        }
                        dDrops.add(DDrop.of(dropProperties));
                        in.endObject();
                        properties.itemDrops(dDrops);
                    }
                    in.endArray();
                    break;
                case "sound":
                    properties.sound(Registry.SOUNDS.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "particle":
                    properties.particle(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
            }
        }
        in.endObject();
        return new DBlock(properties);
    }

}