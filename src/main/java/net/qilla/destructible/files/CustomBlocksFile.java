package net.qilla.destructible.files;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.typeadapters.DBlockTA;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.typeadapters.DItemTA;
import org.bukkit.Bukkit;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CustomBlocksFile extends DestructibleFile {

    private final static String DEFAULT_RESOURCE = "custom_blocks_default.json";
    private final static Path FILE_PATH = Paths.get(Destructible.getInstance().getDataFolder() + File.separator + "custom_blocks.json");
    private final Type type;
    private final Gson gson;

    public CustomBlocksFile() {
        super(DEFAULT_RESOURCE, FILE_PATH);
        this.type = new TypeToken<List<DBlock>>() {}.getType();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(DItem.class, new DItemTA())
                .registerTypeAdapter(DBlock.class, new DBlockTA())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void save() {
        List<DBlock> dBlockList = DRegistry.DESTRUCTIBLE_BLOCKS.values().stream().toList();

        String jsonString = this.gson.toJson(dBlockList, type);

        try(BufferedWriter bufferedWriter = com.google.common.io.Files.newWriter(super.newFile, StandardCharsets.UTF_8)) {
            bufferedWriter.write(jsonString);
        } catch(IOException exception) {
            Bukkit.getLogger().severe("There was a problem saving custom blocks!" + exception);
        }
    }

    @Override
    public void load() {
        try(BufferedReader bufferedReader = Files.newReader(super.newFile, StandardCharsets.UTF_8)) {
            List<DBlock> dBlockList = this.gson.fromJson(bufferedReader, type);
            DRegistry.DESTRUCTIBLE_BLOCKS.clear();
            for(DBlock dBlock : dBlockList) DRegistry.DESTRUCTIBLE_BLOCKS.put(dBlock.getId(), dBlock);
        } catch(IOException | JsonSyntaxException exception) {
            super.reset();
            Bukkit.getLogger().severe("There was a problem loading: \"" + this.newPath + "\"\n The old file has been renamed to \"" + this.newFile.getName() + "\".old");
        }
    }

    @Override
    public void clear() {
        DRegistry.DESTRUCTIBLE_BLOCKS.clear();
    }
}