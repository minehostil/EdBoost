package com.minehostil.edboost;

import com.minehostil.edboost.commands.EdBoostCommand;
import com.minehostil.edboost.config.ConfigManager;
import com.minehostil.edboost.config.MessageManager;
import com.minehostil.edboost.listener.CyberLevelsListener;
import com.minehostil.edboost.listener.EdToolsListener;
import com.minehostil.edboost.listener.RivalHarvesterHoesListener;
import com.minehostil.edboost.listener.RivalMobSwordsListener;
import com.minehostil.edboost.listener.RivalPickaxesListener;
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

        boostManager = new BoostManager(storage, configManager, getLogger());

        EdBoostCommand command = new EdBoostCommand(boostManager, configManager, messageManager);
        getCommand("edboost").setExecutor(command);
        getCommand("edboost").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new EdToolsListener(boostManager), this);
        registerOptionalHooks();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EdBoostExpansion(boostManager, configManager).register();
            getLogger().info("Expansión de PlaceholderAPI registrada.");
        } else {
            getLogger().warning("PlaceholderAPI no está instalado. Los placeholders %edboost_*% no funcionarán.");
        }

        getLogger().info("EdBoost habilitado correctamente.");
    }

    /**
     * Registra los listeners de los plugins opcionales (soft-dependency):
     * RivalHarvesterHoes, RivalPickaxes, RivalMobSwords y CyberLevels.
     * Cada uno solo se activa si el plugin correspondiente está presente
     * en el servidor; si no, simplemente no se registra el listener y se
     * deja un log informativo (mismo patrón que los hooks de FusionPlugin).
     */
    private void registerOptionalHooks() {
        if (getServer().getPluginManager().getPlugin("RivalHarvesterHoes") != null) {
            getServer().getPluginManager().registerEvents(new RivalHarvesterHoesListener(boostManager), this);
            getLogger().info("Hook de RivalHarvesterHoes conectado.");
        } else {
            getLogger().info("RivalHarvesterHoes no está instalado, se omite ese hook.");
        }

        if (getServer().getPluginManager().getPlugin("RivalPickaxes") != null) {
            getServer().getPluginManager().registerEvents(new RivalPickaxesListener(boostManager), this);
            getLogger().info("Hook de RivalPickaxes conectado.");
        } else {
            getLogger().info("RivalPickaxes no está instalado, se omite ese hook.");
        }

        if (getServer().getPluginManager().getPlugin("RivalMobSwords") != null) {
            getServer().getPluginManager().registerEvents(new RivalMobSwordsListener(boostManager), this);
            getLogger().info("Hook de RivalMobSwords conectado.");
        } else {
            getLogger().info("RivalMobSwords no está instalado, se omite ese hook.");
        }

        if (getServer().getPluginManager().getPlugin("CyberLevels") != null) {
            getServer().getPluginManager().registerEvents(new CyberLevelsListener(boostManager), this);
            getLogger().info("Hook de CyberLevels conectado.");
        } else {
            getLogger().info("CyberLevels no está instalado, se omite ese hook.");
        }
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