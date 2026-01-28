package com.haksnbot.ffclaims.naming;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Data manager for claim names.
 */
public class NamingDataManager {

    private final JavaPlugin plugin;
    private final Map<Long, String> claimNames = new HashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

    public NamingDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<Long, String> getClaimNames() {
        return claimNames;
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "naming-data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create naming-data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        claimNames.clear();
        if (dataConfig.contains("claims")) {
            for (String key : dataConfig.getConfigurationSection("claims").getKeys(false)) {
                try {
                    long id = Long.parseLong(key);
                    claimNames.put(id, dataConfig.getString("claims." + key));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public void save() {
        if (dataConfig == null) return;

        // Clear and rewrite
        dataConfig.set("claims", null);
        for (Map.Entry<Long, String> entry : claimNames.entrySet()) {
            dataConfig.set("claims." + entry.getKey(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save naming-data.yml: " + e.getMessage());
        }
    }

    public String getClaimName(long claimId) {
        return claimNames.get(claimId);
    }

    public void setClaimName(long claimId, String name) {
        claimNames.put(claimId, name);
        save();
    }

    public void removeClaimName(long claimId) {
        claimNames.remove(claimId);
        save();
    }
}
