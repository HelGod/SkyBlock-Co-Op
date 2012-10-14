package nz.Tzeentchful.SkyBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.qgel.skySMP.Island;
import de.qgel.skySMP.Party;

public class IslandManager {

	private HashMap<String, Island> playerIslands = null;
	private Stack<Island> orphaned = null;

	private Island lastIsland = new Island(0, 0);
	private skySMP plugin;
	private static int islandY = 100;
	private static int islandSpacing = 120;

	PartyManager prtymanager = new PartyManager();


	public IslandManager(skySMP plugin) {
		this.plugin = plugin;

		if(playerIslands == null){
			playerIslands = loadIslandList();
		}
		if(orphaned == null){
			orphaned = loadorphanedIslands();
		}
		if(lastIsland == null){
			lastIsland = loadLastIsland();
		}
	}


	public HashMap<String, Island> loadIslandList(){
		if(new File("playerIslands.bin").exists()){
			HashMap<String, Island> load = null;
			try {
				load = (HashMap<String,Island>)SLAPI.load("playerIslands.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(load != null){

				return load;
			}
		}else{
			return new HashMap<String, Island>();
		}
		return null;
	}

	public Stack<Island> loadorphanedIslands(){
		if(new File("orpahnedIslands.bin").exists()){
			Stack<Island> load = null;
			try {
				load = (Stack<Island>)SLAPI.load("orpahnedIslands.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(load != null){

				return load;
			}
		}else{
			return new Stack<Island>();
		}
		return null;
	}

	public Island loadLastIsland(){
		if(new File("lastIsland.bin").exists()){
			Island load = null;
			try {
				load = (Island)SLAPI.load("lastIsland.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(load != null){

				return load;
			}
		}else{
			return new Island(0, 0);
		}
		return null;
	}


	public void saveIslandList(){
		if(new File("playerIslands.bin").exists()){
			try {
				SLAPI.save(playerIslands, "playerIslands.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}


		}else{
			try {
				new File("playerIslands.bin").createNewFile();
				saveIslandList();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveOrphanedIslands(){
		if(new File("orpahnedIslands.bin").exists()){
			try {
				SLAPI.save(orphaned, "orpahnedIslands.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}


		}else{
			try {
				new File("orpahnedIslands.bin").createNewFile();
				saveOrphanedIslands();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveLastIsland(){
		if(new File("lastIsland.bin").exists()){
			try {
				SLAPI.save(lastIsland, "lastIsland.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}


		}else{
			try {
				new File("lastIsland.bin").createNewFile();
				saveLastIsland();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public HashMap<String, Island> getIslandList(){
		return playerIslands;
	}


	public void setOrphanedIslands(Stack<Island> list){
		orphaned = list;
	}

	public Stack<Island> getOrphanedIslands(){
		return orphaned;
	}

	public void removeIsland(String player){
		playerIslands.remove(player);
		saveOrphanedIslands();
		saveLastIsland();
		saveIslandList();
	}

	public boolean createIsland(Player player) {
		Island last = lastIsland;
		int bf = orphaned.size();
		boolean dolast = false;
		try {
			Island next;
			//if we have space because of a deleted Island, create one there
			if(hasOrphanedIsland()) {
				next = getOrphanedIslands().pop();
				dolast = true;
			} else {
				next = nextIslandLocation(last);
			}
			
			generateIslandBlocks(next.x, next.z, player, skySMP.skyworld);
			registerPlayerIsland(player.getName(), next);
			protectIsland(player, player.getName());
			teleportHome(player);
			if(!dolast){
			lastIsland = next;
			}
			
			Island home = getPlayerIsland(player.getName());

			Block highest = skySMP.skyworld.getHighestBlockAt(home.x, home.z);
			Iterator<ProtectedRegion> it = getWorldGuard().getRegionManager(skySMP.skyworld).getApplicableRegions(highest.getLocation()).iterator();
			while(it.hasNext()){
				ProtectedRegion region = it.next();
				if(!region.getOwners().contains(getWorldGuard().wrapPlayer(player))){
					 getWorldGuard().getRegionManager(skySMP.skyworld).removeRegion(region.getTypeName());
				}
			}
			//run the item remover again... just to be safe. ;)
			player.getInventory().clear();
			List<Entity> Entities = player.getNearbyEntities(15,15,15);
			Iterator<Entity> ent = Entities.iterator();
			while (ent.hasNext()) {
				ent.next().remove();
			}
		} catch (Exception ex) {
			player.sendMessage("Could not create your Island. Please contact a server moderator.");
			setLastIsland(last);
			ex.printStackTrace();
			return false;
		}
		saveLastIsland();
		saveIslandList();
		return true;
	}

	public void generateIslandBlocks(int x, int z, Player player, World world ){
		int y = islandY; //blub

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
		world.generateTree(new Location(world, x+5,y+3,z+5), TreeType.TREE);   


		// chest
		Block blockToChange = world.getBlockAt(x+1,y+3,z+1);
		blockToChange.setTypeId(54);
		Chest chest = (Chest) blockToChange.getState();
		Inventory inventory = chest.getInventory();
		inventory.clear();

		for(ItemStack current : skySMP.chestItems){
			inventory.addItem(current);
		}

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
		Island nextPos = new Island(x, z);
		if ( x < z )
		{
			if ( -1 * x < z)
			{
				nextPos.x = nextPos.x + islandSpacing;
				return nextPos;
			}
			nextPos.z = nextPos.z + islandSpacing;
			return nextPos;
		}
		if( x > z)
		{
			if ( -1 * x >= z)
			{
				nextPos.x = nextPos.x - islandSpacing;
				return nextPos;
			}
			nextPos.z = nextPos.z - islandSpacing;
			return nextPos;
		}
		if ( x <= 0)
		{
			nextPos.z = nextPos.z + islandSpacing;
			return nextPos;
		}
		nextPos.z = nextPos.z - islandSpacing;
		return nextPos;
	}

	public void addOwnerToIsland(String owner, Player player)
	{

		try
		{
			if (hasIsland(owner) && getWorldGuard().getRegionManager(skySMP.skyworld).hasRegion(owner.toLowerCase()+"Island"))
			{
				owner = owner.toLowerCase();
				DefaultDomain owners = getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(owner + "Island").getOwners();
				owners.addPlayer(player.getName());
				getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(owner + "Island").setOwners(owners);
				getWorldGuard().getRegionManager(skySMP.skyworld).save();

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}


	public void removeOwnerFromIsland(String owner, String player)
	{
		
		try
		{
			if (hasIsland(owner) && getWorldGuard().getRegionManager(skySMP.skyworld).hasRegion(owner.toLowerCase()+"Island"))
			{
				ProtectedRegion region = getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(owner.toLowerCase()+"Island");
				DefaultDomain owners = region.getOwners();
				if(owners.getPlayers().contains(player.toLowerCase())){
					owners.removePlayer(player.toLowerCase());
					region.setOwners(owners);
					getWorldGuard().getRegionManager(skySMP.skyworld).save();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Island getLastIsland() {
		return lastIsland; 
	}
	public void setLastIsland(Island island) { 
		lastIsland = island; 
		try{SLAPI.save(lastIsland, "lastIsland.bin");
		}catch(Exception e){

		}
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

	public boolean hasIsland(String playername) {
		if (playerIslands.containsKey(playername)) {
			return true;
		} else if (playerIslands.containsKey(playername.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	public void deleteIsland(String playerName) {
		int bf = orphaned.size();
		if(hasIsland(playerName)) {
			unprotectIsland(playerName);
			Island island = getPlayerIsland(playerName);
			for(int x = island.x - 50; x < island.x + 50; x++) {
				for(int y = islandY - 200; y < skySMP.skyworld.getMaxHeight(); y++) {
					for(int z = island.z - 50; z < island.z + 50; z++) {
						Block block = skySMP.skyworld.getBlockAt(x,y,z);
						if(block.getTypeId() != 0) {
							block.setTypeId(0);
						}
					}
				}
			}

			orphaned.push(island);
			removeIsland(playerName);
			saveIslandList();
			saveOrphanedIslands();
		}
	}

	public Island getPlayerIsland(String playerName) {
		if(hasIsland(playerName)) {
			if (playerIslands.containsKey(playerName)) {
				return playerIslands.get(playerName);
			} else {
				return playerIslands.get(playerName.toLowerCase());
			}
		} 

		Island spawn = new Island(skySMP.SPAWN_X,  skySMP.SPAWN_Z);
		return spawn;
	}

	public boolean hasOrphanedIsland() {
		if(orphaned.isEmpty()){
			return false;
		}else{
			return true;
		}
	}
	
	public Island getOrphanedIsland() {
		if(hasOrphanedIsland()) {
			return orphaned.pop();
		}    
		
		Island spawn = new Island(skySMP.SPAWN_X,  skySMP.SPAWN_Z);
		
		return spawn;
	}

	public void clearOrphanedIsland() {
		while(hasOrphanedIsland()) {
			orphaned.pop();
		}    
	}

	public boolean protectIsland(CommandSender sender, String player){
		try
		{
			if (hasIsland(player) && !getWorldGuard().getRegionManager(skySMP.skyworld).hasRegion(player.toLowerCase()+"Island")){
				ProtectedRegion region = null;
				DefaultDomain owners = new DefaultDomain();
				region = new ProtectedCuboidRegion(player.toLowerCase() +"Island",getProtectionVectorLeft(getPlayerIsland(player)), getProtectionVectorRight(getPlayerIsland(player)));
				if(prtymanager.hasParty(player)){
					Party plrparty = prtymanager.getPlayerParty(player);
					for(String cmem : plrparty.getMembers()){
						if(!cmem.equals("EmptySlot")){
							owners.addPlayer(cmem);
						}
					}
				}
				owners.addPlayer(player);
				region.setOwners(owners);
				region.setParent(getWorldGuard().getRegionManager(skySMP.skyworld).getRegion("__Global__"));
				if(skySMP.showmsg){
				region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(getWorldGuard(), sender, "You are entering a protected island area. (" +player + ")"));
				region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(getWorldGuard(), sender, "You are leaving a protected island area. (" +player + ")"));
				}
				region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, "deny"));
				region.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(getWorldGuard(), sender, "deny"));
				getWorldGuard().getRegionManager(skySMP.skyworld).addRegion(region);
				getWorldGuard().getRegionManager(skySMP.skyworld).save();
				return true;
			}else{
				sender.sendMessage(ChatColor.DARK_RED + "Player doesn't have an island or it's already protected!");
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public void unprotectIsland(String playerName){
		if(getWorldGuard().getRegionManager(skySMP.skyworld).hasRegion(playerName.toLowerCase() + "Island")){
			getWorldGuard().getRegionManager(skySMP.skyworld).removeRegion(playerName.toLowerCase() + "Island");
			try {
				getWorldGuard().getRegionManager(skySMP.skyworld).save();
			} catch (ProtectionDatabaseException e) {
				e.printStackTrace();
			}
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

	public void teleportHome(Player player) {
		Island home = getPlayerIsland(player.getName());

		Block highest = skySMP.skyworld.getHighestBlockAt(home.x, home.z);
		player.teleport(highest.getLocation().add(0.5, 0, 0.5));

	}
	
	public void teleportHomeOther(String player, Player target) {
		Island home = getPlayerIsland(player);

		Block highest = skySMP.skyworld.getHighestBlockAt(home.x, home.z);
		target.teleport(highest.getLocation().add(0.5, 0, 0.5));

	}

	public void teleportGroup(String leader, Player player) {
		Island home = getPlayerIsland(leader);

		Block highest = skySMP.skyworld.getHighestBlockAt(home.x, home.z);
		player.teleport(highest.getLocation().add(0.5, 0, 0.5));
	}

	public WorldEditPlugin getWorldEdit() {
		Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldEdit");

		// WorldEdit may not be loaded
		if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
			return null;
		}

		return (WorldEditPlugin) plugin;
	}

	public WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}
	
	
	public void checkislands(CommandSender cs){
		
		Set<String> r = getWorldGuard().getRegionManager(skySMP.skyworld).getRegions().keySet();
		getWorldGuard().getRegionManager(skySMP.skyworld).getRegions().keySet().removeAll(r);
		
		/*for(int i = 0; i <= r.size(); i++){
			//getWorldGuard().getRegionManager(skySMP.skyworld).removeRegion(r.);
			r.
		}*/
		
		
		
		Iterator<Entry<String, Island>> it = playerIslands.entrySet().iterator();
		while(it.hasNext()){
			
			Entry<String, Island> curr = it.next();
			String hell = curr.getKey();

			protectIsland(cs ,hell); 
		}
		
		try {
			getWorldGuard().getRegionManager(skySMP.skyworld).save();
		} catch (ProtectionDatabaseException e) {
			e.printStackTrace();
		}
		
	}
	
	
}