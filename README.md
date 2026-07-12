# Ranks System 

A complete rank management system for PowerNukkitX servers.  
Players can have **one rank** at a time. Each rank can have:
- A **display prefix** (with color codes `&`)
- A list of **permissions** (applied via permission attachments)
- A **default** flag (assigned to new players without a rank)

The prefix appears in:
- The player's **name tag** (above their head)
- The player's **display name** (used in chat)
- **Chat messages** are automatically formatted with the prefix

---

## Features
- ✅ Single rank per player (replaces old rank when giving a new one)
- ✅ Permission attachments per rank
- ✅ Default rank for new players
- ✅ Full command set: create, delete, give, remove, list, info, perms add/remove, setdisplay, reload, help
- ✅ Reload command to refresh config without restart
- ✅ Developer API for other plugins

---

## Installation
1. Download the plugin JAR and place it in your `plugins/` folder.
2. Restart the server or use `/reload` (not recommended) – the plugin will generate default `ranks.yml` and `players.yml` in the plugin's data folder.
3. Configure ranks in `ranks.yml` (see below).
4. Use `/rank reload` to apply changes without restarting.

---

## Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/rank` | Show help | `ranks.command` |
| `/rank help` | Show help | `ranks.command` |
| `/rank create <rank>` | Create a new rank | `ranks.command.create` |
| `/rank delete <rank>` | Delete a rank | `ranks.command.delete` |
| `/rank give <player> <rank>` | Set a player's rank (replaces existing) | `ranks.command.give` |
| `/rank remove <player> <rank>` | Remove a rank from a player | `ranks.command.remove` |
| `/rank list` | List all ranks | `ranks.command.list` |
| `/rank info <rank>` | Show rank details | `ranks.command.info` |
| `/rank perms add <rank> <permission>` | Add a permission to a rank | `ranks.command.perms.add` |
| `/rank perms remove <rank> <permission>` | Remove a permission from a rank | `ranks.command.perms.remove` |
| `/rank setdisplay <rank> <display>` | Set the rank's display prefix (use `&` for colours) | `ranks.command.setdisplay` |
| `/rank reload` | Reload ranks and player data from disk | `ranks.command.reload` |

---

## Permissions
All permissions are listed in `plugin.yml`. Default: **OP only**.

---

## Configuration

### `ranks.yml`
```yaml
ranks:
  default:
    permissions:
      - exampleplugin.helloworld
    default: true
    display: "&7[Default] "
  admin:
    permissions:
      - "*"
    default: false
    display: "&c[Admin] "
```

- **permissions**: list of permission nodes (or `"*"` for all)
- **default**: `true` if this rank should be given to new players
- **display**: prefix shown before the player's name (supports colour codes `&`)

### `players.yml`
Auto‑generated; stores each player's UUID and rank name.
```yaml
players:
  "uuid-here": ["admin"]
```

---

## Developer API
Other plugins can access rank information via the `RanksAPI` class.

### Available Methods
| Method | Description |
|--------|-------------|
| `getPlayerRankName(Player)` | Returns the rank name, or `null` |
| `getPlayerRank(Player)` | Returns the `Rank` object, or `null` |
| `getPlayerRankPrefix(Player)` | Returns the colorized prefix (e.g., `[Admin] `), or `""` |
| `getPlayerDisplayNameWithRank(Player)` | Returns the full display name with prefix |

---

## Using the Ranks API in Your Plugin
To make your plugin react to a player’s rank (e.g., give special effects, block certain actions, or grant abilities), you need to listen to the appropriate PowerNukkitX events and check the player’s rank using the `RanksAPI`.

### EX: Grant Extra Damage Based on Rank

Create a listener that triggers when a player attacks an entity, and deals bonus damage depending on the rank:

```java
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageModifier;
import org.powernukkitx.ranks.RanksAPI;
import org.powernukkitx.ranks.RankManager;

public class RankDamageListener implements Listener {

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Entity target)) return;

        String rankName = RanksAPI.getPlayerRankName(player);
        if (rankName == null) return;

        // Define bonus damage per rank (example)
        double bonus = switch (rankName.toLowerCase()) {
            case "admin" -> 5.0;
            case "moderator" -> 3.0;
            case "vip" -> 2.0;
            default -> 0.0;
        };

        if (bonus > 0) {
            event.setDamage(event.getDamage() + bonus);
            // Optional: send a message or play a particle effect
        }
    }
}
```

### EX: Prevent Breaking Blocks in Certain Ranks

```java
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockBreakEvent;
import org.powernukkitx.ranks.RanksAPI;

public class RankBlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String rankName = RanksAPI.getPlayerRankName(player);
        if ("guest".equalsIgnoreCase(rankName)) {
            event.setCancelled(true);
            player.sendMessage("Guests cannot break blocks!");
        }
    }
}
```

### Register Your Listener

In your plugin’s `onEnable()`:

```java
public void onEnable() {
    getServer().getPluginManager().registerEvents(new RankDamageListener(), this);
    getServer().getPluginManager().registerEvents(new RankBlockListener(), this);
}
```

---

## Support
For issues or suggestions, please open an issue on the repository.

---
