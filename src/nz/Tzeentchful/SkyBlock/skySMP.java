/**
 * Skyblock SMP mod
 * @author Qgel & Tzeentchful
 * Original idea and map by Noobcrew
 * http://www.minecraftforum.net/topic/600254-surv-skyblock/
 */

package nz.Tzeentchful.SkyBlock;

import java.io.IOException;
import java.util.*;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import de.qgel.skySMP.Island;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;


public class skySMP extends JavaPlugin {

	public static World skyworld = null;
	public static World spawnworld = null;
	public static int SPAWN_X = 0;
	public static int SPAWN_Z = 0;
	public static List<ItemStack> chestItems = null;
	public static Boolean showmsg = null;
	private Island centerIsland;
	private PartyManager prtymanager = new PartyManager();
	private IslandManager islandmanager = new IslandManager(this);
	private int task = 0;
	@Override
	public void onDisable() {

		//cleanup so we don't get memory leaks on a reload. 
		SPAWN_X = 0;
		SPAWN_Z = 0;
		skyworld  = null;
		spawnworld = null;
		showmsg = null;
		prtymanager.clean();

		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is now Disabled!" );
	}

	@Override
	public void onEnable() {
		loadconfig();
		if(getServer().getWorlds().contains(skyworld)){
			centerIsland = new Island(0, 0); 

			//command aliases
			List<String> aliases = new ArrayList<String>();
			aliases.add("sb");
			//Register command
			getCommand("skyblock").setExecutor(new SkyblockCommand(this));
			getCommand("skyblock").setAliases(aliases);


			PluginDescriptionFile pdfFile = this.getDescription();

			System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		

			//=======[-Metrics-]=======
			try {
				Metrics metrics = new Metrics(this);
				metrics.addCustomData(new Metrics.Plotter("Total Islands") {

					@Override
					public int getValue() {
						return islandmanager.getIslandList().size();
					}

				});
				metrics.addCustomData(new Metrics.Plotter("Orphaned Islands") {

					@Override
					public int getValue() {
						return islandmanager.getOrphanedIslands().size();
					}

				});
				metrics.addCustomData(new Metrics.Plotter("Partys") {

					@Override
					public int getValue() {
						return prtymanager.getInviteList().size();
					}

				});
				metrics.start();
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
		}
	}

	public void loadconfig(){
		List<String> defchest = new ArrayList<String>();
		defchest.add("287:12");
		defchest.add("327:1");
		defchest.add("338:1");
		defchest.add("40:1");
		defchest.add("79:2");
		defchest.add("361:1");
		defchest.add("39:1");
		defchest.add("360:1");
		defchest.add("81:1");
		defchest.add("323:1");
		
		getConfig().addDefault("SkyWorld", "SkyWorld");
		getConfig().addDefault("SpawnWorld", "world");
		getConfig().addDefault("WG Region Messages", true);
		getConfig().addDefault("Chest", defchest);

		getConfig().options().copyDefaults(true);
		saveConfig();
		
		showmsg = getConfig().getBoolean("WG Region Messages");
		chestItems = parseChestItems(getConfig().getStringList("Chest"));

		skyworld = getServer().getWorld(getConfig().getString("SkyWorld"));
		spawnworld = getServer().getWorld(getConfig().getString("SpawnWorld"));
	}




	public void savePartyList()
	{
		try
		{
			SLAPI.save(prtymanager.getPartyList(), "partylist.bin");
		}catch (Exception e) 
		{
			System.out.println("Error saving the party list!");
			e.printStackTrace();
		}
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

	public IslandManager getIslandManager(){
		return islandmanager;
	}
	
	public List<ItemStack> parseChestItems(List<String> in){
		List<ItemStack> out = new ArrayList<ItemStack>();
		for(String current : in){
			String[] enchantment = current.split(" ");
			
				String[] split = current.split(":");

				ItemStack i = new ItemStack(0);
				i.setTypeId(Integer.parseInt(split[0]));
				  if (split.length > 1) {
	                  i.setAmount(Integer.parseInt(split[1]));
	                  if (split.length > 2)
	                    i.setDurability((short)Integer.parseInt(split[2]));
	                }
				
                if(enchantment.length  == 2){
                	 String[] enchant = enchantment[1].split(":");
                     Enchantment enchantmentInt = new EnchantmentWrapper(Integer.parseInt(enchant[0]));
                     int levelInt = Integer.parseInt(enchant[1]);
                     i.addUnsafeEnchantment(enchantmentInt, levelInt);
                }
                
                out.add(i);
		}
		return out;
	
	}
}

