package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemTA;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CustomItemsFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "custom_items_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "custom_items.json");
    private final Type type;
    private final Gson gson;

    public CustomItemsFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
        this.type = new TypeToken<List<DItem>>() {}.getType();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(DItem.class, new DItemTA())
                .create();
    }

    @Override
    public void save() {
        List<DItem> dBlockList = Registries.DESTRUCTIBLE_ITEMS.values().stream().toList();

        String jsonString = this.gson.toJson(dBlockList, type);

        try(BufferedWriter bufferedWriter = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonString);
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving custom items!" + exception);
        }
    }

    @Override
    public void load() {
        try(BufferedReader bufferedReader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            List<DItem> dBlockList = this.gson.fromJson(bufferedReader, type);
            for(DItem dItem : dBlockList) Registries.DESTRUCTIBLE_ITEMS.put(dItem.getId(), dItem);
        } catch(IOException exception) {
            super.reset();
        }
    }
}