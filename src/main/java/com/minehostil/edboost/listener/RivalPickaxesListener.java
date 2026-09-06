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
 * Aplica el boost permanente del jugador a los eventos de RivalPickaxes:
 * essence, dinero, XP y procboost del pico.
 *
 * CONVENCIÓN DE CÁLCULO: valor final = valor base * multiplicador total.
 * Ej: essence base 7, boost x2 -> 14. Se multiplica el campo de VALOR
 * directamente (getEssence/getMoney/getXP), no el campo intermedio
 * "boost" que estos eventos también exponen — igual razón que en
 * RivalHarvesterHoesListener.
 *
 * PickaxeEnchantProcBoostEvent es la única excepción: no tiene un campo
 * de "valor" separado, el campo "boost" ES el valor a boostear, así que
 * ahí sí se multiplica getBoost()/setBoost() directamente (misma fórmula,
 * aplicada al único campo disponible).
 *
 * Verificado contra el JAR real (paquete me.rivaldev.pickaxes.api.events).
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

        event.setEssence(event.getEssence() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMoneyGain(PickaxeMoneyReceiveEnchant event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "pickaxes_money");
        if (boost == 1.0D) return;

        event.setMoney(event.getMoney() * boost);
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
