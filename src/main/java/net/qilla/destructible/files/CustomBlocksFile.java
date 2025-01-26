package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryMaster;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.typeadapters.DBlockTA;
import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CustomBlocksFile extends DestructibleFile {

    private static CustomBlocksFile INSTANCE;
    private static final String DEFAULT_RESOURCE = "custom_blocks_default.json";
    private static final Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "custom_blocks.json");
    private final Type type = new TypeToken<List<DBlock>>() {
    }.getType();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DBlock.class, new DBlockTA())
            .setPrettyPrinting()
            .create();

    public static CustomBlocksFile getInstance() {
        if(INSTANCE == null) INSTANCE = new CustomBlocksFile();
        return INSTANCE;
    }

    private CustomBlocksFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
    }

    @Override
    public void save() {
        try(BufferedWriter writer = Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            var registry = DRegistry.BLOCKS;
            List<DBlock> list = registry.values().stream().toList();

            writer.write(gson.toJson(list, type));
        } catch(IOException e) {
            Bukkit.getLogger().severe("Error saving custom blocks!\n" + e.getMessage());
        }
    }

    @Override
    public void load() {
        this.clear();
        try(BufferedReader reader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            List<DBlock> list = gson.fromJson(reader, type);
            var registry = DRegistry.BLOCKS;

            list.forEach(item -> registry.put(item.getId(), item));
        } catch(IOException | JsonSyntaxException e) {
            super.reset();
            Bukkit.getLogger().severe("Error loading \"" + this.newPath + "\". File has been reset!\n" + e.getMessage());
        }
    }

    @Override
    public void clear() {
        DRegistry.BLOCKS.clear();
    }
}