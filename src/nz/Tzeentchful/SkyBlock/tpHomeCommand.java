package nz.Tzeentchful.SkyBlock;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tpHomeCommand implements CommandExecutor {
    private final skySMP plugin;
    public tpHomeCommand(skySMP plugin) {
        this.plugin = plugin;
    }
    
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player) ){
            return false;
        }
        Player player = (Player)sender;
        if (hasParty(sender.getName()) >= 0)
        {
        	if(player.getWorld().getEnvironment().getId() == 0) {
				plugin.teleportGroup(plugin.getPartyList().get(hasParty(sender.getName())).getLeader(), ((Player)sender),skySMP.skyworld);
				return true;
				}
        		else {
				player.sendMessage("Can't tphome in the nether, sorry");
				return true;
			}
        }else if(plugin.hasIsland(sender.getName())) {
			if(player.getWorld().getEnvironment().getId() == 0) {
				plugin.teleportHome(((Player)sender),skySMP.skyworld);
				return true;
			} else {
				player.sendMessage("Can't tphome in the nether, sorry");
				return true;
			}
		} else {
			sender.sendMessage("Error.  You do not have an island or a party.");
			return true;
		}
	}

	private int hasParty(String playerName)
    {
     try{	
      if(Bukkit.getPlayer(playerName).isOnline())
      {
		for (int i = 0; i < plugin.getPartyList().size(); i++)
			if (plugin.getPartyList().get(i).hasMember(playerName))
				return i;
		return -1;
      } else
      return -2;
     }catch(Exception e){
    	 return -2;
     }
    }
}
