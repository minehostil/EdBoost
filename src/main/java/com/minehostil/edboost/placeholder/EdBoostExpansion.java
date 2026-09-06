package com.minehostil.edboost.placeholder;

import com.minehostil.edboost.config.ConfigManager;
import com.minehostil.edboost.manager.BoostManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;

/**
 * Placeholders expuestos (ver PLACEHOLDERS.md para la lista completa):
 *
 * %edboost_<economia>%           -> multiplicador total del jugador en esa economía (ej: 1.10)
 * %edboost_<economia>_percent%   -> el mismo valor como porcentaje (ej: +10.0)
 * %edboost_<economia>_max%       -> máximo configurado (multiplicador) para esa economía
 * %edboost_<economia>_name%      -> nombre mostrado (display-name) de esa economía
 *
 * %edboost_total% fue ELIMINADO: bajo la convención de multiplicador
 * directo, sumar el boost de varias economías entre sí no tiene sentido
 * (cada economía es un multiplicador independiente de una moneda
 * distinta) — igual que en ArmorBoost.
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

        String lower = params.toLowerCase(Locale.ROOT);

        if (lower.endsWith("_max")) {
            String economy = params.substring(0, params.length() - "_max".length());
            return format(configManager.getMaxBoost(economy));
        }

        if (lower.endsWith("_name")) {
            String economy = params.substring(0, params.length() - "_name".length());
            return configManager.getDisplayName(economy);
        }

        if (lower.endsWith("_percent")) {
            String economy = params.substring(0, params.length() - "_percent".length());
            double total = boostManager.getBoostValue(player.getUniqueId(), economy);
            double percent = (total - 1.0D) * 100.0D;
            String sign = percent > 0 ? "+" : "";
            return sign + String.format(Locale.ROOT, "%.1f", percent);
        }

        double value = boostManager.getBoostValue(player.getUniqueId(), lower);
        return format(value);
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%.4f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}