package com.minehostil.edboost.storage;

import com.minehostil.edboost.EdBoost;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Almacena de forma permanente el valor acumulado de boost que tiene
 * cada jugador por economía. Es la única fuente de verdad: el
 * multiplicador se lee de aquí en tiempo real cada vez que EdTools
 * dispara EdToolsCurrencyAddEvent (ver EdToolsListener), sin ningún
 * concepto de duración ni sincronización con la API de boosters de
 * EdTools.
 */
public class BoostStorage {

    private final EdBoost plugin;
    private Connection connection;

    public BoostStorage(EdBoost plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, "boosts.db");
        try {
            Class.forName("com.minehostil.edboost.libs.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS player_boosts (" +
                                "uuid TEXT NOT NULL, " +
                                "economy TEXT NOT NULL, " +
                                "amount REAL NOT NULL, " +
                                "PRIMARY KEY (uuid, economy)" +
                                ")"
                );
            }
        } catch (ClassNotFoundException | SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo inicializar la base de datos de boosts.", exception);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "Error cerrando la conexión de la base de datos.", exception);
            }
        }
    }

    /**
     * Suma (o resta, si amount es negativo) al valor acumulado del jugador
     * en esa economía y devuelve el nuevo total.
     */
    public double addAmount(UUID uuid, String economy, double amount) {
        double current = getAmount(uuid, economy);
        double updated = current + amount;
        setAmount(uuid, economy, updated);
        return updated;
    }

    public void setAmount(UUID uuid, String economy, double amount) {
        String sql = "INSERT INTO player_boosts (uuid, economy, amount) VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid, economy) DO UPDATE SET amount = excluded.amount";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, economy.toLowerCase());
            statement.setDouble(3, amount);
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Error guardando el boost en la base de datos.", exception);
        }
    }

    public double getAmount(UUID uuid, String economy) {
        String sql = "SELECT amount FROM player_boosts WHERE uuid = ? AND economy = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, economy.toLowerCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("amount");
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Error leyendo el boost de la base de datos.", exception);
        }
        return 0.0D;
    }

    public void removeEconomy(UUID uuid, String economy) {
        String sql = "DELETE FROM player_boosts WHERE uuid = ? AND economy = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, economy.toLowerCase());
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Error eliminando el boost de la base de datos.", exception);
        }
    }

    /**
     * Devuelve un mapa economía -> cantidad con todos los boosts
     * permanentes activos del jugador. Se usa para /edboost list y para
     * el placeholder de "total".
     */
    public Map<String, Double> getAllAmounts(UUID uuid) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT economy, amount FROM player_boosts WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.put(resultSet.getString("economy"), resultSet.getDouble("amount"));
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Error leyendo todos los boosts de la base de datos.", exception);
        }
        return result;
    }
}
