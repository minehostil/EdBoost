package com.minehostil.edboost.commands;

import com.minehostil.edboost.config.ConfigManager;
import com.minehostil.edboost.config.MessageManager;
import com.minehostil.edboost.manager.BoostManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * /edboost add <jugador> <economia> <cantidad>
 * /edboost remove <jugador> <economia> <cantidad>
 * /edboost reset <jugador> <economia|all>
 * /edboost list <jugador>
 */
public class EdBoostCommand implements CommandExecutor, TabCompleter {

    private final BoostManager boostManager;
    private final ConfigManager configManager;
    private final MessageManager messages;

    public EdBoostCommand(BoostManager boostManager, ConfigManager configManager, MessageManager messages) {
        this.boostManager = boostManager;
        this.configManager = configManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("edboost.use")) {
            messages.send(sender, "no-permission-use");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "add":
                return handleAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "list":
                return handleList(sender, args);
            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("edboost.admin")) {
            messages.send(sender, "no-permission-admin");
            return true;
        }
        if (args.length < 4) {
            messages.send(sender, "usage-add-only");
            return true;
        }

        OfflinePlayer target = resolvePlayer(sender, args[1]);
        if (target == null) return true;

        String economy = args[2];
        if (!validateEconomy(sender, economy)) return true;

        Double amount = parseDouble(sender, args[3]);
        if (amount == null) return true;

        applyAdd(sender, target, economy, amount);
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("edboost.admin")) {
            messages.send(sender, "no-permission-admin");
            return true;
        }
        if (args.length < 4) {
            messages.send(sender, "usage-remove-only");
            return true;
        }

        OfflinePlayer target = resolvePlayer(sender, args[1]);
        if (target == null) return true;

        String economy = args[2];
        if (!validateEconomy(sender, economy)) return true;

        Double amount = parseDouble(sender, args[3]);
        if (amount == null) return true;

        applyRemove(sender, target, economy, amount);
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("edboost.admin")) {
            messages.send(sender, "no-permission-admin");
            return true;
        }
        if (args.length < 3) {
            messages.send(sender, "usage-reset-only");
            return true;
        }

        OfflinePlayer target = resolvePlayer(sender, args[1]);
        if (target == null) return true;

        String economy = args[2];
        if (economy.equalsIgnoreCase("all")) {
            boostManager.resetAllBoosts(target.getUniqueId());
            messages.send(sender, "reset-all", Map.of("player", nameOf(target)));
        } else {
            if (!validateEconomy(sender, economy)) return true;
            boostManager.resetBoost(target.getUniqueId(), economy);
            messages.send(sender, "reset-one", Map.of(
                    "economy", configManager.getDisplayName(economy),
                    "player", nameOf(target)
            ));
        }
        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        OfflinePlayer target;
        if (args.length >= 2) {
            target = resolvePlayer(sender, args[1]);
            if (target == null) return true;
        } else if (sender instanceof org.bukkit.entity.Player) {
            target = (OfflinePlayer) sender;
        } else {
            messages.send(sender, "usage-list-only");
            return true;
        }

        Map<String, Double> boosts = boostManager.getAllBoosts(target.getUniqueId());
        if (boosts.isEmpty()) {
            messages.send(sender, "list-empty", Map.of("player", nameOf(target)));
            return true;
        }

        messages.send(sender, "list-header", Map.of("player", nameOf(target)));
        double total = 0.0D;
        for (Map.Entry<String, Double> entry : boosts.entrySet()) {
            messages.send(sender, "list-entry", Map.of(
                    "economy", configManager.getDisplayName(entry.getKey()),
                    "amount", format(entry.getValue())
            ));
            total += entry.getValue();
        }
        messages.send(sender, "list-total", Map.of("total", format(total)));
        return true;
    }

    private void applyAdd(CommandSender sender, OfflinePlayer target, String economy, double amount) {
        BoostManager.AddResult result = boostManager.addBoost(target.getUniqueId(), economy, amount);
        if (!result.isAccepted()) {
            messages.send(sender, "boost-max-exceeded", Map.of(
                    "economy", configManager.getDisplayName(economy),
                    "max", format(result.getMax()),
                    "total", format(result.getTotal())
            ));
            return;
        }
        messages.send(sender, "boost-added", Map.of(
                "amount", format(amount),
                "economy", configManager.getDisplayName(economy),
                "player", nameOf(target),
                "total", format(result.getTotal())
        ));
    }

    private void applyRemove(CommandSender sender, OfflinePlayer target, String economy, double amount) {
        // Quitar nunca puede superar el máximo (va hacia abajo), pero se reutiliza
        // addBoost con el monto negativo para mantener una sola fuente de verdad.
        BoostManager.AddResult result = boostManager.addBoost(target.getUniqueId(), economy, -amount);
        if (!result.isAccepted()) {
            messages.send(sender, "boost-max-exceeded", Map.of(
                    "economy", configManager.getDisplayName(economy),
                    "max", format(result.getMax()),
                    "total", format(result.getTotal())
            ));
            return;
        }
        messages.send(sender, "boost-removed", Map.of(
                "amount", format(amount),
                "economy", configManager.getDisplayName(economy),
                "player", nameOf(target),
                "total", format(result.getTotal())
        ));
    }

    private void sendUsage(CommandSender sender) {
        messages.send(sender, "usage-header");
        messages.send(sender, "usage-add");
        messages.send(sender, "usage-remove");
        messages.send(sender, "usage-reset");
        messages.send(sender, "usage-list");
    }

    private boolean validateEconomy(CommandSender sender, String economy) {
        if (!configManager.isValidEconomy(economy)) {
            messages.send(sender, "unknown-economy", Map.of("economy", economy));
            return false;
        }
        return true;
    }

    private OfflinePlayer resolvePlayer(CommandSender sender, String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player == null || (!player.hasPlayedBefore() && !player.isOnline())) {
            messages.send(sender, "player-not-found");
            return null;
        }
        return player;
    }

    private Double parseDouble(CommandSender sender, String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException exception) {
            messages.send(sender, "invalid-amount");
            return null;
        }
    }

    private String nameOf(OfflinePlayer player) {
        return player.getName() != null ? player.getName() : player.getUniqueId().toString();
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%.4f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("add", "remove", "reset", "list"));
        } else if (args.length == 2) {
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                options.add(player.getName());
            }
        } else if (args.length == 3 && List.of("add", "remove", "reset").contains(args[0].toLowerCase(Locale.ROOT))) {
            options.addAll(configManager.getEconomies().keySet());
            if (args[0].equalsIgnoreCase("reset")) {
                options.add("all");
            }
        }

        List<String> filtered = new ArrayList<>();
        String current = args[args.length - 1].toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(current)) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}