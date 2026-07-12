package org.powernukkitx.ranks;

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginCommand;
import org.powernukkitx.command.data.CommandEnum;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.command.tree.ParamList;
import org.powernukkitx.command.tree.node.PlayersNode;
import org.powernukkitx.command.utils.CommandLogger;
import org.powernukkitx.utils.TextFormat;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;

import java.util.List;
import java.util.Map;

public class RanksCommand extends PluginCommand<Main> {

    private static final String PREFIX = TextFormat.BOLD + "" + TextFormat.GRAY + "(" + TextFormat.RESET + TextFormat.GOLD + "!" + TextFormat.BOLD + TextFormat.GRAY + ")" + TextFormat.RESET + " ";

    public RanksCommand() {
        super("rank", "Ranks management", Main.INSTANCE);
        this.setPermission("ranks.command");
        this.getCommandParameters().clear();

        // No-args help pattern
        this.getCommandParameters().put("help", new CommandParameter[0]);

        // Explicit help subcommand
        this.getCommandParameters().put("help2", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("help", "help"))
        });

        // reload
        this.getCommandParameters().put("reload", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("reload", "reload"))
        });

        // create <rank>
        this.getCommandParameters().put("create", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("create", "create")),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT)
        });
        // delete <rank>
        this.getCommandParameters().put("delete", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("delete", "delete")),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT)
        });
        // give <player> <rank>   (sets the player's rank, replacing any existing)
        this.getCommandParameters().put("give", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("give", "give")),
                CommandParameter.newType("player", false, CommandParamType.SELECTION, new PlayersNode()),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT)
        });
        // remove <player> <rank> (removes that specific rank if it's the player's current rank)
        this.getCommandParameters().put("remove", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("remove", "remove")),
                CommandParameter.newType("player", false, CommandParamType.SELECTION, new PlayersNode()),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT)
        });
        // list
        this.getCommandParameters().put("list", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("list", "list"))
        });
        // info <rank>
        this.getCommandParameters().put("info", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("info", "info")),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT)
        });
        // perms add <rank> <permission>
        this.getCommandParameters().put("permsadd", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("perms", "perms")),
                CommandParameter.newEnum("subaction", false, new CommandEnum("add", "add")),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT),
                CommandParameter.newType("permission", false, CommandParamType.RAW_TEXT)
        });
        // perms remove <rank> <permission>
        this.getCommandParameters().put("permsremove", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("perms", "perms")),
                CommandParameter.newEnum("subaction", false, new CommandEnum("remove", "remove")),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT),
                CommandParameter.newType("permission", false, CommandParamType.RAW_TEXT)
        });
        // setdisplay <rank> <display>
        this.getCommandParameters().put("setdisplay", new CommandParameter[]{
                CommandParameter.newEnum("action", false, new CommandEnum("setdisplay", "setdisplay")),
                CommandParameter.newType("rank", false, CommandParamType.RAW_TEXT),
                CommandParameter.newType("display", false, CommandParamType.RAW_TEXT)
        });

        this.enableParamTree();
    }

    @Override
    public int execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) {
        RankManager manager = RankManager.getInstance();
        var list = result.getValue();

        // Handle help (no args or explicit help)
        if (result.getKey().equals("help") || result.getKey().equals("help2")) {
            sendHelp(sender, log);
            return 1;
        }

        switch (result.getKey()) {
            case "reload" -> {
                if (!sender.hasPermission("ranks.command.reload")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                manager.load();
                for (Player p : Main.INSTANCE.getServer().getOnlinePlayers().values()) {
                    manager.applyPermissions(p);
                }
                log.addMessage(PREFIX + TextFormat.GREEN + "Ranks reloaded successfully.").output();
            }
            case "create" -> {
                if (!sender.hasPermission("ranks.command.create")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                String rank = list.getResult(1);
                if (manager.createRank(rank)) {
                    log.addMessage(PREFIX + TextFormat.GREEN + "Rank '" + rank + "' created.").output();
                } else {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank already exists.").output();
                }
            }
            case "delete" -> {
                if (!sender.hasPermission("ranks.command.delete")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                String rank = list.getResult(1);
                if (manager.deleteRank(rank)) {
                    log.addMessage(PREFIX + TextFormat.GREEN + "Rank '" + rank + "' deleted.").output();
                } else {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank not found.").output();
                }
            }
            case "give" -> {
                if (!sender.hasPermission("ranks.command.give")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                List<Player> players = list.getResult(1);
                String rank = list.getResult(2);
                if (manager.getRank(rank) == null) {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank not found.").output();
                    return 0;
                }
                for (Player p : players) {
                    if (manager.giveRank(p, rank)) {
                        log.addMessage(PREFIX + TextFormat.GREEN + "Set rank '" + rank + "' for " + p.getName()).output();
                    } else {
                        log.addMessage(PREFIX + TextFormat.YELLOW + p.getName() + " already has that rank.").output();
                    }
                }
            }
            case "remove" -> {
                if (!sender.hasPermission("ranks.command.remove")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                List<Player> players = list.getResult(1);
                String rank = list.getResult(2);
                if (manager.getRank(rank) == null) {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank not found.").output();
                    return 0;
                }
                for (Player p : players) {
                    if (manager.removeRank(p, rank)) {
                        log.addMessage(PREFIX + TextFormat.GREEN + "Removed rank '" + rank + "' from " + p.getName()).output();
                    } else {
                        log.addMessage(PREFIX + TextFormat.YELLOW + p.getName() + " does not have that rank.").output();
                    }
                }
            }
            case "list" -> {
                if (!sender.hasPermission("ranks.command.list")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                StringBuilder sb = new StringBuilder(TextFormat.AQUA + "Ranks: ");
                for (RankManager.Rank r : manager.getAllRanks()) {
                    sb.append(TextFormat.WHITE).append(r.getName()).append(TextFormat.GRAY).append(", ");
                }
                log.addMessage(PREFIX + sb.toString()).output();
            }
            case "info" -> {
                if (!sender.hasPermission("ranks.command.info")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                String rank = list.getResult(1);
                RankManager.Rank r = manager.getRank(rank);
                if (r == null) {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank not found.").output();
                    return 0;
                }
                log.addMessage(PREFIX + TextFormat.YELLOW + "Rank: " + TextFormat.WHITE + r.getName() +
                        TextFormat.YELLOW + " (default: " + TextFormat.WHITE + r.isDefault() + TextFormat.YELLOW + ")").output();
                log.addMessage(PREFIX + TextFormat.YELLOW + "Display: " + TextFormat.WHITE + r.getDisplayName()).output();
                log.addMessage(PREFIX + TextFormat.YELLOW + "Permissions: " + TextFormat.WHITE +
                        String.join(", ", r.getPermissions())).output();
            }
            case "permsadd" -> {
                if (!sender.hasPermission("ranks.command.perms.add")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                String rank = list.getResult(2);
                String perm = list.getResult(3);
                RankManager.Rank r = manager.getRank(rank);
                if (r == null) {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank not found.").output();
                    return 0;
                }
                if (r.getPermissions().contains(perm)) {
                    log.addMessage(PREFIX + TextFormat.YELLOW + "Permission already exists.").output();
                } else {
                    r.getPermissions().add(perm);
                    manager.saveRanks();
                    for (Player p : Main.INSTANCE.getServer().getOnlinePlayers().values()) {
                        if (rank.equals(manager.getPlayerRankName(p))) {
                            manager.applyPermissions(p);
                        }
                    }
                    log.addMessage(PREFIX + TextFormat.GREEN + "Permission added.").output();
                }
            }
            case "permsremove" -> {
                if (!sender.hasPermission("ranks.command.perms.remove")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                String rank = list.getResult(2);
                String perm = list.getResult(3);
                RankManager.Rank r = manager.getRank(rank);
                if (r == null) {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank not found.").output();
                    return 0;
                }
                if (!r.getPermissions().contains(perm)) {
                    log.addMessage(PREFIX + TextFormat.YELLOW + "Permission not found.").output();
                } else {
                    r.getPermissions().remove(perm);
                    manager.saveRanks();
                    for (Player p : Main.INSTANCE.getServer().getOnlinePlayers().values()) {
                        if (rank.equals(manager.getPlayerRankName(p))) {
                            manager.applyPermissions(p);
                        }
                    }
                    log.addMessage(PREFIX + TextFormat.GREEN + "Permission removed.").output();
                }
            }
            case "setdisplay" -> {
                if (!sender.hasPermission("ranks.command.setdisplay")) {
                    log.addMessage(PREFIX + TextFormat.RED + "You don't have permission.").output();
                    return 0;
                }
                String rank = list.getResult(1);
                String display = list.getResult(2);
                if (manager.setRankDisplay(rank, display)) {
                    log.addMessage(PREFIX + TextFormat.GREEN + "Display name for rank '" + rank + "' set to '" + display + "'").output();
                } else {
                    log.addMessage(PREFIX + TextFormat.RED + "Rank not found.").output();
                }
            }
            default -> {
                sendHelp(sender, log);
            }
        }
        return 1;
    }

    private void sendHelp(CommandSender sender, CommandLogger log) {
        log.addMessage(PREFIX + TextFormat.YELLOW + "Available subcommands:");
        log.addMessage(TextFormat.WHITE + "  /rank create <rank> " + TextFormat.GRAY + "- Create a new rank");
        log.addMessage(TextFormat.WHITE + "  /rank delete <rank> " + TextFormat.GRAY + "- Delete a rank");
        log.addMessage(TextFormat.WHITE + "  /rank give <player> <rank> " + TextFormat.GRAY + "- Set a player's rank (replaces any existing)");
        log.addMessage(TextFormat.WHITE + "  /rank remove <player> <rank> " + TextFormat.GRAY + "- Remove a rank from a player");
        log.addMessage(TextFormat.WHITE + "  /rank list " + TextFormat.GRAY + "- List all ranks");
        log.addMessage(TextFormat.WHITE + "  /rank info <rank> " + TextFormat.GRAY + "- View rank details");
        log.addMessage(TextFormat.WHITE + "  /rank perms add <rank> <permission> " + TextFormat.GRAY + "- Add a permission to a rank");
        log.addMessage(TextFormat.WHITE + "  /rank perms remove <rank> <permission> " + TextFormat.GRAY + "- Remove a permission from a rank");
        log.addMessage(TextFormat.WHITE + "  /rank setdisplay <rank> <display> " + TextFormat.GRAY + "- Set rank display name (use & for colors)");
        log.addMessage(TextFormat.WHITE + "  /rank reload " + TextFormat.GRAY + "- Reload ranks and player data from disk");
        log.addMessage(TextFormat.WHITE + "  /rank help " + TextFormat.GRAY + "- Show this help").output();
    }
}