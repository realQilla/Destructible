package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryMaster;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.typeadapters.DItemTA;
import org.bukkit.Bukkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CustomItemsFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "custom_items_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "custom_items.json");
    private final Type type = new TypeToken<List<DItem>>() {
    }.getType();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DItem.class, new DItemTA())
            .setPrettyPrinting()
            .create();

    public CustomItemsFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
    }

    @Override
    public void save() {
        try(BufferedWriter writer = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            var registry = DRegistry.ITEMS;
            List<DItem> list = registry.values().stream().toList();

            writer.write(gson.toJson(list, type));
        } catch(IOException e) {
            Bukkit.getLogger().severe("Error saving custom items!\n" + e.getMessage());
        }
    }

    @Override
    public void load() {
        this.clear();
        try(BufferedReader reader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            List<DItem> list = gson.fromJson(reader, type);
            var registry = DRegistry.ITEMS;

            list.forEach(item -> registry.put(item.getId(), item));
        } catch(IOException | JsonSyntaxException e) {
            super.reset();
            Bukkit.getLogger().severe("Error loading \"" + this.newPath + "\". File has been reset!\n" + e.getMessage());
        }
    }

    @Override
    public void clear() {
        DRegistry.ITEMS.clear();
    }
}