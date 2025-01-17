package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.typeadapters.DToolTA;
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

public class CustomToolsFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "custom_tools_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "custom_tools.json");
    private final Type type;
    private final Gson gson;

    public CustomToolsFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
        this.type = new TypeToken<List<DTool>>() {
        }.getType();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(DTool.class, new DToolTA())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void save() {
        List<DTool> dBlockList = Registries.DESTRUCTIBLE_ITEMS.values().stream()
                .filter(item -> item instanceof DTool)
                .map(item -> (DTool) item)
                .toList();

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
            List<DTool> dBlockList = this.gson.fromJson(bufferedReader, type);
            for(DTool dTool : dBlockList) Registries.DESTRUCTIBLE_ITEMS.put(dTool.getId(), dTool);
        } catch(IOException | JsonSyntaxException exception) {
            super.reset();
            Bukkit.getLogger().severe("There was a problem loading: \"" + this.newPath + "\"\n The old file has been renamed to \"" + this.newFile.getName() + "\".old");
        }
    }

    @Override
    public void clear() {
        Registries.DESTRUCTIBLE_ITEMS.clear();
        save();
    }
}