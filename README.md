# ServerSync

## Info

A Minecraft plugin for **multiserver inventory & data synchronization**.
 
**Supports:**
 * **Latest:** Minecraft 1.20
 * **Previous:** Minecraft 1.20

**Requires:**
 * A **Minecraft server**, running Bukkit/Spigot/Paper/etc.
 * A **MySQL database**, for data storage and synchronization.
 
**Recommended:**
 * A **BungeeCord server network**, for multiserver synchronization

## How to use

Each server on BungeeCord has its own player data, which means inventories and such will be different when traveling to a different server.

The solution to this issue is to use a plugin to synchronize the player data between servers. Unfortunately, I could not find many options for the newest versions of MC.

So I built a separate plugin called *ServerSync* to handle player data synchronization between Minecraft servers.

1. Install the plugin on all servers on your BungeeCord network. 
2. Start & stop each server once to generate the default files.
3. Edit each server's "config.yml" file to set the "server_name" setting to its Bungee server name.
4. Edit each server's "database.yml" file to set the database connection credentials.
5. Restart each server and enjoy!

If it does not work, ensure that the database has a table labeled "playerData" formatted with columns like inventory, etc. If not, use this SQL query to create the table and restart the servers:

``CREATE TABLE playerData (player varchar(255), server varchar(255), health double, air int, hunger int, effects text, inventory text, armor text, enderInventory text, xp float, mode varchar(255))``

## Credits

Developer: **krakenmyboy**

---
