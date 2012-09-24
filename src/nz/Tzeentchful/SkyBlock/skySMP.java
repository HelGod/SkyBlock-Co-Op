/**
* Skyblock SMP mod
* @author Qgel & Tzeentchful
* Original idea and map by Noobcrew
* http://www.minecraftforum.net/topic/600254-surv-skyblock/
*/

package nz.Tzeentchful.SkyBlock;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import de.qgel.skySMP.Island;
import de.qgel.skySMP.Party;

import org.bukkit.command.CommandSender;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.economy.Economy;
//import net.milkbowl.vault.economy.EconomyResponse;
import nz.Tzeentchful.SkyBlock.Metrics.Graph;

public class skySMP extends JavaPlugin {
    private HashMap<String, Island> playerIslands = new HashMap<String, Island>();
    private Stack<Island> orphaned = new Stack<Island>();
    public static World skyworld = null;
    public static World spawnworld = null;
    private static int SPAWN_X = 0;
    private static int SPAWN_Z = 0;
    private Island lastIsland;
    private List<Party> partyList = new ArrayList<Party>();
    private static int ISLANDS_Y = 190;
    private static int ISLAND_SPACING = 120;
    public static Permission perms = null;
    public static Economy econ = null;
    private Island centerIsland;
    public void onDisable() {
        //save out IslandData and party to disk
    	try {
			SLAPI.save(playerIslands, "playerIslands.bin");
			SLAPI.save(lastIsland, "lastIsland.bin");
			this.savePartyList();
		} catch (Exception e) {
			System.out.println("Something went wrong saving the island and/or party data!");
			e.printStackTrace();
		}
    	PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is now Disabled!" );
    }

	public void onEnable() {
		loadconfig();
		if(getServer().getWorlds().contains(skyworld)){
        centerIsland = new Island(); 
    	centerIsland.x = 0;
    	centerIsland.z = 0;
		setupPermissions();
        // Register commands
        getCommand("newIsland").setExecutor(new CreateIslandCommand(this));
        getCommand("removeIsland").setExecutor(new RemoveIslandCommand(this));
        getCommand("tphome").setExecutor(new tpHomeCommand(this));
        getCommand("skyHelp").setExecutor(new skyHelpCommand(this));
        getCommand("skydev").setExecutor(new skyDevCommand(this));
        getCommand("party").setExecutor(new partyCommand(this));
        PluginDescriptionFile pdfFile = this.getDescription();
        
        //Load the Island data from disk
        try { 
        	if(new File("lastIsland.bin").exists())
        		lastIsland = (Island)SLAPI.load("lastIsland.bin");
        	
        	if(null == lastIsland) {
            	//in case we don't have any data on disk
            	lastIsland = new Island(); 
            	lastIsland.x = 0;
            	lastIsland.z = 0;
        	}
        	if(new File("playerIslands.bin").exists()){
            	@SuppressWarnings("unchecked")
        		HashMap<String,Island> load = (HashMap<String,Island>)SLAPI.load("playerIslands.bin");
    			if(null != load) 
    				playerIslands = load;
        	}
        	if(new File("partylist.bin").exists())
        	{
        		@SuppressWarnings("unchecked")
        		List<Party> tempPartyList = (ArrayList<Party>)SLAPI.load("partylist.bin");
        		partyList = tempPartyList;
        	}
        	if(partyList == null) {
            	//in case we don't have any data on disk
        		partyList = new ArrayList<Party>();
        		partyList.add(new Party("NoLeader", "NoMember", centerIsland));
        	}
        	if(new File("orphanedIslands.bin").exists()) {
        		@SuppressWarnings("unchecked")
				Stack<Island> load = (Stack<Island>)SLAPI.load("orphanedIslands.bin");
				if(null != load)
					orphaned = load;
			}else
        		new File("orphanedIslands.bin");
        	
        } catch (Exception e) {
			System.out.println("Could not load Island data from disk.");
			System.out.println(e.getMessage());
			e.printStackTrace();
        }
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		} else{
			System.out.println("[SkyBlockCo-Op] Could not find the SkyBlock world. Disableing");
			getServer().getPluginManager().disablePlugin(this);
		}
		//=======[-Metrics-]=======
		try {
		    Metrics metrics = new Metrics(this);
		    
		    Graph islandGraphs = metrics.createGraph("Islands");
		    islandGraphs.addPlotter(new Metrics.Plotter("Active Islands") {

		        @Override
		        public int getValue() {
		            return playerIslands.size();
		        }

		    });
		    islandGraphs.addPlotter(new Metrics.Plotter("Orphaned Islands") {

		        @Override
		        public int getValue() {
		            return orphaned.size();
		        }

		    });

		    
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
    }
	
	public void loadconfig(){
		getConfig().addDefault("SkyWorld", "SkyWorld");
		getConfig().addDefault("SpawnWorld", "world");
		
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		skyworld = getServer().getWorld(getConfig().getString("SkyWorld"));
		spawnworld = getServer().getWorld(getConfig().getString("SpawnWorld"));
	}
    
    public boolean hasIsland(final Player player) {
    	if (playerIslands.containsKey(player.getName()))
    		return true;
    	else if (playerIslands.containsKey(player.getName().toLowerCase()))
    		return true;
    	else
    		return false;
    }
    public boolean hasIsland(String playername) {
    	if (playerIslands.containsKey(playername))
    		return true;
    	else if (playerIslands.containsKey(playername.toLowerCase()))
    		return true;
    	else
    		return false;
    }

    public boolean hasOrphanedIsland() {
    	return !orphaned.empty();
    }
    public Island getOrphanedIsland() {
    	if(hasOrphanedIsland()) {
    		return orphaned.pop();
    	}    
    	
    	Island spawn = new Island();
    	spawn.x = SPAWN_X;
    	spawn.z = SPAWN_Z;
    	
    	return spawn;
    }
        
    public void clearOrphanedIsland() {
    	while(hasOrphanedIsland()) {
    		orphaned.pop();
    	}    
    }
    
    public Island getPlayerIsland(String playerName) {
    	if(hasIsland(playerName)) {
    		if (playerIslands.containsKey(playerName))
    			return playerIslands.get(playerName);
    		else
    			return playerIslands.get(playerName.toLowerCase());
    	} 
    	Island spawn = new Island();
    	spawn.x = SPAWN_X;
    	spawn.z = SPAWN_Z;
    	return spawn;
    }
    
    public int getISLANDS_Y() { return ISLANDS_Y; };
    public Island getLastIsland() { return lastIsland; }
    public void setLastIsland(Island island) { lastIsland = island; try{SLAPI.save(lastIsland, "lastIsland.bin");}catch(Exception e){}}
    public int getISLAND_SPACING() { return ISLAND_SPACING; }
    public HashMap<String, Island> getPlayers() {return playerIslands; }

	public void deleteIsland(String playerName,World world) {
		if(hasIsland(playerName)) {
			getWorldGuard().getRegionManager(skyworld).removeRegion(playerName + "Island");
			Island island = getPlayerIsland(playerName);
			for(int x = island.x - 50; x < island.x + 50; x++)
				for(int y = ISLANDS_Y - 200; y < world.getMaxHeight(); y++)
					for(int z = island.z - 50; z < island.z + 50; z++) {
						Block block = world.getBlockAt(x,y,z);
						if(block.getTypeId() != 0)
							block.setTypeId(0);
					}

			orphaned.push(island);
			playerIslands.remove(playerName);
		}
	}
	
	public void addOrphan(Island island)
	{
		orphaned.push(island);
	}

	public void unregisterPlayerIsland(String player)
	{
		playerIslands.remove(player);
	}
	
	public void registerPlayerIsland(String player, Island newIsland) {
		playerIslands.put(player, newIsland);
		try {
			SLAPI.save(playerIslands, "playerIslands.bin");
			SLAPI.save(lastIsland, "lastIsland.bin");
			SLAPI.save(orphaned, "orpahnedIslands.bin");
		} catch (Exception e) {
			System.out.println("Something went wrong saving the Island data. That's really bad but there is nothing we can really do about it. Sorry");
			e.printStackTrace();
		}
	}

	public void teleportHome(Player player, World homeWorld) {
		Island home = getPlayerIsland(player.getName());
		int h = ISLANDS_Y;
		while(homeWorld.getBlockTypeIdAt(home.x, h, home.z) != 0) {
				h++;
			}
		    h++;
			homeWorld.loadChunk(home.x, home.z);
			player.teleport(new Location(homeWorld, home.x, h, home.z));	
		}
	
	public void teleportGroup(String leader, Player player, World homeWorld) {
		Island home = getPlayerIsland(leader);
		int h = ISLANDS_Y;
				   while(homeWorld.getBlockTypeIdAt(home.x, h, home.z) != 0) {
				    	h++;
				   }
				   h++;
				   homeWorld.loadChunk(home.x, home.z);
				   player.teleport(new Location(homeWorld, home.x, h, home.z));	

	}
	
	
	public List<Party> getPartyList()
	{
		return partyList;
	}
	
	public void savePartyList()
	{
		try
		{
			  SLAPI.save(partyList, "partylist.bin");
		}catch (Exception e) 
		{
			System.out.println("Error saving the party list!");
			e.printStackTrace();
		}
	}
	
	
	public void addPerk(Player player, String perk)
	{
		perms.playerAdd((String) null, player.getName(), perk);
	}
	
	public void removePerk(Player player, String perk)
	{
		perms.playerRemove((String) null, player.getName(), perk);
	}
	
	public void addGroup(Player player, String perk)
	{
		perms.playerAddGroup((String) null, player.getName(), perk);
	}
	
	public boolean checkPerk(String player, String perk)
	{
		if (perms.has((String) null, player, perk))
			return true;
		else
			return false;
	}
	
	public boolean checkGroup(String player, String perk)
	{
		if (perms.playerInGroup((String) null, player, perk))
			return true;
		else
			return false;
	}
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	public WorldEditPlugin getWorldEdit() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
   
        // WorldEdit may not be loaded
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
   
        return (WorldEditPlugin) plugin;
    }
	
	public WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public void protectIsland(CommandSender sender, String player)
	{
		try
		{
			if (hasIsland(player) && !this.getWorldGuard().getRegionManager(skyworld).hasRegion(player+"Island"))
			{
				ProtectedRegion region = null;
				DefaultDomain owners = new DefaultDomain();
				region = new ProtectedCuboidRegion(player +"Island", this.getProtectionVectorLeft(this.getPlayerIsland(player)), this.getProtectionVectorRight(this.getPlayerIsland(player)));
				owners.addPlayer(player);
				region.setOwners(owners);
				region.setParent(this.getWorldGuard().getRegionManager(skyworld).getRegion("__Global__"));
				region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(this.getWorldGuard(), sender, "You are entering a protected island area. (" +player + ")"));
				region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(this.getWorldGuard(), sender, "You are leaving a protected island area. (" +player + ")"));
				region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(this.getWorldGuard(), sender, "deny"));
				this.getWorldGuard().getRegionManager(skyworld).addRegion(region);
			}else
			{
				sender.sendMessage("Player doesn't have an island or it's already protected!");
			}
		} catch (Exception ex) {
            ex.printStackTrace();
        }
	}

	private BlockVector getProtectionVectorLeft(Island island)
	{
		return new BlockVector(island.x + 50, 255, island.z + 50);
	}
	
	private BlockVector getProtectionVectorRight(Island island)
	{
		return new BlockVector(island.x - 50, 0, island.z - 50);
	}
	
}

