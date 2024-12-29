package net.qilla.destructible.files;

import org.bukkit.Bukkit;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.logging.Logger;

public abstract class DestructibleFile {

    private static final Logger LOGGER = Bukkit.getLogger();
    final String defaultResourceLoc;
    final Path newPath;
    final File newFile;

    public DestructibleFile(String defaultResource, Path filePath) {
        if(filePath == null) throw new IllegalArgumentException("Path cannot be null");
        this.defaultResourceLoc = defaultResource;
        this.newPath = filePath;
        this.newFile = filePath.toFile();
        if(!newFile.exists()) reset();
    }

    public void createFile() {
        try {
            Files.createDirectories(this.newPath.getParent());

            URL url = getClass().getClassLoader().getResource(this.defaultResourceLoc);
            if(url == null) throw new MalformedURLException("Resource not found: " + this.defaultResourceLoc);

            URLConnection connection = url.openConnection();

            connection.setUseCaches(false);
            try(InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, this.newPath);
            }
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    public void reset() {
        if(Files.exists(this.newPath)) {
            try {
                Files.move(this.newPath, Paths.get(this.newPath + ".old"), StandardCopyOption.REPLACE_EXISTING);
            } catch(IOException exception) {
                LOGGER.severe(exception.toString());
            }
            Bukkit.getLogger().severe("There was a problem loading: \"" + this.newPath + "\"\n The old file has been renamed to \"" + this.newFile.getName() + "\".old");
        }
        createFile();
    }

    public abstract void save();

    public abstract void load();
}