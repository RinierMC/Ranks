package org.powernukkitx.ranks;

import org.powernukkitx.Player;
import org.powernukkitx.utils.TextFormat;

import javax.annotation.Nullable;

public class RanksAPI {

    /**
     * Get the rank name of a player.
     *
     * @param player the player
     * @return rank name, or null if player has no rank
     */
    @Nullable
    public static String getPlayerRankName(Player player) {
        return RankManager.getInstance().getPlayerRankName(player);
    }

    /**
     * Get the rank object of a player.
     *
     * @param player the player
     * @return Rank object, or null if player has no rank
     */
    @Nullable
    public static RankManager.Rank getPlayerRank(Player player) {
        String rankName = getPlayerRankName(player);
        if (rankName == null) return null;
        return RankManager.getInstance().getRank(rankName);
    }

    /**
     * Get the display prefix of a player's rank (colorized).
     *
     * @param player the player
     * @return prefix string (e.g., "[Admin] "), or empty string if no rank
     */
    public static String getPlayerRankPrefix(Player player) {
        RankManager.Rank rank = getPlayerRank(player);
        if (rank == null) return "";
        return TextFormat.colorize(rank.getDisplayName());
    }

    /**
     * Get the display name including rank prefix for a player.
     *
     * @param player the player
     * @return full display name (e.g., "[Admin] Steve"), or just the player's name if no rank
     */
    public static String getPlayerDisplayNameWithRank(Player player) {
        String prefix = getPlayerRankPrefix(player);
        if (prefix.isEmpty()) return player.getName();
        return prefix + " " + player.getName();
    }
}