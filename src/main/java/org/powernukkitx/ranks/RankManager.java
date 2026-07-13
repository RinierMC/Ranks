package org.powernukkitx.ranks;

import org.powernukkitx.Player;
import org.powernukkitx.permission.PermissionAttachment;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.ConfigSection;
import org.powernukkitx.utils.TextFormat;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RankManager {
    private static RankManager instance;
    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final Map<UUID, String> playerRank = new ConcurrentHashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    private Config rankConfig;
    private Config playerConfig;

    private RankManager() {}

    public static RankManager getInstance() {
        if (instance == null) instance = new RankManager();
        return instance;
    }

    public void load() {
        File rankFile = new File(Main.INSTANCE.getDataFolder(), "ranks.yml");
        rankConfig = new Config(rankFile, Config.YAML);
        ranks.clear();

        if (rankFile.exists() && !rankConfig.getRootSection().isEmpty()) {
            ConfigSection section = rankConfig.getSection("ranks");
            for (String name : section.getKeys(false)) {
                ConfigSection data = section.getSection(name);
                List<String> perms = data.getStringList("permissions");
                boolean isDefault = data.getBoolean("default", false);
                String display = data.getString("display", "");
                ranks.put(name, new Rank(name, perms, isDefault, display));
            }
        } else {
            Rank defaultRank = new Rank("default", Collections.singletonList("exampleplugin.helloworld"), true, "&7[Default] ");
            ranks.put("default", defaultRank);
            saveRanks();
        }

        File playerFile = new File(Main.INSTANCE.getDataFolder(), "players.yml");
        playerConfig = new Config(playerFile, Config.YAML);
        playerRank.clear();

        if (playerFile.exists()) {
            ConfigSection section = playerConfig.getSection("players");
            for (String uuidStr : section.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> list = section.getStringList(uuidStr);
                if (!list.isEmpty()) {
                    playerRank.put(uuid, list.get(0));
                }
            }
        }

        Main.INSTANCE.getServer().getOnlinePlayers().values().forEach(this::applyPermissions);
    }

    public void save() {
        saveRanks();
        savePlayers();
    }

    public void saveRanks() {
        ConfigSection root = new ConfigSection();
        ConfigSection rankSection = new ConfigSection();
        for (Rank rank : ranks.values()) {
            ConfigSection data = new ConfigSection();
            data.put("permissions", rank.permissions);
            data.put("default", rank.isDefault);
            data.put("display", rank.displayName);
            rankSection.put(rank.name, data);
        }
        root.put("ranks", rankSection);
        rankConfig.setAll(root);
        rankConfig.save();
    }

    public void savePlayers() {
        ConfigSection root = new ConfigSection();
        ConfigSection playerSection = new ConfigSection();
        for (Map.Entry<UUID, String> entry : playerRank.entrySet()) {
            List<String> list = new ArrayList<>();
            if (entry.getValue() != null) list.add(entry.getValue());
            playerSection.put(entry.getKey().toString(), list);
        }
        root.put("players", playerSection);
        playerConfig.setAll(root);
        playerConfig.save();
    }

    private final List<PlaceholderResolver> resolvers = new ArrayList<>();

    public void registerPlaceholderResolver(PlaceholderResolver resolver) {
        resolvers.add(resolver);
    }

    public String replacePlaceholders(String text, Player player) {
        if (text == null || text.isEmpty()) return text;
        String result = text;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{([^}]+)\\}").matcher(text);
        while (m.find()) {
            String tag = m.group(1);
            String replacement = null;
            for (PlaceholderResolver resolver : resolvers) {
                replacement = resolver.resolve(tag, player);
                if (replacement != null) break;
            }
            if (replacement != null) {
                result = result.replace("{" + tag + "}", replacement);
            }
        }
        return result;
    }

    public boolean createRank(String name) {
        if (ranks.containsKey(name)) return false;
        ranks.put(name, new Rank(name, new ArrayList<>(), false, ""));
        saveRanks();
        return true;
    }

    public boolean deleteRank(String name) {
        if (!ranks.containsKey(name)) return false;
        for (UUID uuid : new ArrayList<>(playerRank.keySet())) {
            if (name.equals(playerRank.get(uuid))) {
                playerRank.remove(uuid);
            }
        }
        ranks.remove(name);
        saveRanks();
        savePlayers();
        Main.INSTANCE.getServer().getOnlinePlayers().values().forEach(this::applyPermissions);
        return true;
    }

    public boolean giveRank(Player player, String rankName) {
        if (!ranks.containsKey(rankName)) return false;
        UUID uuid = player.getUniqueId();
        String current = playerRank.get(uuid);
        if (rankName.equals(current)) return false;
        playerRank.put(uuid, rankName);
        savePlayers();
        applyPermissions(player);
        return true;
    }

    public boolean removeRank(Player player, String rankName) {
        UUID uuid = player.getUniqueId();
        String current = playerRank.get(uuid);
        if (current == null || !current.equals(rankName)) return false;
        playerRank.remove(uuid);
        savePlayers();
        applyPermissions(player);
        return true;
    }

    public String getPlayerRankName(Player player) {
        return playerRank.get(player.getUniqueId());
    }

    public Collection<Rank> getAllRanks() {
        return Collections.unmodifiableCollection(ranks.values());
    }

    public Rank getRank(String name) {
        return ranks.get(name);
    }

    public boolean setRankDisplay(String rankName, String display) {
        Rank rank = ranks.get(rankName);
        if (rank == null) return false;
        rank.displayName = display;
        saveRanks();
        for (Player p : Main.INSTANCE.getServer().getOnlinePlayers().values()) {
            if (rankName.equals(getPlayerRankName(p))) {
                applyPermissions(p);
            }
        }
        return true;
    }

    public void applyPermissions(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment old = attachments.remove(uuid);
        if (old != null) player.removeAttachment(old);

        PermissionAttachment attachment = player.addAttachment(Main.INSTANCE);
        attachments.put(uuid, attachment);

        String rankName = playerRank.get(uuid);
        Rank rank = rankName != null ? ranks.get(rankName) : null;
        String rawPrefix = "";
        if (rank != null) {
            if (!rank.displayName.isEmpty()) {
                rawPrefix = rank.displayName;
            }
        }
        String prefix = "";
        if (!rawPrefix.isEmpty()) {
            String replaced = replacePlaceholders(rawPrefix, player);
            prefix = TextFormat.colorize(replaced).trim();
        }

        String displayName = prefix.isEmpty() ? player.getName() : prefix + " " + player.getName();
        player.setNameTag(displayName);
    }

    public void applyDefaultRank(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerRank.containsKey(uuid)) {
            for (Rank rank : ranks.values()) {
                if (rank.isDefault) {
                    giveRank(player, rank.name);
                    break;
                }
            }
        }
    }

    public static class Rank {
        private final String name;
        private final List<String> permissions;
        private final boolean isDefault;
        private String displayName;

        public Rank(String name, List<String> permissions, boolean isDefault, String displayName) {
            this.name = name;
            this.permissions = permissions;
            this.isDefault = isDefault;
            this.displayName = displayName;
        }

        public String getName() { return name; }
        public List<String> getPermissions() { return permissions; }
        public boolean isDefault() { return isDefault; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}