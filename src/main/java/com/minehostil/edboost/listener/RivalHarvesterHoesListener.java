package com.minehostil.edboost.listener;

import com.minehostil.edboost.manager.BoostManager;
import me.rivaldev.harvesterhoes.api.events.HoeEssenceReceiveEnchantEvent;
import me.rivaldev.harvesterhoes.api.events.HoeXPGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Aplica el boost permanente del jugador a los eventos de RivalHarvesterHoes,
 * usando las mismas dos economías que ya cubría el hook de FusionPlugin para
 * este plugin: essence y XP de la azada.
 *
 * Verificado contra el JAR real (paquete me.rivaldev.harvesterhoes.api.events):
 * ambos eventos exponen getMultiplier()/setMultiplier(double), y se combinan
 * de forma MULTIPLICATIVA (multiplier * boost) — no aditiva como EdTools —
 * así que aquí NO se resta 1.0 antes de aplicar, se usa el total tal cual.
 *
 * Economías usadas en config.yml: hoes_essence, hoes_xp.
 *
 * Nota: HoeMoneyReceiveEnchant también existe en la API y tiene su propio
 * getBoost()/setBoost(double), pero el hook original de FusionPlugin no lo
 * usa (solo essence y XP) — se deja fuera aquí por consistencia; añadirlo
 * es trivial si se necesita más adelante (economía "hoes_money").
 */
public class RivalHarvesterHoesListener implements Listener {

    private final BoostManager boostManager;

    public RivalHarvesterHoesListener(BoostManager boostManager) {
        this.boostManager = boostManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEssenceGain(HoeEssenceReceiveEnchantEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "hoes_essence");
        if (boost == 1.0D) return;

        event.setMultiplier(event.getMultiplier() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onXpGain(HoeXPGainEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "hoes_xp");
        if (boost == 1.0D) return;

        event.setMultiplier(event.getMultiplier() * boost);
    }
}
