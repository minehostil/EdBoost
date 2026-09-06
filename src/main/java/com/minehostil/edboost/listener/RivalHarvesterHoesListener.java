package com.minehostil.edboost.listener;

import com.minehostil.edboost.manager.BoostManager;
import me.rivaldev.harvesterhoes.api.events.HoeEssenceReceiveEnchantEvent;
import me.rivaldev.harvesterhoes.api.events.HoeMoneyReceiveEnchant;
import me.rivaldev.harvesterhoes.api.events.HoeXPGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Aplica el boost permanente del jugador a los eventos de RivalHarvesterHoes:
 * essence, dinero y XP de la azada.
 *
 * CONVENCIÓN DE CÁLCULO: valor final = valor base * multiplicador total.
 * Ej: essence base 7, boost x2 -> 14. Por eso se multiplica directamente
 * el campo de VALOR de cada evento (getEssence/getMoney/getXP), no un
 * campo intermedio "boost"/"multiplier" — estos eventos exponen ambos,
 * pero usar el valor directo hace que el resultado final sea exactamente
 * base*total sin depender de cómo el plugin combine internamente ese
 * campo intermedio con la cantidad base.
 *
 * Verificado contra el JAR real (paquete me.rivaldev.harvesterhoes.api.events).
 *
 * Economías en config.yml: hoes_essence, hoes_money, hoes_xp.
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

        event.setEssence(event.getEssence() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMoneyGain(HoeMoneyReceiveEnchant event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "hoes_money");
        if (boost == 1.0D) return;

        event.setMoney(event.getMoney() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onXpGain(HoeXPGainEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "hoes_xp");
        if (boost == 1.0D) return;

        event.setXP(event.getXP() * boost);
    }
}
