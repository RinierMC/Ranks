package org.powernukkitx.ranks;

import org.powernukkitx.Player;

@FunctionalInterface
public interface PlaceholderResolver {
    /**
     * Resolves a placeholder tag (without braces, e.g. "faction") for a given player.
     * @param tag the placeholder name (e.g., "faction")
     * @param player the player
     * @return the replacement string, or null if this resolver cannot handle the tag
     */
    String resolve(String tag, Player player);
}