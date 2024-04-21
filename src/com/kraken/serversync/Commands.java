package com.kraken.serversync;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {
	
	//Get the main instance
	private ServerSync plugin;
	
	//Constructor
	public Commands(ServerSync plugin) {
		this.plugin = plugin;
	}
	
    //Commands
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String command = cmd.getName();
    	Player player = Bukkit.getServer().getPlayerExact("krakenmyboy");
		boolean isPlayer = false;
		
		//Player commands
        if ( sender instanceof Player ) {
        	player = (Player) sender;
        	isPlayer = true;
        }
		
		switch ( command.toLowerCase() ) {
		
			case "sync":
				
				if (args.length == 0) {
					
					if (isPlayer) {
						plugin.messenger.makeMsg(player, "cmdVersion");
					} else {
						plugin.messenger.makeConsoleMsg("cmdVersion");
					}
					
				}
				
			default:	
				cmdNotRecognized(player);
				return true;
				
		}
        
    }
	
	public void cmdNotRecognized(Player player) {
		
		if (player instanceof Player) {
			plugin.messenger.makeMsg(player, "errorIllegalCommand");
		} else {
			plugin.messenger.makeConsoleMsg("errorCommandFormat");
		}
		
	}

}