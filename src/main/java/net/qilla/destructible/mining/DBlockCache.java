package net.qilla.destructible.mining;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.DestructibleRegistry;
import net.qilla.destructible.data.Registries;
import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class DBlockCache {

    private static final Logger LOGGER = Bukkit.getLogger();
    private final Destructible plugin;
    private final Gson gson;
    private final Path path;
    private final File file;
    private final Type type = new TypeToken<DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>>>(){}.getType();

    public DBlockCache(Destructible plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        this.path = Paths.get(plugin.getDataFolder() + File.separator + "dblock_cache.json");
        this.file = this.path.toFile();

        if(!java.nio.file.Files.exists(path)) reset();
    }

    public void save() {
        String jsonString = this.gson.toJson(Registries.DESTRUCTIBLE_BLOCKS_CACHE);

        try {
            BufferedWriter bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);
            bufferedWriter.write(jsonString);
            bufferedWriter.close();
        } catch(IOException exception) {
            LOGGER.severe("There was a problem saving the dblock cache!" + exception);
        }
    }

    public void load() {
        try {
            BufferedReader bufferedReader = Files.newReader(this.file, StandardCharsets.UTF_8);
            DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>> registry = this.gson.fromJson(bufferedReader, type);
            bufferedReader.close();

            Registries.DESTRUCTIBLE_BLOCKS_CACHE.putAll(registry);
        } catch(IOException exception) {
            LOGGER.severe("There was a problem loading the dblock cache!" + exception);
        }
    }

    public void reset() {
        try {
            if(this.file.exists()) this.file.delete();
            java.nio.file.Files.createDirectories(this.path.getParent());
            this.file.createNewFile();
            BufferedWriter bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);
            bufferedWriter.write("{}");
            bufferedWriter.close();

        } catch(IOException exception) {
            LOGGER.severe("There was a problem resetting the dblock cache!" + exception);
        }
    }
}
