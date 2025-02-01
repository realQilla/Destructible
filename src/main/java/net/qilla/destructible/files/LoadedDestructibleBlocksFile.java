package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.qlibrary.file.QSavedData;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

public class LoadedDestructibleBlocksFile extends QSavedData<Map<Long, Map<Integer, String>>> {

    private static LoadedDestructibleBlocksFile INSTANCE;

    private static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();
    private static final Type TYPE = new TypeToken<Map<Long, Map<Integer, String>>>() {
    }.getType();
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "localdb" + File.separator + "loaded_destructible_blocks.json");
    private final static String DEFAULT_RESOURCE = "loaded_destructible_blocks_default.json";

    public static LoadedDestructibleBlocksFile getInstance() {
        if(INSTANCE == null) INSTANCE = new LoadedDestructibleBlocksFile();
        return INSTANCE;
    }

    private LoadedDestructibleBlocksFile() {
        super(GSON, TYPE, DEFAULT_RESOURCE, FILE_PATH, DRegistry.LOADED_BLOCKS);

    }

    @Override
    public void load() {
        this.clear();
        try(BufferedReader reader = Files.newReader(FILE_PATH.toFile(), StandardCharsets.UTF_8)) {
            Map<Long, Map<Integer, String>> list = GSON.fromJson(reader, TYPE);

            list.forEach((chunkKey, subChunkMap) -> {
                subChunkMap.forEach((subChunkKey, blockID) -> {
                    DRegistry.LOADED_BLOCKS_GROUPED.computeIfAbsent(blockID, key -> new HashMap<>())
                            .computeIfAbsent(chunkKey, key -> new HashSet<>())
                            .add(subChunkKey);
                });
            });

            DRegistry.LOADED_BLOCKS.putAll(list);
        } catch(IOException e) {
            super.resetFile();
            Destructible.getInstance().getLogger().log(Level.SEVERE, "Error loading '" + FILE_PATH + "'. File has been reset!", e);
        }
    }

    @Override
    public void clear() {
        DRegistry.LOADED_BLOCKS.clear();
        DRegistry.LOADED_BLOCKS_GROUPED.clear();
    }
}