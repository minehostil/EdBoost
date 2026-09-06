package com.minehostil.edboost.config;

import com.minehostil.edboost.EdBoost;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

        if (mergeMissingDefaultEconomies(config)) {
            plugin.saveConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }

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

    /**
     * saveDefaultConfig() solo escribe el config.yml empaquetado si el
     * archivo TODAVÍA NO EXISTE en el servidor — si ya existe (por
     * ejemplo, tras actualizar el plugin y agregar nuevas economías como
     * hoes_essence, pickaxes_money, etc.), Bukkit nunca fusiona las claves
     * nuevas automáticamente. Sin esto, cualquier economía añadida en una
     * versión posterior del plugin queda invisible en un servidor que ya
     * tenía EdBoost instalado, y /edboost add la rechaza como "economía
     * inexistente" aunque el listener correspondiente sí esté activo.
     *
     * Este método compara las economías del config.yml empaquetado dentro
     * del JAR contra las del config.yml real en disco, y agrega las que
     * falten (sin tocar ni sobrescribir las que el admin ya haya
     * personalizado). Devuelve true si se agregó algo nuevo.
     */
    private boolean mergeMissingDefaultEconomies(FileConfiguration liveConfig) {
        try (InputStream stream = plugin.getResource("config.yml")) {
            if (stream == null) return false;

            YamlConfiguration packagedDefaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));

            ConfigurationSection defaultEconomies = packagedDefaults.getConfigurationSection("economies");
            if (defaultEconomies == null) return false;

            boolean changed = false;
            for (String key : defaultEconomies.getKeys(false)) {
                String path = "economies." + key;
                if (liveConfig.contains(path)) continue;

                liveConfig.set(path + ".display-name", packagedDefaults.getString(path + ".display-name"));
                liveConfig.set(path + ".max-boost", packagedDefaults.getDouble(path + ".max-boost"));
                changed = true;
                plugin.getLogger().info("Economía '" + key + "' añadida automáticamente a config.yml (nueva en esta versión).");
            }
            return changed;
        } catch (IOException exception) {
            plugin.getLogger().warning("No se pudo revisar economías nuevas para fusionar en config.yml: " + exception.getMessage());
            return false;
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
