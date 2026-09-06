package com.minehostil.edboost.manager;

import com.minehostil.edboost.config.ConfigManager;
import com.minehostil.edboost.storage.BoostStorage;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Gestiona el registro permanente de boosts por jugador/economía.
 *
 * CONVENCIÓN MATEMÁTICA (igual que ArmorBoost): el valor almacenado por
 * economía es un MULTIPLICADOR DIRECTO, no un porcentaje aditivo.
 * Ej: 1.05 = x1.05 (+5%). Un total de 1.0 significa "sin boost".
 *
 * ADVERTENCIA DE DISEÑO: si el total acumulado de un jugador para una
 * economía queda por debajo de 1.0, el multiplicador REDUCIRÁ su dinero
 * en vez de aumentarlo, porque EdToolsListener lo aplica tal cual
 * (dinero * total). addBoost() por eso exige un "confirmed=true"
 * explícito para aceptar cualquier resultado por debajo de 1.0 — sin
 * confirmación, se rechaza y no se guarda nada.
 *
 * getTotalBoostValue() fue eliminado: sumar el multiplicador de varias
 * economías entre sí no tiene sentido matemático bajo esta convención
 * (cada economía es independiente), igual que en ArmorBoost.
 *
 * A diferencia de un enfoque basado en EdToolsBoostersAPI (que requiere
 * simular "permanente" con una duración enorme en segundos), este manager
 * es una capa simple sobre BoostStorage: el multiplicador se aplica en
 * tiempo real interceptando EdToolsCurrencyAddEvent (ver EdToolsListener),
 * así que no existe ningún concepto de "duración" que gestionar aquí.
 */
public class BoostManager {

    /** Total por debajo de este valor reduce el dinero del jugador en vez de aumentarlo. */
    private static final double BASELINE = 1.0D;

    private final BoostStorage storage;
    private final ConfigManager configManager;
    private final Logger logger;

    public BoostManager(BoostStorage storage, ConfigManager configManager, Logger logger) {
        this.storage = storage;
        this.configManager = configManager;
        this.logger = logger;
    }

    /**
     * Suma "amount" al multiplicador permanente del jugador para esa
     * economía, respetando el máximo configurado (también un
     * multiplicador, ej: 2.0 = tope de x2.0).
     *
     * Si el resultado supera el máximo, se rechaza sin guardar nada.
     * Si el resultado queda por debajo de 1.0 (reduciría el dinero del
     * jugador) y "confirmed" es false, también se rechaza sin guardar
     * nada — el llamador debe pedir confirmación explícita y reintentar
     * con confirmed=true. Si se guarda por debajo de 1.0, se deja un
     * log de advertencia en consola.
     */
    public AddResult addBoost(UUID uuid, String economy, double amount, boolean confirmed) {
        double current = storage.getAmount(uuid, economy);
        double newTotal = current + amount;
        double max = configManager.getMaxBoost(economy);

        if (newTotal > max) {
            return AddResult.rejectedMaxExceeded(current, max);
        }

        boolean belowBaseline = newTotal < BASELINE;
        if (belowBaseline && !confirmed) {
            return AddResult.rejectedBelowBaseline(current, newTotal);
        }

        if (belowBaseline) {
            logger.warning("El boost de '" + economy + "' para " + uuid
                    + " quedó en x" + newTotal + ", por debajo de x1.0 — esto REDUCIRÁ su "
                    + economy + " en vez de aumentarlo. Confirmado explícitamente por un admin.");
        }

        storage.setAmount(uuid, economy, newTotal);
        return AddResult.accepted(newTotal, belowBaseline);
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

    /** Devuelve el multiplicador total del jugador para esa economía (1.0 si no tiene boost). */
    public double getBoostValue(UUID uuid, String economy) {
        double stored = storage.getAmount(uuid, economy);
        return stored <= 0.0D ? BASELINE : stored;
    }

    public Map<String, Double> getAllBoosts(UUID uuid) {
        return storage.getAllAmounts(uuid);
    }

    /** Resultado de un intento de suma/resta de boost. */
    public static class AddResult {
        public enum Reason { NONE, MAX_EXCEEDED, BELOW_BASELINE_UNCONFIRMED }

        private final boolean accepted;
        private final double total;
        private final double max;
        private final boolean belowBaseline;
        private final Reason reason;

        private AddResult(boolean accepted, double total, double max, boolean belowBaseline, Reason reason) {
            this.accepted = accepted;
            this.total = total;
            this.max = max;
            this.belowBaseline = belowBaseline;
            this.reason = reason;
        }

        public static AddResult accepted(double newTotal, boolean belowBaseline) {
            return new AddResult(true, newTotal, 0.0D, belowBaseline, Reason.NONE);
        }

        public static AddResult rejectedMaxExceeded(double currentTotal, double max) {
            return new AddResult(false, currentTotal, max, false, Reason.MAX_EXCEEDED);
        }

        public static AddResult rejectedBelowBaseline(double currentTotal, double wouldBeTotal) {
            return new AddResult(false, wouldBeTotal, 0.0D, true, Reason.BELOW_BASELINE_UNCONFIRMED);
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

        public boolean isBelowBaseline() {
            return belowBaseline;
        }

        public Reason getReason() {
            return reason;
        }
    }
}