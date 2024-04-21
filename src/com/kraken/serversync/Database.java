package com.kraken.serversync;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Database {
	
	//Main class instance
	private ServerSync plugin;
	
    //JDBC driver
    static String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    //Create the connection object
    private static Connection connection = null;
    
    //Connection credentials & info
    private String dbName = null;
    private String url;
    private String user;
    private String pass;
    
    //Flag to check if the table is present and ready for queries
    boolean formatted = false;

    //Constructor
    public Database(ServerSync plugin) {
    	
    	//Get the instance of the main plugin class
    	this.plugin = plugin;
    	
    	//Database config file
	    File dbFile = new File("plugins/ServerSync", "database.yml");
	    FileConfiguration dbConfig = YamlConfiguration.loadConfiguration(dbFile);
	    Set<String> dbConfigKeys = dbConfig.getKeys(false);
	    
	    //Try to connect to the database using config file info
	    boolean valuesPresent = dbConfigKeys.containsAll(Arrays.asList("url", "user", "pass", "dbName"));
	    
	    if (valuesPresent) {
	    	
	    	//Database connection values missing error
	        plugin.getLogger().info("Attempting to set up database connector using values from database.yml...");
	    	
	    	//Get the database connection info/credentials
	    	url = dbConfig.getString("url");
	    	user = dbConfig.getString("user");
	    	pass = dbConfig.getString("pass");
	    	dbName = dbConfig.getString("dbName");
	    	
	    	//Make sure the database is formatted correctly
	    	getConnection();
	        formatted = checkDatabaseFormat();
	    	closeConnection();
	    
    	} else {
    		
	        //Database connection values missing error
	        plugin.getLogger().info("Could not set up database connector: credentials missing from database.yml file. Creating default database.yml...");
	        
	        //Set default keys and empty values to be filled in by admin
	        dbConfig.set("url", "");
	        dbConfig.set("user", "");
	        dbConfig.set("pass", "");
	        dbConfig.set("dbName", "");
	        
	        plugin.saveCustomFile(dbConfig, dbFile);
	        
	    }
        
    }
    
    //Format the database appropriately if not already so
    public boolean checkDatabaseFormat() {
    	
    	try (Statement statement = connection.createStatement()) {
    		
    		//Create tables for player data
    		String query = "CREATE TABLE IF NOT EXISTS playerData ("
    				+ "player varchar(255), server varchar(255), "
    				+ "health double, air int, hunger int, effects text, "
    				+ "inventory text, armor text, enderInventory text, "
    				+ "xp float, mode varchar(255)"
    				+ ")";
    		statement.execute(query);
    		
    		return true;
    		
		} catch (SQLException e) {
			plugin.getLogger().info("Could not initially format database: exception thrown on query.");
			e.printStackTrace();
		}
    	
    	return false;
    	
    }
    
    //Close the JDBC connection to database
    public boolean closeConnection() {

    	//Close the connection
    	try {
            if (connection != null && !connection.isClosed()){
                connection.close();
                return true;
            }
        } catch(Exception e) {
			plugin.getLogger().info("Could not close connection to database: exception thrown on closing.");
            e.printStackTrace();
        }
    	
    	return false;
        
    }
    
    //Return the JDBC connection to database
    public Connection getConnection() {
    	
    	//Check if a connection is already made
    	try {
            if (connection == null || connection.isClosed()){
            	Class.forName(JDBC_DRIVER);
                connection = DriverManager.getConnection("jdbc:mysql://" + url + "/" + dbName, user, pass);
            }
        } catch(Exception e) {
			plugin.getLogger().info("Could not connect to database: exception thrown on connection.");
            e.printStackTrace();
        }
    	
    	return connection;
        
    }
    
    //Return the name of the database being used
    public String getDbName() {
    	return dbName;
    }
    
}