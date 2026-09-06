package com.minehostil.edboost.listener;

import com.minehostil.edboost.manager.BoostManager;
import me.rivaldev.mobsword.rivalmobswords.api.RivalMobKillEvent;
import me.rivaldev.mobsword.rivalmobswords.api.SwordEssenceReceiveEnchantEvent;
import me.rivaldev.mobsword.rivalmobswords.api.SwordMoneyReceiveEvent;
import me.rivaldev.mobsword.rivalmobswords.api.SwordXPGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Aplica el boost permanente del jugador a los eventos de RivalMobSwords:
 * essence, dinero, XP y procboost de la espada.
 *
 * CONVENCIÓN DE CÁLCULO: valor final = valor base * multiplicador total.
 * Ej: essence base 7, boost x2 -> 14. Se multiplica el campo de VALOR
 * directamente (getEssence/getMoney/getXP), no el campo intermedio
 * "boost" que estos eventos también exponen — misma razón que en los
 * otros dos listeners de Rival.
 *
 * RivalMobKillEvent es la excepción: no tiene un campo de "valor"
 * separado, el campo "procboost" ES el valor a boostear, así que ahí se
 * multiplica getProcboost()/setProcboost() directamente.
 *
 * Verificado contra el JAR real (paquete me.rivaldev.mobsword.rivalmobswords.api).
 *
 * Economías en config.yml: mobswords_essence, mobswords_money,
 * mobswords_xp, mobswords_procboost.
 */
public class RivalMobSwordsListener implements Listener {

    private final BoostManager boostManager;

    public RivalMobSwordsListener(BoostManager boostManager) {
        this.boostManager = boostManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSwordEssenceGain(SwordEssenceReceiveEnchantEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "mobswords_essence");
        if (boost == 1.0D) return;

        event.setEssence(event.getEssence() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSwordMoneyGain(SwordMoneyReceiveEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "mobswords_money");
        if (boost == 1.0D) return;

        event.setMoney(event.getMoney() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSwordXpGain(SwordXPGainEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "mobswords_xp");
        if (boost == 1.0D) return;

        event.setXP(event.getXP() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobKill(RivalMobKillEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "mobswords_procboost");
        if (boost == 1.0D) return;

        event.setProcboost(event.getProcboost() * boost);
    }
}
