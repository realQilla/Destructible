package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
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

public class LoadedCachedCustomBlocksFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "loaded_cached_custom_blocks_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "localdb" + File.separator + "loaded_cached_custom_blocks.json");
    private final Gson gson;
    private final Type type;

    public LoadedCachedCustomBlocksFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
        this.gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .create();
        this.type = new TypeToken<DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>>>() {}.getType();
    }

    @Override
    public void save() {
        String jsonString = this.gson.toJson(Registries.DESTRUCTIBLE_BLOCKS_CACHE);

        try(BufferedWriter bufferedWriter = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonString);
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving the custom block cache!\n" + exception);
        }
    }

    @Override
    public void load() {
        try(BufferedReader bufferedReader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>> registry = this.gson.fromJson(bufferedReader, type);
            Registries.DESTRUCTIBLE_BLOCKS_CACHE.putAll(registry);
        } catch(IOException | JsonSyntaxException exception) {
            super.reset();
        }
    }
}