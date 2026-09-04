package com.minehostil.edboost.config;

import com.minehostil.edboost.EdBoost;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carga messages.yml y resuelve mensajes con placeholders (%player%,
 * %economy%, %amount%, %total%, %max%) y color (& legacy y &#rrggbb hex,
 * siguiendo la convención del proyecto).
 */
public class MessageManager {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final EdBoost plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessageManager(EdBoost plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        prefix = messages.getString("prefix", "&#55ff55[EdBoost]&r ");
    }

    /** Envía un mensaje ya formateado (con prefijo, color y placeholders resueltos). */
    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(colorize(prefix + resolve(key, placeholders)));
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    /** Devuelve el mensaje ya con placeholders reemplazados, sin color ni prefijo aplicado aún. */
    private String resolve(String key, Map<String, String> placeholders) {
        String raw = messages.getString(key, key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return raw;
    }

    private String colorize(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            ChatColor hexColor = ChatColor.of("#" + matcher.group(1));
            matcher.appendReplacement(buffer, hexColor.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
