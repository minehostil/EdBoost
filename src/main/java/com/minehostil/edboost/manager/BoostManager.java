package com.minehostil.edboost.manager;

import com.minehostil.edboost.config.ConfigManager;
import com.minehostil.edboost.storage.BoostStorage;

import java.util.Map;
import java.util.UUID;

/**
 * Gestiona el registro permanente de boosts por jugador/economía.
 *
 * A diferencia de un enfoque basado en EdToolsBoostersAPI (que requiere
 * simular "permanente" con una duración enorme en segundos), este manager
 * es una capa simple sobre BoostStorage: el multiplicador se aplica en
 * tiempo real interceptando EdToolsCurrencyAddEvent (ver EdToolsListener),
 * así que no existe ningún concepto de "duración" que gestionar aquí.
 *
 * También aplica el límite máximo de boost por economía, leído de
 * config.yml a través de ConfigManager.
 */
public class BoostManager {

    private final BoostStorage storage;
    private final ConfigManager configManager;

    public BoostManager(BoostStorage storage, ConfigManager configManager) {
        this.storage = storage;
        this.configManager = configManager;
    }

    /**
     * Suma "amount" al boost permanente del jugador para esa economía,
     * respetando el máximo configurado para esa economía.
     *
     * Si el resultado superaría el máximo, no se guarda nada y se
     * devuelve un AddResult rechazado con el total actual (sin modificar).
     */
    public AddResult addBoost(UUID uuid, String economy, double amount) {
        double current = storage.getAmount(uuid, economy);
        double newTotal = current + amount;
        double max = configManager.getMaxBoost(economy);

        if (newTotal > max) {
            return AddResult.rejected(current, max);
        }

        storage.setAmount(uuid, economy, newTotal);
        return AddResult.accepted(newTotal);
    }

    /** Reinicia (elimina) el boost permanente del jugador para una economía específica. */
    public void resetBoost(UUID uuid, String economy) {
        storage.removeEconomy(uuid, economy);
    }

    /** Reinicia todos los boosts permanentes del jugador en todas las economías. */
    public void resetAllBoosts(UUID uuid) {
        for (String economy : storage.getAllAmounts(uuid).keySet()) {
            storage.removeEconomy(uuid, economy);
        }
    }

    public double getBoostValue(UUID uuid, String economy) {
        return storage.getAmount(uuid, economy);
    }

    public double getTotalBoostValue(UUID uuid) {
        double total = 0.0D;
        for (double value : storage.getAllAmounts(uuid).values()) {
            total += value;
        }
        return total;
    }

    public Map<String, Double> getAllBoosts(UUID uuid) {
        return storage.getAllAmounts(uuid);
    }

    /** Resultado de un intento de suma/resta de boost, respetando el máximo configurado. */
    public static class AddResult {
        private final boolean accepted;
        private final double total;
        private final double max;

        private AddResult(boolean accepted, double total, double max) {
            this.accepted = accepted;
            this.total = total;
            this.max = max;
        }

        public static AddResult accepted(double newTotal) {
            return new AddResult(true, newTotal, 0.0D);
        }

        public static AddResult rejected(double currentTotal, double max) {
            return new AddResult(false, currentTotal, max);
        }

        public boolean isAccepted() {
            return accepted;
        }

        public double getTotal() {
            return total;
        }

        public double getMax() {
            return max;
        }
    }
}