package org.powernukkitx.ranks;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerJoinEvent ;

public class RankListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent  event) {
        RankManager manager = RankManager.getInstance();
        manager.applyDefaultRank(event.getPlayer());
        manager.applyPermissions(event.getPlayer());
        manager.savePlayers();
    }
}