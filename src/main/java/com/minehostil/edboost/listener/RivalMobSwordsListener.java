package com.minehostil.edboost.listener;

import com.minehostil.edboost.manager.BoostManager;
import me.rivaldev.mobsword.rivalmobswords.api.RivalMobKillEvent;
import me.rivaldev.mobsword.rivalmobswords.api.SwordEssenceReceiveEnchantEvent;
import me.rivaldev.mobsword.rivalmobswords.api.SwordXPGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Aplica el boost permanente del jugador a los eventos de RivalMobSwords,
 * las mismas tres economías que ya cubría el hook de FusionPlugin: essence,
 * XP y procboost de la espada.
 *
 * Verificado contra el JAR real (paquete me.rivaldev.mobsword.rivalmobswords.api).
 * Combinación MULTIPLICATIVA en todos los casos:
 * - SwordEssenceReceiveEnchantEvent tiene su propio getBoost()/setBoost(double).
 * - SwordXPGainEvent también tiene getBoost()/setBoost(double) (además de
 *   getXP()/setXP(), pero el hook original de FusionPlugin multiplica el
 *   campo "boost", no el XP directamente — se replica igual aquí).
 * - RivalMobKillEvent no tiene campo "boost" separado, se multiplica
 *   getProcboost()/setProcboost(double) directamente.
 *
 * Economías en config.yml: mobswords_essence, mobswords_xp, mobswords_procboost.
 *
 * SwordLevelUpEvent (notificación de subida de nivel en FusionPlugin) no se
 * replica aquí: en el original es un mensaje de chat ligado al sistema de
 * mensajes de FusionPlugin y al ítem equipado, no una aplicación de boost.
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

        event.setBoost(event.getBoost() * boost);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSwordXpGain(SwordXPGainEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        double boost = boostManager.getBoostValue(player.getUniqueId(), "mobswords_xp");
        if (boost == 1.0D) return;

        event.setBoost(event.getBoost() * boost);
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
