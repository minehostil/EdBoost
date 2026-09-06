package com.minehostil.edboost.listener;

import com.minehostil.edboost.manager.BoostManager;
import es.edwardbelt.edgens.iapi.event.EdToolsCurrencyAddEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Aplica el boost permanente del jugador en el momento en que EdTools
 * va a otorgarle una economía, usando EdToolsCurrencyAddEvent.
 *
 * CONVENCIÓN DE CÁLCULO: valor final = valor base * multiplicador total.
 * Ej: cantidad base 7, boost x2 -> 14. Se multiplica getAmount()
 * directamente, igual que el hook original de FusionPlugin (EdToolsHook).
 *
 * Antes esta clase usaba event.addMultiplier(total - 1.0), asumiendo que
 * esa API combinaba el "extra" aditivamente sobre una base. Se simplificó
 * a la multiplicación directa del amount para que el resultado sea
 * exactamente base*total sin depender de cómo EdTools combine
 * internamente su propio campo "multiplier".
 */
public class EdToolsListener implements Listener {

    private final BoostManager boostManager;

    public EdToolsListener(BoostManager boostManager) {
        this.boostManager = boostManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCurrencyAdd(EdToolsCurrencyAddEvent event) {
        double boost = boostManager.getBoostValue(event.getUuid(), event.getCurrency());
        if (boost == 1.0D) return;

        event.setAmount(event.getAmount() * boost);
    }
}
