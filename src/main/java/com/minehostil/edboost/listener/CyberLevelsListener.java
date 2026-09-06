package com.minehostil.edboost.listener;

import com.minehostil.edboost.manager.BoostManager;
import com.bitaspire.cyberlevels.event.ExpChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Aplica el boost permanente del jugador a la EXP ganada en CyberLevels.
 *
 * Verificado contra el JAR real (paquete com.bitaspire.cyberlevels.event):
 * ExpChangeEvent expone getAmount()/setAmount(double) (además de un alias
 * getExpAmount()/setExpAmount(double) — se usa getAmount/setAmount por ser
 * el mismo que usa el hook original de FusionPlugin). No hay un campo
 * "boost" separado, así que se multiplica el amount directamente.
 *
 * Economía en config.yml: cyberlevels_exp.
 */
public class CyberLevelsListener implements Listener {

    private final BoostManager boostManager;

    public CyberLevelsListener(BoostManager boostManager) {
        this.boostManager = boostManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExpChange(ExpChangeEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "cyberlevels_exp");
        if (boost == 1.0D) return;

        event.setAmount(event.getAmount() * boost);
    }
}
