package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DRegistry;
import org.bukkit.Bukkit;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedDestructibleBlocksFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "loaded_destructible_blocks_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "localdb" + File.separator + "loaded_destructible_blocks.json");
    private final Gson gson;
    private final Type type;

    public LoadedDestructibleBlocksFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
        this.gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();
        this.type = new TypeToken<ConcurrentHashMap<Long, ConcurrentHashMap<Integer, String>>>() {}.getType();
    }

    @Override
    public void save() {
        String jsonString = this.gson.toJson(DRegistry.LOADED_DESTRUCTIBLE_BLOCKS);

        try(BufferedWriter bufferedWriter = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonString);
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving Destructible loaded blocks!\n" + exception);
        }
    }

    @Override
    public void load() {
        try(BufferedReader bufferedReader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            ConcurrentHashMap<Long, ConcurrentHashMap<Integer, String>> registry = this.gson.fromJson(bufferedReader, type);
            DRegistry.LOADED_DESTRUCTIBLE_BLOCKS.putAll(registry);
        } catch(IOException | JsonSyntaxException exception) {
            super.reset();
            Bukkit.getLogger().severe("There was a problem loading: \"" + this.newPath + "\"\n The old file has been renamed to \"" + this.newFile.getName() + "\".old");
        }
    }

    @Override
    public void clear() {
        DRegistry.LOADED_DESTRUCTIBLE_BLOCKS.clear();
    }
}