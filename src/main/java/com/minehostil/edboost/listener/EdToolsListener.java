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
 * Se usa addMultiplier() en vez de tocar getAmount()/setAmount() directo,
 * para no pisar el cálculo de EdTools ni el de otros plugins que también
 * escuchen este evento (boosters nativos, otros hooks, etc.) — cada uno
 * suma su propio multiplicador sobre el mismo evento.
 */
public class EdToolsListener implements Listener {

    private final BoostManager boostManager;

    public EdToolsListener(BoostManager boostManager) {
        this.boostManager = boostManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCurrencyAdd(EdToolsCurrencyAddEvent event) {
        double boost = boostManager.getBoostValue(event.getUuid(), event.getCurrency());
        if (boost <= 0.0D) return;

        event.addMultiplier(boost);
    }
}