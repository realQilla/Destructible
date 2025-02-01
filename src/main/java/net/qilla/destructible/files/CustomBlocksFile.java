package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryHolder;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.typeadapters.DBlockTA;
import net.qilla.qlibrary.file.QSavedData;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CustomBlocksFile extends QSavedData<Collection<DBlock>> {

    private static CustomBlocksFile INSTANCE;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DBlock.class, new DBlockTA())
            .setPrettyPrinting()
            .create();
    private static final Type TYPE = new TypeToken<List<DBlock>>() {
    }.getType();
    private static final Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "custom_blocks.json");
    private static final String DEFAULT_RESOURCE = "custom_blocks_default.json";

    public static CustomBlocksFile getInstance() {
        if(INSTANCE == null) INSTANCE = new CustomBlocksFile();
        return INSTANCE;
    }

    private CustomBlocksFile() {
        super(GSON, TYPE, DEFAULT_RESOURCE, FILE_PATH, DRegistry.BLOCKS.values());
    }

    @Override
    public void load() {
        this.clear();
        try(BufferedReader reader = Files.newReader(FILE_PATH.toFile(), StandardCharsets.UTF_8)) {
            List<DBlock> list = GSON.fromJson(reader, TYPE);
            Map<String, DBlock> hologramMap = new HashMap<>();
            list.forEach(item -> hologramMap.put(item.getID(), item));
            DRegistry.BLOCKS.putAll(hologramMap);
        } catch(IOException e) {
            super.resetFile();
            Destructible.getInstance().getLogger().log(Level.SEVERE, "Error loading '" + FILE_PATH + "'. File has been reset!", e);
        }
    }

    @Override
    public void clear() {
        DRegistry.BLOCKS.clear();
    }
}