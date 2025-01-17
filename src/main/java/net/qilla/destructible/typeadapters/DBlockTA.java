package net.qilla.destructible.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBlockTA extends TypeAdapter<DBlock> {

    @Override
    public void write(JsonWriter out, DBlock value) throws IOException {
        out.beginObject();
        out.name("ID").value(value.getId());
        out.name("BLOCK_MATERIAL").value(Registry.MATERIAL.getKey(value.getMaterial()).value());
        out.name("BLOCK_STRENGTH").value(value.getStrength());
        out.name("BLOCK_DURABILITY").value(value.getDurability());
        out.name("BLOCK_COOLDOWN").value(value.getCooldown());
        out.name("CORRECT_TOOLS");
        out.beginArray();
        for(ToolType tool : value.getCorrectTools()) {
            out.value(tool.toString());
        }
        out.endArray();
        out.name("BREAK_SOUND").value(Registry.SOUNDS.getKey(value.getBreakSound()).value());
        out.name("BREAK_PARTICLE").value(Registry.MATERIAL.getKey(value.getBreakParticle()).value());
        out.name("ITEM_DROPS");
        out.beginArray();
        for(ItemDrop drop : value.getLootpool()) {
            out.beginObject();
            out.name("DESTRUCTIBLE_ITEM").value(drop.getDItem().getId());
            out.name("MIN_AMOUNT").value(drop.getMinAmount());
            out.name("MAX_AMOUNT").value(drop.getMaxAmount());
            out.name("CHANCE").value(drop.getChance());
            out.endObject();
        }
        out.endArray();
        out.endObject();
    }

    @Override
    public DBlock read(JsonReader in) throws IOException {
        DBlock.Builder builder = new DBlock.Builder();
        in.beginObject();
        while(in.hasNext()) {
            switch(in.nextName()) {
                case "ID":
                    builder.id(in.nextString());
                    break;
                case "BLOCK_MATERIAL":
                    builder.material(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "BLOCK_STRENGTH":
                    builder.strength(in.nextInt());
                    break;
                case "BLOCK_DURABILITY":
                    builder.durability(in.nextInt());
                    break;
                case "BLOCK_COOLDOWN":
                    builder.cooldown(in.nextLong());
                    break;
                case "CORRECT_TOOLS":
                    Set<ToolType> tools = new HashSet<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        tools.add(ToolType.valueOf(in.nextString()));
                    }
                    in.endArray();
                    builder.correctTools(tools);
                    break;
                case "BREAK_SOUND":
                    builder.breakSound(Registry.SOUNDS.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "BREAK_PARTICLE":
                    builder.breakParticle(Registry.MATERIAL.get(NamespacedKey.fromString(in.nextString())));
                    break;
                case "ITEM_DROPS":
                    List<ItemDrop> itemDrops = new ArrayList<>();
                    in.beginArray();
                    while(in.hasNext()) {
                        in.beginObject();
                        ItemDrop.Builder dropBuilder = new ItemDrop.Builder();
                        while(in.hasNext()) {
                            switch(in.nextName()) {
                                case "DESTRUCTIBLE_ITEM":
                                    dropBuilder.dItem(in.nextString());
                                    break;
                                case "MIN_AMOUNT":
                                    dropBuilder.minAmount(in.nextInt());
                                    break;
                                case "MAX_AMOUNT":
                                    dropBuilder.maxAmount(in.nextInt());
                                    break;
                                case "CHANCE":
                                    dropBuilder.chance(in.nextDouble());
                                    break;
                            }
                        }
                        itemDrops.add(dropBuilder.build());
                        in.endObject();
                        builder.lootpool(itemDrops);
                    }
                    in.endArray();
                    break;
            }
        }
        in.endObject();
        return builder.build();
    }

}