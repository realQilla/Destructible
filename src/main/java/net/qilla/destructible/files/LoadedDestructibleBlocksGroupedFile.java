package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryKey;
import net.qilla.destructible.data.registry.DRegistryMaster;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedDestructibleBlocksGroupedFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "loaded_destructible_blocks_grouped_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "localdb" + File.separator + "loaded_destructible_blocks_grouped.json");
    private final Type type = new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap<Long, Set<Integer>>>>() {
    }.getType();
    private final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();

    public LoadedDestructibleBlocksGroupedFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
    }

    @Override
    public void save() {
        try(BufferedWriter writer = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            var map = DRegistry.LOADED_BLOCKS_GROUPED;

            writer.write(gson.toJson(map, type));
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving Destructible loaded blocks(GROUPED)!\n" + exception);
        }
    }

    @Override
    public void load() {
        this.clear();
        try(BufferedReader reader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            ConcurrentHashMap<String, ConcurrentHashMap<Long, Set<Integer>>> map = gson.fromJson(reader, type);
            var registry = DRegistry.LOADED_BLOCKS_GROUPED;

            registry.putAll(map);
        } catch(IOException | JsonSyntaxException e) {
            super.reset();
            Bukkit.getLogger().severe("Error loading \"" + this.newPath + "\". File has been reset!\n" + e.getMessage());
        }
    }

    @Override
    public void clear() {
        DRegistry.LOADED_BLOCKS_GROUPED.clear();
    }
}