package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.typeadapters.DItemTA;
import net.qilla.qlibrary.file.QSavedData;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CustomItemsFile extends QSavedData<Collection<DItem>> {

    private static CustomItemsFile INSTANCE;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DItem.class, new DItemTA())
            .setPrettyPrinting()
            .create();
    private static final Type TYPE = new TypeToken<List<DItem>>() {
    }.getType();
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "custom_items.json");
    private final static String DEFAULT_RESOURCE = "custom_items_default.json";

    public static CustomItemsFile getInstance() {
        if(INSTANCE == null) INSTANCE = new CustomItemsFile();
        return INSTANCE;
    }

    private CustomItemsFile() {
        super(GSON, TYPE, DEFAULT_RESOURCE, FILE_PATH, DRegistry.ITEMS.values());
    }

    @Override
    public void load() {
        this.clear();
        try(BufferedReader reader = Files.newReader(FILE_PATH.toFile(), StandardCharsets.UTF_8)) {
            List<DItem> list = GSON.fromJson(reader, TYPE);
            Map<String, DItem> hologramMap = new HashMap<>();
            list.forEach(item -> hologramMap.put(item.getID(), item));
            DRegistry.ITEMS.putAll(hologramMap);
        } catch(IOException e) {
            super.resetFile();
            Destructible.getInstance().getLogger().log(Level.SEVERE, "Error loading '" + FILE_PATH + "'. File has been reset!", e);
        }
    }

    @Override
    public void clear() {
        DRegistry.ITEMS.clear();
    }
}