package com.minehostil.edboost.placeholder;

import com.minehostil.edboost.config.ConfigManager;
import com.minehostil.edboost.manager.BoostManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;

/**
 * Placeholders expuestos (ver PLACEHOLDERS.md para la lista completa):
 *
 * %edboost_<economia>%           -> valor del boost del jugador en esa economía
 * %edboost_<economia>_max%       -> máximo configurado para esa economía
 * %edboost_<economia>_name%      -> nombre mostrado (display-name) de esa economía
 * %edboost_total%                -> suma de todos los boosts permanentes del jugador
 */
public class EdBoostExpansion extends PlaceholderExpansion {

    private final BoostManager boostManager;
    private final ConfigManager configManager;

    public EdBoostExpansion(BoostManager boostManager, ConfigManager configManager) {
        this.boostManager = boostManager;
        this.configManager = configManager;
    }

    @Override
    public String getIdentifier() {
        return "edboost";
    }

    @Override
    public String getAuthor() {
        return "MineHostil";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("total")) {
            return format(boostManager.getTotalBoostValue(player.getUniqueId()));
        }

        if (params.toLowerCase(Locale.ROOT).endsWith("_max")) {
            String economy = params.substring(0, params.length() - "_max".length());
            return format(configManager.getMaxBoost(economy));
        }

        if (params.toLowerCase(Locale.ROOT).endsWith("_name")) {
            String economy = params.substring(0, params.length() - "_name".length());
            return configManager.getDisplayName(economy);
        }

        String economy = params.toLowerCase(Locale.ROOT);
        double value = boostManager.getBoostValue(player.getUniqueId(), economy);
        return format(value);
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%.4f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}