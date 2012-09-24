package nz.Tzeentchful.SkyBlock;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.qgel.skySMP.Island;

public class skyHelpCommand implements CommandExecutor {
	
	private final skySMP plugin;
	private HashMap<String, Island> playerIslands;
	public skyHelpCommand(skySMP plugin) {
        this.plugin = plugin;
        playerIslands = new HashMap<String, Island>();
    }
    
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player) ){
            return false;
        }
        if (split.length == 0) {
        	sender.sendMessage("Skyblock gives you a tiny map in the sky. Survive, expand, and build a sky empire!" );
        	sender.sendMessage("Commands:");
        	sender.sendMessage("/newIsland [replace]: gives you a new Island (and starting items) or replaces your old one");
        	sender.sendMessage("/tphome: Teleports you to your island.");
        	//sender.sendMessage("/skyTasks or /t : Show a list of tasks that can be completed for rewards.");
        	sender.sendMessage("/party : Join a skyblocking party (max 4 people)");
        	//sender.sendMessage("/skybucks : See the best skyblockers and how many skybucks you have");
        	//sender.sendMessage("/skyhelp : Print this help message");
        }else if (split.length == 1) {
        	if (sender.isOp())
        	{
        		if (split[0].equals("playerlist"))
        				{
        					playerIslands = plugin.getPlayers();
        					sender.sendMessage(playerIslands.toString());
        		    		
        				}
        	}
        }
        
       	return true;
	}

}
