package com.minehostil.edboost;

import com.minehostil.edboost.commands.EdBoostCommand;
import com.minehostil.edboost.config.ConfigManager;
import com.minehostil.edboost.config.MessageManager;
import com.minehostil.edboost.listener.EdToolsListener;
import com.minehostil.edboost.manager.BoostManager;
import com.minehostil.edboost.placeholder.EdBoostExpansion;
import com.minehostil.edboost.storage.BoostStorage;
import org.bukkit.plugin.java.JavaPlugin;

public class EdBoost extends JavaPlugin {

    private BoostStorage storage;
    private BoostManager boostManager;
    private ConfigManager configManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("EdTools") == null) {
            getLogger().severe("EdTools no está instalado. Desactivando EdBoost.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        configManager.load();

        messageManager = new MessageManager(this);
        messageManager.load();

        storage = new BoostStorage(this);
        storage.connect();

        boostManager = new BoostManager(storage, configManager);

        EdBoostCommand command = new EdBoostCommand(boostManager, configManager, messageManager);
        getCommand("edboost").setExecutor(command);
        getCommand("edboost").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new EdToolsListener(boostManager), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EdBoostExpansion(boostManager, configManager).register();
            getLogger().info("Expansión de PlaceholderAPI registrada.");
        } else {
            getLogger().warning("PlaceholderAPI no está instalado. Los placeholders %edboost_*% no funcionarán.");
        }

        getLogger().info("EdBoost habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.disconnect();
        }
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}