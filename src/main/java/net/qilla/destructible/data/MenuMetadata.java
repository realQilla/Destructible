package net.qilla.destructible.data;

import java.util.Map;

public class MenuMetadata {

    private final Map<String, Object> metadata;

    public MenuMetadata() {
        metadata = new java.util.HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if(!metadata.containsKey(key)) return null;
        return (T) metadata.get(key);
    }

    public boolean has(String key) {
        return metadata.containsKey(key);
    }

    public void set(String key, Object value) {
        metadata.put(key, value);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
