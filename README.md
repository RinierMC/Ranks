# Ranks Plugin for PowerNukkitX

A complete rank management system for PowerNukkitX servers.  
Players can have **one rank** at a time. Each rank can have:

- A **display prefix** (with color codes `&`)
- A list of **permissions** (applied via permission attachments)
- A **default** flag (assigned to new players without a rank)

The prefix appears in:
- The player's **name tag** (above their head)
- The player's **display name** (used in chat)
- **Chat messages** – automatically formatted with the prefix

---

## Features

- ✅ Single rank per player (replaces old rank when giving a new one)
- ✅ Permission attachments per rank
- ✅ Default rank for new players
- ✅ Full command set: create, delete, give, remove, list, info, perms add/remove, setdisplay, reload, help
- ✅ Reload command to refresh config without restart
- ✅ **Pluggable placeholder system** – other plugins can register their own tags (e.g., `{faction}`)
- ✅ Developer API for other plugins

---

## Installation

1. Download the plugin JAR and place it in your `plugins/` folder.
2. Restart the server (or use `/reload` – not recommended). The plugin will generate default `ranks.yml` and `players.yml` in the plugin's data folder.
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
  vip:
    permissions:
      - vip.fly
      - vip.kit
    default: false
    display: "&6[&eVIP&6] "
```

- **permissions**: list of permission nodes (or `"*"` for all)
- **default**: `true` if this rank should be given to new players
- **display**: prefix shown before the player's name (supports colour codes `&` and **placeholders** – see below)

### `players.yml`
Auto‑generated; stores each player's UUID and rank name.
```yaml
players:
  "uuid-here": ["admin"]
```

---

## Placeholder System

The Ranks plugin supports **dynamic placeholders** in the `display` field. Placeholders are written as `{tag}` and are replaced with real values per player.

**Built‑in placeholders:** none by default – but you can register your own (see below).

### Example with Faction plugin

If you have the Factions plugin installed, it automatically registers the following tags:
- `{faction}` – the player's faction name
- `{faction_role}` – the player's role (MEMBER, OFFICER, LEADER)
- `{faction_stars}` – the role symbol (`*`, `**`, `***`)

Then you can use them in `ranks.yml`:
```yaml
display: "&7[{faction}{faction_stars}] &r"
```
This would show `[MyFaction***] PlayerName` for the faction leader.

---

## Adding Custom Placeholders (for Plugin Developers)

Other plugins can add their own placeholders by registering a **placeholder resolver**.  
You can do this **with** or **without** a compile‑time dependency on the Ranks plugin.

### Option A: With compile‑time dependency (recommended for simplicity)

Add the Ranks plugin as a `provided` dependency in your `pom.xml`:
```xml
<dependency>
    <groupId>org.powernukkitx</groupId>
    <artifactId>Ranks</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

Then implement `PlaceholderResolver`:

```java
import org.powernukkitx.Player;
import org.powernukkitx.ranks.PlaceholderResolver;

public class MyPlaceholderResolver implements PlaceholderResolver {
    @Override
    public String resolve(String tag, Player player) {
        if ("myplugin".equalsIgnoreCase(tag)) {
            return "Hello from MyPlugin!";
        }
        return null; // Unknown tag
    }
}
```

Register it in your plugin's `onEnable()`:

```java
import org.powernukkitx.ranks.RanksAPI;

public class MyPlugin extends PluginBase {
    @Override
    public void onEnable() {
        RanksAPI.registerPlaceholderResolver(new MyPlaceholderResolver());
        getLogger().info("Registered placeholders with Ranks plugin.");
    }
}
```

Now server admins can use `{myplugin}` in rank displays.

---

### Option B: Without compile‑time dependency (using reflection)

If you want to avoid a hard dependency, use reflection to load the Ranks API and register the resolver.

**Example from the Factions plugin** – this code works even if Ranks is not installed:

```java
private void registerPlaceholderResolver() {
    try {
        Plugin ranksPlugin = getServer().getPluginManager().getPlugin("Ranks");
        if (ranksPlugin == null) {
            getLogger().info("Ranks plugin not found - placeholders not available.");
            return;
        }

        ClassLoader ranksClassLoader = ranksPlugin.getClass().getClassLoader();
        Class<?> ranksApiClass = Class.forName("org.powernukkitx.ranks.RanksAPI", true, ranksClassLoader);
        Class<?> resolverInterface = Class.forName("org.powernukkitx.ranks.PlaceholderResolver", true, ranksClassLoader);
        Method registerMethod = ranksApiClass.getMethod("registerPlaceholderResolver", resolverInterface);

        // Create a dynamic proxy that implements PlaceholderResolver
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("resolve")) {
                String tag = (String) args[0];
                Player player = (Player) args[1];
                // Your logic to resolve the placeholder
                if ("faction".equalsIgnoreCase(tag)) {
                    return getFactionOf(player);
                }
                return null;
            }
            // Handle equals/hashCode/toString
            if (method.getName().equals("toString")) return "MyResolver";
            if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
            if (method.getName().equals("equals")) return proxy == args[0];
            return null;
        };

        Object resolver = Proxy.newProxyInstance(
                resolverInterface.getClassLoader(),
                new Class<?>[]{resolverInterface},
                handler
        );

        registerMethod.invoke(null, resolver);
        getLogger().info("Registered custom placeholders with Ranks plugin.");
    } catch (ClassNotFoundException e) {
        getLogger().info("Ranks API not found - placeholders not available.");
    } catch (Exception e) {
        getLogger().warning("Failed to register placeholder resolver: " + e.getMessage());
    }
}
```

```java
public void onEnable() {
        getServer().getScheduler().scheduleDelayedTask(this, this::registerPlaceholderResolver, 100);
    }
```

**Important notes for reflection approach:**
- Load the classes using the Ranks plugin's own class loader to avoid `ClassNotFoundException`.
- Handle all proxy methods (`resolve`, `toString`, `hashCode`, `equals`) to avoid unexpected errors.
- Always catch `ClassNotFoundException` – the plugin works fine without Ranks.
- Use a **delayed task** (e.g., 40–100 ticks) to register the resolver, as Ranks may enable after your plugin.
- This will enables your plugin to run without the Ranks Plugin.

---

## Developer API

Other plugins can also **read** rank information using the `RanksAPI` class.

### Available Methods

| Method | Description |
|--------|-------------|
| `String getPlayerRankName(Player)` | Returns the rank name, or `null` |
| `Rank getPlayerRank(Player)` | Returns the `Rank` object, or `null` |
| `String getPlayerRankPrefix(Player)` | Returns the colorized prefix (e.g., `[Admin] `), or `""` |
| `String getPlayerDisplayNameWithRank(Player)` | Returns the full display name with prefix |
| `void registerPlaceholderResolver(PlaceholderResolver)` | Register a custom placeholder resolver |

---

## Using the Ranks API in Your Plugin (Examples)

### Example: Grant Extra Damage Based on Rank

```java
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.ranks.RanksAPI;

public class RankDamageListener implements Listener {
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        String rankName = RanksAPI.getPlayerRankName(player);
        if (rankName == null) return;
        double bonus = switch (rankName.toLowerCase()) {
            case "admin" -> 5.0;
            case "moderator" -> 3.0;
            case "vip" -> 2.0;
            default -> 0.0;
        };
        if (bonus > 0) {
            event.setDamage(event.getDamage() + bonus);
        }
    }
}
```

### Example: Prevent Breaking Blocks for a Specific Rank

```java
@EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    String rank = RanksAPI.getPlayerRankName(player);
    if ("guest".equalsIgnoreCase(rank)) {
        event.setCancelled(true);
        player.sendMessage("Guests cannot break blocks!");
    }
}
```

---

## Support

For issues or suggestions, please open an issue on the repository.
