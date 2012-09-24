package nz.Tzeentchful.SkyBlock;

import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
//import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.qgel.skySMP.Island;

public class skyDevCommand implements CommandExecutor {
    private final skySMP plugin;
    private Island registerIsland;
    private Island orphanIsland;
    //private Island checkIsland;
    //private Island deleteIsland;
    public skyDevCommand(skySMP plugin) {
        this.plugin = plugin;
    }
    
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player) || !(sender.hasPermission("sbenhanced.skydev")) ){
            return false;
        }
		//sender.sendMessage("The last island location is: " +plugin.getLastIsland().x + " / " +plugin.getLastIsland().z);
		Player player = (Player) sender;
		if (split.length == 0) {
        	if (sender.hasPermission("sbenhanced.skydev"))
        	{
        		player.sendMessage("[skydev usage]");
        		player.sendMessage("/skydev addorphan: the next person to make an island will use your location.");
        		player.sendMessage("/skydev register <playername>: set playername's island location to your location.");
        		player.sendMessage("/skydev protect <playername>: add protection to the player's island.");
        		player.sendMessage("/skydev check <playername>: check to see if the player has a registered island.");
        		player.sendMessage("/skydev offline <playername>: see how long it has been (in hours) since the player logged in.");
        		player.sendMessage("/skydev clearorphan: remove any orphans you have added.");
        	}else
        		player.sendMessage("You don't have permission to use this command.");
           	}else if (split.length == 1) {
        	if (split[0].equals("lastisland") && sender.hasPermission("sbenhanced.skydev"))
        	{
        		registerIsland = new Island(); 
            	registerIsland.x = player.getLocation().getBlockX();
            	registerIsland.z = player.getLocation().getBlockZ();
        		plugin.setLastIsland(registerIsland);
        	}else if (split[0].equals("forcesave") && sender.hasPermission("sbenhanced.skydev"))
        	{
        		try {
        			SLAPI.save(plugin.getPlayers(), "playerIslands.bin");
        			SLAPI.save(plugin.getLastIsland(), "lastIsland.bin");
        		} catch (Exception e) {
        			System.out.println("Something went wrong saving the Island data. That's really bad but there is nothing we can really do about it. Sorry");
        			e.printStackTrace();
        		}
        	}else if (split[0].equals("addorphan") && player.isOp())
        	{
        		orphanIsland = new Island(); 
            	orphanIsland.x = player.getLocation().getBlockX();
            	orphanIsland.z = player.getLocation().getBlockZ();
            	plugin.addOrphan(orphanIsland);
        	}else if (split[0].equals("check") && player.isOp())
        	{
            	if (plugin.hasIsland(split[1]))
            	{
            		player.sendMessage("This is a registered player island.");
            	}
           	}else if (split[0].equals("clearorphan") && player.isOp())
        	{
        		plugin.clearOrphanedIsland();
        	}else if (split[0].equals("offline") && sender.hasPermission("sbenhanced.skydev"))
        	{
        		OfflinePlayer[] oplayers;
            	long offlineTime;
            	oplayers = Bukkit.getServer().getOfflinePlayers();
            	for (int i = 0; i < oplayers.length; i++)
            	{
            		offlineTime = oplayers[i].getLastPlayed();
            		offlineTime = (System.currentTimeMillis() - offlineTime)/3600000;
            		if (offlineTime > 240)
            		    player.sendMessage(oplayers[i].getName() + " hasn't been on for " + offlineTime + " hours");
            	}
        	}
        	
    } else if (split.length == 2) {
            	if (split[0].equals("register") && player.isOp())
            	{
            		registerIsland = new Island(); 
                	registerIsland.x = player.getLocation().getBlockX();
                	registerIsland.z = player.getLocation().getBlockZ();;
            		plugin.registerPlayerIsland(split[1], registerIsland);
            	}else if (split[0].equals("protect") && sender.hasPermission("sbenhanced.skydev"))
            	{
            		plugin.protectIsland(sender, split[1]);
            	}else if (split[0].equals("unregister") && player.isOp())
            	{
            		plugin.unregisterPlayerIsland(split[1]);
            	}else if (split[0].equals("check") && sender.hasPermission("sbenhanced.skydev"))
        	    {
        		if (plugin.hasIsland(split[1]))
        		{	
        		registerIsland = plugin.getPlayerIsland(split[1]); 
        		player.sendMessage(split[1] + " has an island at: " + registerIsland.x + "/" + registerIsland.z);
        		}else
        			player.sendMessage(split[1] + " does not have an island registered.");
                }else if (split[0].equals("offline") && sender.hasPermission("sbenhanced.skydev"))
            	{
            		OfflinePlayer[] oplayers;
                	long offlineTime;
                	oplayers = Bukkit.getServer().getOfflinePlayers();
                	for (int i = 0; i < oplayers.length; i++)
                	{
                		offlineTime = oplayers[i].getLastPlayed();
                		offlineTime = (System.currentTimeMillis() - offlineTime)/3600000;
                		if (oplayers[i].getName().equalsIgnoreCase(split[1]))
                		{
                		   player.sendMessage(oplayers[i].getName() + " hasn't been on for " + offlineTime + " hours");
                  		   return true;
                		}
                	}
            	}else if (split[0].equals("copy") && player.isOp())
            	{
            		if (plugin.hasIsland(split[1]))
            		{
            			
            		}else
            			player.sendMessage("That player doesn't have an island registered");
            		
            	} 
    }
        return true;
	
	}
	
}
