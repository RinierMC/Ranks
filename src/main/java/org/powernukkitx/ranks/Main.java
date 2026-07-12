package org.powernukkitx.ranks;

import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.utils.TextFormat;

public class Main extends PluginBase {
    public static Main INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        getLogger().info(TextFormat.DARK_GREEN + "Ranks plugin enabled!");

        RankManager.getInstance().load();

        getServer().getCommandMap().register("rank", new RanksCommand());

        getServer().getPluginManager().registerEvents(new RankListener(), this);
        getServer().getPluginManager().registerEvents(new RankChatListener(), this);
    }

    @Override
    public void onDisable() {
        RankManager.getInstance().save();
        getLogger().info(TextFormat.DARK_RED + "Ranks plugin disabled!");
    }
}