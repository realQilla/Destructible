package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.Registries;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedDestructibleBlocksGroupedFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "loaded_destructible_blocks_grouped_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "localdb" + File.separator + "loaded_destructible_blocks_grouped.json");
    private final Gson gson;
    private final Type type;

    public LoadedDestructibleBlocksGroupedFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
        this.gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .create();
        this.type = new TypeToken<ConcurrentHashMap<String, ConcurrentHashMap<ChunkPos, Set<Integer>>>>() {}.getType();
    }

    @Override
    public void save() {
        String jsonString = this.gson.toJson(Registries.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED);

        try(BufferedWriter bufferedWriter = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonString);
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving Destructible loaded blocks(GROUPED)!\n" + exception);
        }
    }

    @Override
    public void load() {
        try(BufferedReader bufferedReader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            ConcurrentHashMap<String, ConcurrentHashMap<ChunkPos, Set<Integer>>> registry = this.gson.fromJson(bufferedReader, type);
            Registries.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED.putAll(registry);
        } catch(IOException | JsonSyntaxException exception) {
            super.reset();
        }
    }
}