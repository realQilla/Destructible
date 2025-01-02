package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.RegistryMap;
import net.qilla.destructible.data.Registries;
import org.bukkit.Bukkit;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadedDestructibleBlocksFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "loaded_destructible_blocks_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "localdb" + File.separator + "loaded_destructible_blocks.json");
    private final Gson gson;
    private final Type type;

    public LoadedDestructibleBlocksFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
        this.gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .create();
        this.type = new TypeToken<RegistryMap<ChunkPos, RegistryMap<Integer, String>>>() {}.getType();
    }

    @Override
    public void save() {
        String jsonString = this.gson.toJson(Registries.LOADED_DESTRUCTIBLE_BLOCKS);

        try(BufferedWriter bufferedWriter = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonString);
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving Destructible loaded blocks!\n" + exception);
        }
    }

    @Override
    public void load() {
        try(BufferedReader bufferedReader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            RegistryMap<ChunkPos, RegistryMap<Integer, String>> registry = this.gson.fromJson(bufferedReader, type);
            Registries.LOADED_DESTRUCTIBLE_BLOCKS.putAll(registry);
        } catch(IOException | JsonSyntaxException exception) {
            super.reset();
        }
    }
}