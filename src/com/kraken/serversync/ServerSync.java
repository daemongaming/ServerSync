package com.kraken.serversync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.WeakHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerSync extends JavaPlugin {

	public static ServerSync plugin;
	public SyncListener listener;
	private Database db;
	
	//Lang vars
	public static String VERSION;
	private static String PLUGIN_NAME = "ServerSync";
	private static String SERVER_NAME;
	String language;
	ArrayList<String> languages = new ArrayList<String>();
	Messages messenger;
	
	//Options
	WeakHashMap<String, Boolean> options = new WeakHashMap<>();
	
    @Override
    public void onEnable() {
    	
    	//Plugin start-up
    	plugin = this;
		PluginManager pm = getServer().getPluginManager();
		
		//Copies the default config.yml from within the .jar if "plugins/<name>/config.yml" does not exist
		getConfig().options().copyDefaults(true);
		
		//Language/Messages handler class construction
		languages.add("english");
		languages.add("spanish");
		loadMessageFiles();
		language = getConfig().getString("language");
		messenger = new Messages(this, "english");
		
		//Load the build default files
		loadDefaultFiles();
		
	    //Loading default settings into options
    	setOption( "silent_mode", getConfig().getBoolean("silent_mode") );
    	setOption( "debug_mode", getConfig().getBoolean("debug_mode") );
    	silencer( options.get("silent_mode") );
    	
		//Get the server name from config
    	SERVER_NAME = getConfig().getString("server_name");
    	
        //Starts and registers the DHD Listener
  		this.listener = new SyncListener(this);
  		pm.registerEvents((Listener) listener, this);
  		
  		//Set the database info
  		db = new Database(this);
  		
  		//Get the plugin version
  		VERSION = getFileConfig("plugin").getString("version");
    	
    }
    
    @Override
    public void onDisable() {
    	
        getLogger().info("ServerSync has been stopped.");
                
    }
    
    //Messages
    public void msg(Player player, String cmd) {
    	messenger.makeMsg(player, cmd);
    }
    
    public void consoleMsg(String cmd) {
    	messenger.makeConsoleMsg(cmd);
    }
    
    //Options setting
    public void setOption(String option, boolean setting) {
    	getConfig().set(option, setting);
    	saveConfig();
    	options.put(option, setting);
    }
    
    //Language setting
    public void setLanguage(String language) {
    	this.language = language;
    	getConfig().set("language", language);
    	saveConfig();
    	messenger.setLanguage(language);
    }
    
    //Silent mode setting
    public void silencer(boolean silentMode) {
    	messenger.silence(silentMode);
    }
    
	public void loadMessageFiles() {
		for (String lang : languages) {
		    File msgFile = new File("plugins/" + PLUGIN_NAME + "/lang/", lang.toLowerCase() + ".yml");
		    if ( !msgFile.exists() ) {
		    	saveResource("lang/" + lang.toLowerCase() + ".yml", false);
		    }
		}
    }
	
	//Load files from default if not present
	public void loadDefaultFiles() {
		
		//Default files to be loaded: balance.yml, transactions.yml
		String[] files = {"players"};
		
		//Check each file and save from defaults if not present
		for (String fName : files) {
			File file = new File("plugins/" + PLUGIN_NAME + "/", fName + ".yml");
		    if ( !file.exists() ) {
		    	saveResource(fName + ".yml", false);
		    }
		}
	    
    }
	
	//Get a FileConfiguration from file name
	public FileConfiguration getFileConfig(String fileName) {
	    File f = new File("plugins/" + PLUGIN_NAME, fileName + ".yml");
	    FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
	    return fc;
	}
	
    //Save the player file
    public void saveCustomFile(FileConfiguration fileConfig, File file) {
    	try {
			fileConfig.save(file);
		} catch (IOException e) {
			System.out.println("Error saving custom config file: " + file.getName());
		}
    }
    
    //Return the database object
    public Database getDatabase() {
    	return db;
    }
    
    public String getPluginName() {
    	return PLUGIN_NAME;
    }
    
    public String getServerName() {
    	return SERVER_NAME;
    }
    
    //Command handling
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return new Commands(this).onCommand(sender, cmd, label, args);
    }
    
}
