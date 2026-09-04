package com.minehostil.edboost.config;

import com.minehostil.edboost.EdBoost;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Carga config.yml: qué economías están registradas, su nombre para
 * mostrar y el multiplicador máximo acumulable por jugador.
 */
public class ConfigManager {

    private final EdBoost plugin;

    private boolean strictEconomies;
    private final Map<String, EconomyEntry> economies = new LinkedHashMap<>();

    public ConfigManager(EdBoost plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        strictEconomies = config.getBoolean("strict-economies", true);

        economies.clear();
        ConfigurationSection section = config.getConfigurationSection("economies");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = "economies." + key;
                String displayName = config.getString(path + ".display-name", key);
                double maxBoost = config.getDouble(path + ".max-boost", Double.MAX_VALUE);
                economies.put(key.toLowerCase(Locale.ROOT), new EconomyEntry(displayName, maxBoost));
            }
        }

        if (economies.isEmpty()) {
            plugin.getLogger().warning("No hay economías registradas en config.yml (sección 'economies').");
        }
    }

    public boolean isStrictEconomies() {
        return strictEconomies;
    }

    /** true si la economía está registrada en config.yml, o si strict-economies está desactivado. */
    public boolean isValidEconomy(String economy) {
        if (!strictEconomies) return true;
        return economies.containsKey(economy.toLowerCase(Locale.ROOT));
    }

    /** Nombre mostrado de la economía, o el propio ID si no está registrada. */
    public String getDisplayName(String economy) {
        EconomyEntry entry = economies.get(economy.toLowerCase(Locale.ROOT));
        return entry != null ? entry.displayName : economy;
    }

    /** Multiplicador máximo acumulable para esta economía. Sin límite (Double.MAX_VALUE) si no está registrada. */
    public double getMaxBoost(String economy) {
        EconomyEntry entry = economies.get(economy.toLowerCase(Locale.ROOT));
        return entry != null ? entry.maxBoost : Double.MAX_VALUE;
    }

    public Map<String, EconomyEntry> getEconomies() {
        return economies;
    }

    public static class EconomyEntry {
        private final String displayName;
        private final double maxBoost;

        public EconomyEntry(String displayName, double maxBoost) {
            this.displayName = displayName;
            this.maxBoost = maxBoost;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getMaxBoost() {
            return maxBoost;
        }
    }
}
