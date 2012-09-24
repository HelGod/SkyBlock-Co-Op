package nz.Tzeentchful.SkyBlock;


import java.util.Iterator;
import java.util.List;
//import com.sk89q.worldguard.protection.regions.*;
//import com.sk89q.worldguard.domains.*;
//import com.sk89q.worldguard.protection.flags.DefaultFlag;
//import com.sk89q.worldedit.BlockVector;
//import com.sk89q.worldedit.Vector;
//import com.sk89q.worldedit.WorldEditOperation;
//import com.sk89q.worldguard.protection.ApplicableRegionSet;
//import com.sk89q.worldedit.bukkit.WorldEditPlugin;
//import com.sk89q.worldedit.bukkit.selections.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
//import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.qgel.skySMP.Island;
import de.qgel.skySMP.Party;

/**
* Handler for the /newIsland command
* @author Qgel
*/
public class CreateIslandCommand implements CommandExecutor {
    private final skySMP plugin;
    private List<Party> partyList;
    public CreateIslandCommand(skySMP plugin) {
        this.plugin = plugin;
        
       
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("sbenhanced.create")) {
        	player.sendMessage(ChatColor.RED + "You don't have permission to use that command!");
        	return true;
        	}

        partyList = plugin.getPartyList();
        if (hasParty(player.getName()) >= 0){
        	player.sendMessage(ChatColor.RED + "You cannot use this command while in a party.");
        	player.sendMessage(ChatColor.RED + "Use /party leave to start your own island.");
        	return true;
        }
                                                
		//if(!(player.isOp()))
		//{
		//	player.sendMessage("This command is temporarily disabled and will be back shortly!");
		//	return true;
		//}
		
        if(!(player.getWorld().getEnvironment().getId() == 0)) {
        	player.sendMessage("Can only do that in the normal world, sorry");
        	return true;
        }

        if (plugin.hasIsland(player)) {
        	if(split.length == 0) {
        		Island location = plugin.getPlayerIsland(player.getName());
        		player.sendMessage("You already have an Island at " + location.x +" / " + location.z + " If you want a new one, use \"/newIsland replace\" instead.");
        		return true;
        	} else if (split[0].equals("replace")) {
        		        		plugin.deleteIsland(player.getName(), skySMP.skyworld);
        		//player.getInventory().clear();
        		//remove Items that drop on the island due to removal
        		//List<Entity> Entities = player.getNearbyEntities(15,15,15);
        		//Iterator<Entity> ent = Entities.iterator();
        		//while (ent.hasNext())
        		//	ent.next().remove();

        		return createIsland(player, skySMP.skyworld );
        	} else if (split[0].equals("new")) {
        		if(!(player.isOp()))
        			return false;
        		return createIsland(player, skySMP.skyworld);
        	}else if (split[0].equals("test")) {
        		if(!(player.isOp()))
        			return false;
        		//getOldIsland(player);
        		return true;
        	}
        } else {
        	    //getOldIsland(player);
            	return createIsland(player, skySMP.skyworld);
        }
        return false;
    }

	private boolean createIsland(CommandSender sender, World cworld) {
		Player player = (Player) sender;
		Island last = plugin.getLastIsland();
		try {
			Island next;
    		//if we have space because of a deleted Island, create one there
    		if(plugin.hasOrphanedIsland()) {
    			next = plugin.getOrphanedIsland();
    		} else {
                next = nextIslandLocation(last);
                plugin.setLastIsland(next);
                while (plugin.getPlayers().containsValue(next))
                {
                	next = nextIslandLocation(next);
                    plugin.setLastIsland(next);
                }
    		}
    		
            generateIslandBlocks(next.x, next.z, player, skySMP.skyworld);
            plugin.registerPlayerIsland(player.getName(), next);
            plugin.teleportHome(player, skySMP.skyworld);
            //run the item remover again... just to be safe. ;)
            player.getInventory().clear();
    		List<Entity> Entities = player.getNearbyEntities(15,15,15);
    		Iterator<Entity> ent = Entities.iterator();
    		while (ent.hasNext())
    			ent.next().remove();
    		plugin.protectIsland(sender, sender.getName());
    		player.performCommand("sethome");
        } catch (Exception ex) {
            player.sendMessage("Could not create your Island. Pleace contact a server moderator.");
            plugin.setLastIsland(last);
            ex.printStackTrace();
            return false;
        }
        return true;
	}

	public void generateIslandBlocks(int x, int z, Player player, World world ){
        int y = plugin.getISLANDS_Y(); //blub

        for(int x_operate = x; x_operate < x+3; x_operate++){
                for(int y_operate = y; y_operate < y+3; y_operate++){
                        for(int z_operate = z; z_operate < z+6; z_operate++){
                                Block blockToChange = world.getBlockAt(x_operate,y_operate,z_operate);
                                blockToChange.setTypeId(2);  //chest area
                        }
                }
        }

        for(int x_operate = x+3; x_operate < x+6; x_operate++){
                for(int y_operate = y; y_operate < y+3; y_operate++){
                        for(int z_operate = z+3; z_operate < z+6; z_operate++){
                                Block blockToChange = world.getBlockAt(x_operate,y_operate,z_operate);
                                blockToChange.setTypeId(2);    // 3x3 corner
                        }
                }
        }

        //tree
        for(int x_operate = x+3; x_operate < x + 7; x_operate++){
                for(int y_operate = y+7; y_operate < y+10; y_operate++){
                        for(int z_operate = z+3; z_operate < z+7; z_operate++){
                                Block blockToChange = world.getBlockAt(x_operate,y_operate,z_operate);
                                blockToChange.setTypeId(18);    //leaves
                        }
                }
        }


        for(int y_operate = y+3; y_operate < y+9; y_operate++){
                Block blockToChange = world.getBlockAt(x+5,y_operate,z+5);
                blockToChange.setTypeId(17);
        }


        // chest
        Block blockToChange = world.getBlockAt(x+1,y+3,z+1);
        blockToChange.setTypeId(54);
        Chest chest = (Chest) blockToChange.getState();
        Inventory inventory = chest.getInventory();
        inventory.clear();
        ItemStack item = new ItemStack(287,12); //String
        inventory.addItem(item);
        item = new ItemStack(327,1); //Bucket lava
        inventory.addItem(item);
        item = new ItemStack(352,1); //Bone
        inventory.addItem(item);
        item = new ItemStack(338,1); //Sugar Cane
        inventory.addItem(item);
        item = new ItemStack(40,1); //Mushroom red
        inventory.addItem(item);
        item = new ItemStack(79,2); //Ice
        inventory.addItem(item);
        item = new ItemStack(361,1); //pumpkin seeds
        inventory.addItem(item);
        item = new ItemStack(39,1); //mushroom brown
        inventory.addItem(item);
        item = new ItemStack(360,1); //melon slice
        inventory.addItem(item);
        item = new ItemStack(81,1); //cactus
        inventory.addItem(item);
        item = new ItemStack(323,1); //sign
        inventory.addItem(item);
        /*if(player.hasPermission("VIP.pack1")){
        	item = new ItemStack(3,10); //dirt
            inventory.addItem(item);
            item = new ItemStack(12,10); //sand
            inventory.addItem(item);
        } else if(player.hasPermission("VIP.pack2")){
        	item = new ItemStack(3,10); //dirt
            inventory.addItem(item);
            item = new ItemStack(12,10); //sand
            inventory.addItem(item);
            item = new ItemStack(265,5); //iron ingot
            inventory.addItem(item);
        } else if(player.hasPermission("VIP.pack3")){
        	item = new ItemStack(3,10); //dirt
            inventory.addItem(item);
            item = new ItemStack(12,10); //sand
            inventory.addItem(item);
            item = new ItemStack(265,5); //iron ingot
            inventory.addItem(item);
            item = new ItemStack(278,1); //diamond pickaxe
            inventory.addItem(item);
        }*/

        //spawn
        blockToChange = world.getBlockAt(x,y,z);
        blockToChange.setTypeId(7);

        //sand
        blockToChange = world.getBlockAt(x+2,y+1,z+1);
        blockToChange.setTypeId(12);
        blockToChange = world.getBlockAt(x+2,y+1,z+2);
        blockToChange.setTypeId(12);
        blockToChange = world.getBlockAt(x+2,y+1,z+3);
        blockToChange.setTypeId(12); 
	}
	private Island nextIslandLocation(Island lastIsland) {
		// Gets the next position of an Island based on the last one.

		// Generates new Islands in a spiral.
		int x = lastIsland.x;
		int z = lastIsland.z;
		 Island nextPos = new Island();
		 nextPos.x = x;
		 nextPos.z = z;
		    if ( x < z )
		    {
		        if ( ((-1) * x) < z)
		        {
		           nextPos.x = nextPos.x + plugin.getISLAND_SPACING();
		           return nextPos;
		        }
		        nextPos.z = nextPos.z + plugin.getISLAND_SPACING();
		        return nextPos;
		    }
		    if( x > z)
		    {
		        if ( ((-1) * x) >= z)
		        {
		            nextPos.x = nextPos.x - plugin.getISLAND_SPACING();
		            return nextPos;
		        }
		            nextPos.z = nextPos.z - plugin.getISLAND_SPACING();
		            return nextPos;
		    }
		    if ( x <= 0)
		    {
		    	nextPos.z = nextPos.z + plugin.getISLAND_SPACING();
		        return nextPos;
		    }
		    nextPos.z = nextPos.z - plugin.getISLAND_SPACING();
		    return nextPos;
	}
	
	private int hasParty(String playerName)
    {
     try{	
      if(Bukkit.getPlayer(playerName).isOnline())
      {
		for (int i = 0; i < partyList.size(); i++)
			if (partyList.get(i).hasMember(playerName))
				return i;
		return -1;
      } else
      return -2;
     }catch(Exception e){
    	 return -2;
     }
    }
	
	/*
   public boolean getOldIsland(Player player)
   {
	OfflinePlayer[] oplayers;
   	long offlineTime;
   	oplayers = Bukkit.getServer().getOfflinePlayers();
   	for (int i = 0; i < oplayers.length; i++)
   	{
   		offlineTime = oplayers[i].getLastPlayed();
   		offlineTime = (System.currentTimeMillis() - offlineTime)/3600000;
   		if (offlineTime > 336)
   		{
   			if (!oplayers[i].isOp() && !plugin.checkPerk(oplayers[i].getName(), "simpleprefix.donor") && !plugin.checkGroup(oplayers[i].getName(), "moderator") && !plugin.checkGroup(oplayers[i].getName(), "donor"))
   			{
   				//plugin.deleteIsland(oplayers[i].getName(), Bukkit.getWorld("skyIsland"));
   				player.sendMessage("Deleting: " + oplayers[i].getName() + "'s island.  They were offline for " + offlineTime + " hours." );
   				return true;
   			}
   		}
   	}
   	return true;
   }*/
}