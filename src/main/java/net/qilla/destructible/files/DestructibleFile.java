package net.qilla.destructible.files;

import org.bukkit.Bukkit;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.logging.Level;
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
            Files.createDirectories(newPath.getParent());

            URL url = getClass().getClassLoader().getResource(defaultResourceLoc);
            if(url == null) throw new MalformedURLException("Resource not found: " + defaultResourceLoc);

            URLConnection connection = url.openConnection();

            connection.setUseCaches(false);
            try(InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, this.newPath);
            }
        } catch(IOException e) {
            LOGGER.log(Level.SEVERE, "There was a problem creating \"" + this.newPath + "\"!\n" + e.getMessage());
        }
    }

    public void reset() {
        if(Files.exists(this.newPath)) {
            try {
                Files.move(this.newPath, Paths.get(this.newPath + ".old"), StandardCopyOption.REPLACE_EXISTING);
            } catch(IOException e) {
                LOGGER.log(Level.SEVERE, "There was a problem resetting \"" + this.newPath + "\"!\n" + e.getMessage());
            }
        }
        this.createFile();
    }

    public abstract void save();

    public abstract void load();

    public abstract void clear();
}