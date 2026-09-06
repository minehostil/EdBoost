package com.minehostil.edboost.listener;

import com.minehostil.edboost.manager.BoostManager;
import me.rivaldev.pickaxes.api.events.PickaxeEnchantProcBoostEvent;
import me.rivaldev.pickaxes.api.events.PickaxeEssenceReceiveEnchantEvent;
import me.rivaldev.pickaxes.api.events.PickaxeMoneyReceiveEnchant;
import me.rivaldev.pickaxes.api.events.PickaxeXPGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Aplica el boost permanente del jugador a los eventos de RivalPickaxes,
 * las mismas cuatro economías que ya cubría el hook de FusionPlugin:
 * essence, money, XP y procboost del pico.
 *
 * Verificado contra el JAR real (paquete me.rivaldev.pickaxes.api.events).
 * Combinación MULTIPLICATIVA en todos los casos (igual que FusionPlugin):
 * - PickaxeEssenceReceiveEnchantEvent / PickaxeMoneyReceiveEnchant /
 *   PickaxeEnchantProcBoostEvent exponen su propio getBoost()/setBoost(double)
 *   (NO tienen getMultiplier(), a diferencia de RivalHarvesterHoes).
 * - PickaxeXPGainEvent NO tiene campo "boost" separado — solo getXP()/setXP(),
 *   así que ahí se multiplica el XP directamente.
 *
 * Economías en config.yml: pickaxes_essence, pickaxes_money, pickaxes_xp,
 * pickaxes_procboost.
 */
public class RivalPickaxesListener implements Listener {

    private final BoostManager boostManager;

    public RivalPickaxesListener(BoostManager boostManager) {
        this.boostManager = boostManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEssenceGain(PickaxeEssenceReceiveEnchantEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "pickaxes_essence");
        if (boost == 1.0D) return;

        event.setBoost(event.getBoost() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMoneyGain(PickaxeMoneyReceiveEnchant event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "pickaxes_money");
        if (boost == 1.0D) return;

        event.setBoost(event.getBoost() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onXpGain(PickaxeXPGainEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "pickaxes_xp");
        if (boost == 1.0D) return;

        event.setXP(event.getXP() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProcBoost(PickaxeEnchantProcBoostEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "pickaxes_procboost");
        if (boost == 1.0D) return;

        event.setBoost(event.getBoost() * boost);
    }
}
