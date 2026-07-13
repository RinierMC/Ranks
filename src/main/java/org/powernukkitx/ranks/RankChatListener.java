package org.powernukkitx.ranks;

import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.utils.TextFormat;

public class RankChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        RankManager manager = RankManager.getInstance();
        String rankName = manager.getPlayerRankName(player);
        if (rankName != null) {
            RankManager.Rank rank = manager.getRank(rankName);
            if (rank != null && !rank.getDisplayName().isEmpty()) {
                String raw = rank.getDisplayName().trim();
                String replaced = manager.replacePlaceholders(raw, player);
                String prefix = TextFormat.colorize(replaced);
                event.setFormat(prefix + " " + TextFormat.RESET + player.getName() + " - " + event.getMessage());
            }
        }
    }
}