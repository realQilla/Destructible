package net.qilla.destructible.mining.item;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.qilla.destructible.typeadapters.ItemDataTA;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class ItemDataType implements PersistentDataType<String, ItemData> {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ItemData.class, new ItemDataTA())
            .create();
    private static final Type TYPE = new TypeToken<ItemData>() {
    }.getType();
    public static final PersistentDataType<String, ItemData> ITEM = new ItemDataType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ItemData> getComplexType() {
        return ItemData.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull ItemData complex, @NotNull PersistentDataAdapterContext context) {
        return GSON.toJson(complex, TYPE);
    }

    @Override
    public @NotNull ItemData fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return GSON.fromJson(primitive, TYPE);
    }
}