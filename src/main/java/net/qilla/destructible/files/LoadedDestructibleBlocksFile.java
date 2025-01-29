package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import org.bukkit.Bukkit;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedDestructibleBlocksFile extends DestructibleFile {

    private static LoadedDestructibleBlocksFile INSTANCE;
    private final static String DEFAULT_RESOURCE = "loaded_destructible_blocks_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "localdb" + File.separator + "loaded_destructible_blocks.json");
    private static final Type TYPE = new TypeToken<Map<Long, Map<Integer, String>>>() {}.getType();
    private static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();

    public static LoadedDestructibleBlocksFile getInstance() {
        if(INSTANCE == null) INSTANCE = new LoadedDestructibleBlocksFile();
        return INSTANCE;
    }

    private LoadedDestructibleBlocksFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
    }

    @Override
    public void save() {
        try(BufferedWriter writer = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            var registry = DRegistry.LOADED_BLOCKS;

            writer.write(GSON.toJson(registry, TYPE));
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving Destructible loaded blocks(GROUPED)!\n" + exception);
        }
    }

    @Override
    public void load() {
        this.clear();
        try(BufferedReader reader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            Map<Long, Map<Integer, String>> map = GSON.fromJson(reader, TYPE);
            var registry = DRegistry.LOADED_BLOCKS;

            registry.putAll(map);
        } catch(IOException | JsonSyntaxException e) {
            super.reset();
            Bukkit.getLogger().severe("Error loading \"" + this.newPath + "\". File has been reset!\n" + e.getMessage());
        }
    }

    @Override
    public void clear() {
        DRegistry.LOADED_BLOCKS.clear();
    }
}