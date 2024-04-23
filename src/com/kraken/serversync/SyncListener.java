package com.kraken.serversync;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

public class SyncListener implements Listener {
	
	private ServerSync plugin;
	
	//Constructor
	public SyncListener(ServerSync plugin) {
		this.plugin = plugin;
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		boolean debugMode = plugin.options.get("debug_mode");
        	
		//Get the database connection
		Database db = plugin.getDatabase();
		Connection conn = db.getConnection();
		
		//Player data
		Player player = e.getPlayer();
		String pId = player.getUniqueId().toString();
		String server = plugin.getServerName();
		String lastServer = server;
		
		//Player's last server query
		lastServer = getPlayerLastServer(conn, player);
		
		//Check if player has data in the database
		if (lastServer == null) {
			if (debugMode) plugin.getLogger().info("No player data found.");
			db.closeConnection();
			return;
		}
		
		//Check if player's server has changed
		if (lastServer.equalsIgnoreCase(server)) {
			if (debugMode) plugin.getLogger().info("Player data found, but server has not changed. Clearing data...");
			deletePlayerData(conn, player);
			db.closeConnection();
			return;
		}
        	
		//More player data
		Double health = player.getHealth();
		int air = player.getRemainingAir();
		int hunger = player.getFoodLevel();
		float xp = player.getExp();
		GameMode gameMode = player.getGameMode();
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		
		//Inventory data
		Inventory inv = player.getInventory();
		String[] contentsStr = BukkitSerialization.playerInventoryToBase64((PlayerInventory) inv);
		String invStr = contentsStr[0];
		String armorStr = contentsStr[1];
		String enderInvStr = BukkitSerialization.itemStackArrayToBase64(player.getEnderChest().getContents());
		
		//Get the player's data from the database
		String queryData = "SELECT health, air, hunger, effects, inventory, armor, enderInventory, xp, mode FROM playerData WHERE player=?";
		
		try (PreparedStatement getData = conn.prepareStatement(queryData)) {
			
			if (debugMode) plugin.getLogger().info("Attempting to get player data from database...");
			
			getData.setString(1, pId);
			ResultSet rs = getData.executeQuery();
			
			while (rs.next()) {
		
				health = rs.getDouble(1);
				air = rs.getInt(2);
				hunger = rs.getInt(3);
				String effectsStr = rs.getString(4);
				effects = getPotionEffects(effectsStr);
				invStr = rs.getString(5);
				armorStr = rs.getString(6);
				enderInvStr = rs.getString(7);
				xp = (float) rs.getFloat(8);
				String mode = rs.getString(9);
				gameMode = GameMode.valueOf(mode);
				
			}
			
			if (debugMode) plugin.getLogger().info("Player data retrieved from database.");
			
		} catch (SQLException e1) {
			if (debugMode) plugin.getLogger().info("Exception thrown while getting player data from database.");
			e1.printStackTrace();
		}

		//Set the player data accordingly
		setPlayerData(player, health, air, hunger, effects, invStr, armorStr, enderInvStr, xp, gameMode);
		
		//Remove the player's data from the database
		deletePlayerData(conn, player);
		
		//Close the connection
		db.closeConnection();
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		
		boolean debugMode = plugin.options.get("debug_mode");
		
		//Player data
		Player player = e.getPlayer();
		String pId = player.getUniqueId().toString();
		String server = plugin.getServerName();
		
		//Get the database info
		Database db = plugin.getDatabase();
		Connection conn = db.getConnection();
		
		//Set player data to database
		String queryStats = "INSERT INTO playerData(player, server, health, air, hunger, effects, inventory, armor, enderInventory, xp, mode) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try (PreparedStatement putData = conn.prepareStatement(queryStats)) {
			
			if (debugMode) plugin.getLogger().info("Attempting to insert player data into database...");

			//More player data
			Double health = player.getHealth();
			int air = player.getRemainingAir();
			int hunger = player.getFoodLevel();
			float xp = player.getLevel() + player.getExp();
			String mode = player.getGameMode().toString();
			
			Collection<PotionEffect> effects = player.getActivePotionEffects();
			String effectsStr = getEffectsStr(effects);
			
			String invStr = BukkitSerialization.itemStackArrayToBase64(player.getInventory().getContents());
			String armorStr = BukkitSerialization.itemStackArrayToBase64(player.getInventory().getArmorContents());
			String enderInvStr = BukkitSerialization.itemStackArrayToBase64(player.getEnderChest().getContents());
			
			putData.setString(1, pId);
			putData.setString(2, server);
			putData.setDouble(3, health);
			putData.setInt(4, air);
			putData.setInt(5, hunger);
			putData.setString(6, effectsStr);
			putData.setString(7, invStr);
			putData.setString(8, armorStr);
			putData.setString(9, enderInvStr);
			putData.setFloat(10, xp);
			putData.setString(11, mode);
			
			putData.executeUpdate();
			
			if (debugMode) plugin.getLogger().info("Player data inserted into database.");
			
		} catch (SQLException e1) {
			if (debugMode) plugin.getLogger().info("Exception thrown while inserting player data into database.");
			e1.printStackTrace();
		}
		
		//Close the connection
		db.closeConnection();
		
	}
	
	//Delete player data from database
	public void deletePlayerData(Connection conn, Player player) {
		
		boolean debugMode = plugin.options.get("debug_mode");
		
		String queryDelete = "DELETE FROM playerData WHERE player=?";
		
		try (PreparedStatement deleteData = conn.prepareStatement(queryDelete)) {
			String pId = player.getUniqueId().toString();
			deleteData.setString(1, pId);
			deleteData.executeUpdate();
		} catch (SQLException e1) {
			if (debugMode) plugin.getLogger().info("Error removing player data from database.");
			e1.printStackTrace();
		}
		
	}
	
	//Sync a player to the data specified
	public void setPlayerData(Player player, Double health, int air, int hunger, Collection<PotionEffect> effects, String invStr, String armorStr, String enderInvStr, float xp, GameMode gameMode) {
		
		//Set player stats
		player.setHealth(health);
		player.setRemainingAir(air);
		player.setFoodLevel(hunger);
		int level = (int) xp;
		player.setExp(xp - level);
		player.setLevel(level);
		
		//Set the player's potion effects
		Collection<PotionEffect> activeEffects = player.getActivePotionEffects();
		
		for (PotionEffect effect : activeEffects) {
			player.removePotionEffect(effect.getType());
		}
		
		player.addPotionEffects(effects);
		
		//Set player inventory
		setPlayerInv(player, invStr, armorStr, enderInvStr);
		
		//Set the player's game mode
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				player.setGameMode(gameMode);
			}
        }, 20);
		
	}
	
	//Get a player's last server connected to
	public String getPlayerLastServer(Connection conn, Player player) {
		
		boolean debugMode = plugin.options.get("debug_mode");
		
		String lastServer = null;
		
		String queryServer = "SELECT server FROM playerData WHERE player=?";
		
		try (PreparedStatement getServer = conn.prepareStatement(queryServer)) {
			
			if (debugMode) plugin.getLogger().info("Attempting to get player's last server from database...");
			
			String pId = player.getUniqueId().toString();
			getServer.setString(1, pId);
			ResultSet rs = getServer.executeQuery();
			
			while (rs.next()) {
				lastServer = rs.getString("server");
			}
			
			if (debugMode) plugin.getLogger().info("Player's last server retrieved from database: " + lastServer);
			
		} catch (SQLException e1) {
			if (debugMode) plugin.getLogger().info("Error getting player's last server from database.");
			e1.printStackTrace();
		}
		
		return lastServer;
		
	}
	
	public Collection<PotionEffect> getPotionEffects(String effectsStr) {
		
		boolean debugMode = plugin.options.get("debug_mode");
		
		String[] effectsSplitter = effectsStr.split(";");
		
		Collection<PotionEffect> effects = new ArrayList<PotionEffect>();
		
		for (String effect : effectsSplitter) {
			
			if (effect.split(",").length < 3) {
				return effects;
			}
			
			String[] effectParts = effect.split(",");
			String type = effectParts[0];
			String duration = effectParts[1];
			String amplifier = effectParts[2];
			
			PotionEffectType effectType = PotionEffectType.getByName(type.toUpperCase());
			int effectDuration = Integer.parseInt(duration);
			int effectAmplifier = Integer.parseInt(amplifier);
			
			PotionEffect potionEffect = new PotionEffect(effectType, effectDuration, effectAmplifier);
			
			if (potionEffect.getType() != null) {
				effects.add(potionEffect);
			} else {
				if (debugMode) plugin.getLogger().info("Error setting potion effect to player.");
			}
			
		}
		
		return effects;
		
	}
	
	public String getEffectsStr(Collection<PotionEffect> effects) {
		
		String effectsStr = "";
		int effectsCounter = 1;
		
		for (PotionEffect effect : effects) {
			
			effectsStr += effect.getType().getName() + "," + effect.getDuration() + "," + effect.getAmplifier();
			if (effectsCounter < effects.size()) {
				effectsStr += ";";
			}
			effectsCounter++;
			
		}
		
		return effectsStr;
		
	}
	
	//Set the player's inventory from string
	public void setPlayerInv(Player player, String invStr, String armorStr, String enderInvStr) {
		
		boolean debugMode = plugin.options.get("debug_mode");
		
		//Get the inventory contents from base 64 string
		try {
			
			if (debugMode) plugin.getLogger().info("Setting player inventory from string...");
			
			ItemStack[] contents = BukkitSerialization.itemStackArrayFromBase64(invStr);
			player.getInventory().setContents(contents);
			
			ItemStack[] armorContents = BukkitSerialization.itemStackArrayFromBase64(armorStr);
			player.getInventory().setArmorContents(armorContents);
			
			ItemStack[] enderContents = BukkitSerialization.itemStackArrayFromBase64(enderInvStr);
			player.getEnderChest().setContents(enderContents);
			
		} catch (IOException e) {
			if (debugMode) plugin.getLogger().info("Error deserializing inventory from base64 string.");
			e.printStackTrace();
		}
		
	}

}