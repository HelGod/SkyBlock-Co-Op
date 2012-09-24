package nz.Tzeentchful.SkyBlock;



import java.util.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
//import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.qgel.skySMP.Invite;
import de.qgel.skySMP.Party;

import java.util.Iterator;
/**
* Handler for the /party command
* @author Talabrek
*/
public class partyCommand implements CommandExecutor {
    private final skySMP plugin;
    private Party tempParty;
    private String tempLeader;
    private List<Invite> inviteList;
    String tPlayer;
    //private List<String> coopList;
    
    public partyCommand(skySMP plugin) {
        this.plugin = plugin;
    	inviteList = new ArrayList<Invite>();
    	inviteList.add(new Invite("NoInviter", "NoInvited"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        /* if the player has a party, temporarily store the data */
        if (hasParty(player.getName()) >= 0)
        {
      	     tempParty = plugin.getPartyList().get(hasParty(sender.getName()));
     	     tempLeader = plugin.getPartyList().get(hasParty(sender.getName())).getLeader(); 
        }
        if(split.length == 0) 
        { // do this if the command is entered with no arguments (just /party)
        	player.sendMessage(ChatColor.WHITE + "/party invite <FULL playername>" +ChatColor.YELLOW + " to invite a player.");
            if (hasParty(player.getName()) >= 0)
            { // check to see if the player is in a party
            	player.sendMessage(ChatColor.WHITE + "/party leave" +ChatColor.YELLOW + " leave your current party and return to spawn");
            	if (tempLeader.equalsIgnoreCase(sender.getName()))
            	{ // check to see if the player is party leader to display additional commands
            		player.sendMessage(ChatColor.WHITE + "/party remove <FULL playername>" +ChatColor.YELLOW + " remove a member from your party");
            		player.sendMessage(ChatColor.WHITE + "/party makeleader <FULL playername>" +ChatColor.YELLOW + " make the party member leader (transfers island)");
            		if (tempParty.getSize() < tempParty.getMax())
            		{ // show how many more people can be invited to the party
            			player.sendMessage(ChatColor.GREEN + "You can invite " + (tempParty.getMax() - tempParty.getSize()) + " more players." );
            		} else
            			player.sendMessage(ChatColor.RED + "You can't invite any more players." );
            	}
            	/* display a party list (if in a party) */
            	player.sendMessage(ChatColor.YELLOW + "Listing your party members:");
            	player.sendMessage(ChatColor.GREEN + tempLeader + " "+ ChatColor.WHITE + tempParty.getMembers());
            } else if (wasInvited(player.getName()) >= 0)
            { // check to see if this player has been invited to join a party
            	player.sendMessage(inviteList.get(wasInvited(player.getName())).getInviting() + " has invited you to a group.");
        		player.sendMessage(ChatColor.WHITE + "/party [accept/reject]" +ChatColor.YELLOW + " to accept or reject the invite.");
            }
        	return true;
        } else if (split.length == 1)
        { // do this if the command is entered with one argument	
        	   if (split[0].equals("invite")) 
        	   { // if the player uses /party invite without giving a <playername>
        		   player.sendMessage(ChatColor.YELLOW + "Use " +ChatColor.WHITE + " /party invite <playername>" + ChatColor.YELLOW + " to invite a player.");
        		   if (hasParty(player.getName()) >=0 )
        		   { // only show the next part if the player is in a party...
        			   if (tempLeader.equalsIgnoreCase(player.getName()))
        			   { // ...and is the party leader
        				   if (tempParty.getSize() < tempParty.getMax())
               			   { // show how many more people can be invited to the party
        					   player.sendMessage(ChatColor.GREEN + "You can invite " + (tempParty.getMax() - tempParty.getSize()) + " more players." );
               			   } else
               				   player.sendMessage(ChatColor.RED + "You can't invite any more players." );
        				   return true;
        			   } else
        		       { // if the player is not the party leader...
        			   player.sendMessage(ChatColor.RED + "Only the group leader can invite!");
        			   return true;
        		       }
        		   }
        	   }else if (split[0].equals("accept")) 
        	   { // if the player uses /party accept
        		   if (hasParty(player.getName()) <= 0 && wasInvited(player.getName()) >= 0)
    			   { // check to see if the player is NOT in a party and has been invited by someone
        			   if (hasParty(inviteList.get(wasInvited(player.getName())).getInviting()) == -1)
        			   { // check to see if the player who invited has a party, if not then create a new one
        				   /* add a new party to the party list with the inviter as the leader, and the inviter's island as the party's island */
        				   plugin.getPartyList().add(new Party(inviteList.get(wasInvited(player.getName())).getInviting(), player.getName(), plugin.getPlayerIsland(inviteList.get(wasInvited(player.getName())).getInviting()) ));
        				   player.sendMessage(ChatColor.GREEN + "You have joined a new party as a member!  Use /party to see the group members."); // send a message to the invited player
        				   if (Bukkit.getPlayer(inviteList.get(wasInvited(player.getName())).getInviting()).isOnline())
    					       /* send a message to the inviter if he is online (he should be, but this is just a fail-safe to avoid any null pointer exceptions) */
        					   Bukkit.getPlayer(inviteList.get(wasInvited(player.getName())).getInviting()).sendMessage(ChatColor.GREEN + player.getName() + " has joined your party!");
        			   }else
        			   { // if the inviter already has a party, add the invited person to it
        				   if (plugin.getPartyList().get(hasParty(inviteList.get(wasInvited(player.getName())).getInviting())).addMember(player.getName()))
        				   { // try to add the person into the group, if this fails it will return false
        					   player.sendMessage(ChatColor.GREEN + "You have joined a new party as a member! Use /party to see the group members.");
        				       if (Bukkit.getPlayer(inviteList.get(wasInvited(player.getName())).getInviting()).isOnline())
        				    	   /* send a message to the inviter if he is online (he should be, but this is just a fail-safe to avoid any null pointer exceptions) */
        				    	   Bukkit.getPlayer(inviteList.get(wasInvited(player.getName())).getInviting()).sendMessage(ChatColor.GREEN + player.getName() + " has joined your party!");
        				   }
        				   else
        				   { // if there was a problem adding the person, let them know
        					   player.sendMessage(ChatColor.RED + "You couldn't join the party, maybe it's full."); 
        					   return true;
        				   }
        			  }
         			  if (plugin.hasIsland(player.getName())){
        				  /* check to see if the invited player has an island, if so then remove it */
        				  plugin.deleteIsland(player.getName(), skySMP.skyworld); 
         			  }
        			  /* teleport the player to the party's island and set his home port to there */
        			  plugin.teleportGroup(inviteList.get(wasInvited(player.getName())).getInviting(), player, skySMP.skyworld);
        			  player.performCommand("sethome"); // set this as the player's new home
        			  player.getInventory().clear();
        			  /* add the player to the island's worldguard protection */
        			  DefaultDomain owners = plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(inviteList.get(wasInvited(player.getName())).getInviting() + "Island").getOwners();
        			  owners.addPlayer(player.getName());
        			  plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(inviteList.get(wasInvited(player.getName())).getInviting() + "Island").setOwners(owners);
  					  inviteList.remove(wasInvited(player.getName())); // remove the invite from the invite list
  					  plugin.savePartyList(); // save the current group list to the file
  					  return true;    
    			  }else
    			  {
    				  player.sendMessage(ChatColor.RED+ "You can't use that command right now.");
    				  return true;
    			  }	      
        	   }
    	else if (split[0].equals("reject")) 
    	{ // if the player uses /party reject
    		if (wasInvited(player.getName()) >= 0)
    		{ // only do something if the player has received an invite
    		    player.sendMessage(ChatColor.YELLOW + "You have rejected the party invitation.");
    		    if (Bukkit.getPlayer(inviteList.get(wasInvited(player.getName())).getInviting()).isOnline()){
    		    	/* send a message to the inviter if he is online (he should be, but this is just a fail-safe to avoid any null pointer exceptions) */
    		    	Bukkit.getPlayer(inviteList.get(wasInvited(player.getName())).getInviting()).sendMessage(ChatColor.RED + player.getName() + " has rejected your party invite!");
    		    }
    		    inviteList.remove(wasInvited(sender.getName()));
    		    } else
    			player.sendMessage(ChatColor.RED + "You haven't been invited."); 
            return true;
    	}
        /***************************************************************************    
         ***                         OPERATOR COMMANDS                           ***
         ***************************************************************************/   
    	 else if (split[0].equalsIgnoreCase("partypurge")) 
    	{ // clear the party list
    		if (player.isOp())
    		{
    			player.sendMessage(ChatColor.RED+ "Deleting all parties!");
    			partyPurge();
    		}else
    			player.sendMessage(ChatColor.RED+ "You can't access that command!");
            return true;
    	}else if (split[0].equalsIgnoreCase("cleanparty")) 
    	{ // clean parties with only 1 member
    		if (player.isOp())
    		{
    			player.sendMessage(ChatColor.RED+ "Deleting all parties with less than 2 members!");
    			cleanParty();
    		}else
    			player.sendMessage(ChatColor.RED+ "You can't access that command!");
            return true;
    	}else if (split[0].equalsIgnoreCase("invitepurge")) 
    	{ // clear the invite list
    		if (player.isOp())
    		{
    			player.sendMessage(ChatColor.RED+ "Deleting all invites!");
    			invitePurge();
    		}else
    			player.sendMessage(ChatColor.RED+ "You can't access that command!");
            return true;
    	}else if (split[0].equalsIgnoreCase("partydebug")) 
    	{ // display a list of all the parties
    		if (player.isOp())
    		{
    			player.sendMessage(ChatColor.RED+ "Checking Parties.");
    			partyDebug(player, "none");
    		}else
    			player.sendMessage(ChatColor.RED+ "You can't access that command!");
            return true;
    	}else if (split[0].equalsIgnoreCase("invitedebug")) 
    	{ // display a list players who have invited or been invited to a party
    		if (player.isOp())
    		{
    			player.sendMessage(ChatColor.RED+ "Checking Invites.");
    			inviteDebug(player);
    		}else
    			player.sendMessage(ChatColor.RED+ "You can't access that command!");
            return true;
        /***************************************************************************    
         ***                         NORMAL COMMANDS                           ***
         ***************************************************************************/ 
    	}else if (split[0].equals("leave")) 
    	{ // if the player uses /party leave
    		if (hasParty(player.getName().toLowerCase()) >= 0)
            { // check to see if the player is in a party
    			if (plugin.getPartyList().get(hasParty(player.getName())).removeMember(player.getName()) == 0)
    			{ // check to see if the person leaving is the leader, then remove everyone else
    					Iterator<String> ent = tempParty.getMembers().iterator();
    					/* create a new owner list and add only the leader */
	       	    		DefaultDomain owners = new DefaultDomain();
	       	    		owners.addPlayer(player.getName());
	       	    		/* set the player as the island's only owner in the protected region */
	       	    		plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(player.getName() + "Island").setOwners(owners);
    					while (ent.hasNext())
    					{ // iterate through the group list and teleport any online members to the spawn
    						tPlayer = ent.next();
    					    if (Bukkit.getPlayer(tPlayer) != null)
    					    {
    					    	
    					    	Bukkit.getPlayer(tPlayer).sendMessage(ChatColor.RED + "You have been removed from your party by " + player.getName() + "!");
    					    	Bukkit.getPlayer(tPlayer).getInventory().clear();
    					    	Bukkit.getPlayer(tPlayer).teleport(skySMP.spawnworld.getSpawnLocation());
    					    	Bukkit.getPlayer(tPlayer).performCommand("sethome");
    					   }
    					}
    					player.sendMessage(ChatColor.YELLOW + "You have left your party, all members have been removed from your island!");
    					plugin.getPartyList().remove(hasParty(player.getName()));
    					plugin.savePartyList();
    					return true;
                }else
                { // if the player is not the leader
                	//List<Entity> Entities;
    				//Iterator<Entity> ent2;
    				/* remove this player from the leader's worldguard protection */
    				DefaultDomain owners = plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(tempLeader + "Island").getOwners();
    				owners.removePlayer(player.getName());
	       	    	plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(tempLeader + "Island").setOwners(owners);
	       	    	/* remove the player's items and teleport him to the spawn */
                    player.getInventory().clear();
	       	    	player.teleport(skySMP.spawnworld.getSpawnLocation());
    				player.performCommand("sethome");
    				/* send a message to the player and the group leader about the member leaving */
    				player.sendMessage(ChatColor.YELLOW + "You have left your party and returned to the player spawn.");
    			    if (Bukkit.getPlayer(tempLeader) != null)
    			    	Bukkit.getPlayer(tempLeader).sendMessage(ChatColor.RED + player.getName() +" has left your party!");
    			    tempParty.removeMember(player.getName());
    			    if (plugin.getPartyList().get(hasParty(tempLeader)).getSize() < 2)
    			    {
    			    	plugin.getPartyList().remove(hasParty(tempLeader));
        			    if (Bukkit.getPlayer(tempLeader).isOnline())
        			    	Bukkit.getPlayer(tempLeader).sendMessage(ChatColor.RED + "Your party has been disbanded!");
    			    }
                 }
            }else
			{
				player.sendMessage(ChatColor.RED+ "You are not in a party!");
				return true;
			}		
    	}
        }else if (split.length == 2) 
        { // do this if the command is entered with two arguments
        		if (split[0].equalsIgnoreCase("invite"))
        		{ // if the player uses /party invite [player] *** split[1] is the invited player
        			if (Bukkit.getPlayer(split[1]) == null)
            		{ // check to see if this is a valid player
            			 player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
            			 return true;
            		}
        			if (!Bukkit.getPlayer(split[1]).isOnline())
            		{ // check to see if this is a valid player
            			 player.sendMessage(ChatColor.RED + "That player is offline or doesn't exist.");
            			 return true;
            		}
        			if (!plugin.hasIsland(player))
            		{ // check to see if the player has a registered island
            			 player.sendMessage(ChatColor.RED + "You must have an island to create a party.");
            			 return true;
            		}
        			if (player.getName().equalsIgnoreCase(split[1]))
            		{ // make sure that the player isn't trying to invite themselves
            			 player.sendMessage(ChatColor.RED + "You can't invite yourself!");
            			 return true;
            		}
        			if (hasParty(player.getName()) >= 0)
        			{ // check to see if the inviting player has a party (do this if they have a party)
        				if (plugin.getPartyList().get(hasParty(player.getName())).getLeader().equalsIgnoreCase(player.getName()))
        				{ // check to see if the inviting player is the leader of his party
        					if (hasParty(split[1]) == -1)
        					{ // check to see if the invited player has a party
        						if (tempParty.getSize() < tempParty.getMax())
                				{ // check to see if the party is full
        							if (hasInvited(player.getName()) >= 0)
        							{ // check to see if the inviting player has invited anyone else and remove that invite from the list
        								inviteList.remove(hasInvited(player.getName()));
        								player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
        							}
        							inviteList.add(new Invite(split[1], player.getName())); // create a new invite on the list
        							/* send a message to the invited player */
        							Bukkit.getPlayer(split[1]).sendMessage(player.getName() + " has invited you to join a group!");
        							Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/party accept" +ChatColor.YELLOW + " to accept and join the party.");
        							Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/party reject" +ChatColor.YELLOW + " to ignore the invite.");
        							Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + "WARNING: You will lose your current island if you accept!");
         					    } else
                					player.sendMessage(ChatColor.RED + "Your party is full, you can't invite anyone else." );
        					} else
        						player.sendMessage(ChatColor.RED + "That player is already in another party." );
        				} else
        					player.sendMessage(ChatColor.RED + " Only the party leader may invite new players." );
         		   } else if (hasParty(player.getName()) == -1)
         		   { // check to see if the inviting player has a party (do this if they DO NOT have a party)
         			   if (hasParty(split[1]) == -1)
         			   {
         				   if (hasInvited(player.getName()) > 0)
         				   { // check to see if the inviting player has invited anyone else and remove that invite from the list
         					   inviteList.remove(hasInvited(player.getName()));
         					   player.sendMessage(ChatColor.YELLOW + "Removing your previous invite.");
         				   }
         				   inviteList.add(new Invite(split[1], player.getName())); // create a new invite on the list
         				   /* send a message to the invited player */
         				   Bukkit.getPlayer(split[1]).sendMessage(player.getName() + " has invited you to join a group!");
         				   Bukkit.getPlayer(split[1]).sendMessage(ChatColor.WHITE + "/party [accept/reject]" +ChatColor.YELLOW + " to accept or reject the invite.");
         				   Bukkit.getPlayer(split[1]).sendMessage(ChatColor.RED + "WARNING: You will lose your current island!");
         			   }else
         				   player.sendMessage(ChatColor.RED + "That player is already in another party." );
         			   return true;
         		   }
        			
        			
         		   else
         		   {
         			   player.sendMessage(ChatColor.RED + "Only the group leader can invite!");
         			   return true;
         		   }
        	} else if (split[0].equalsIgnoreCase("remove"))
    		{  // if the player uses /party remove [player] *** split[1] is the invited player
        		if (hasParty(player.getName()) >= 0)
        		{ // check to see if the player is in a party
        			if (tempLeader.equalsIgnoreCase(player.getName()))
     		   		{ // check to see if the player is the party leader
        				if (tempParty.hasMember(split[1]))
     			   		{ // check to see if the removed player is in the removing player's party
        					if (player.getName().equalsIgnoreCase(split[1]))
        					{ // check to see if the player is trying to remove himself
        						player.sendMessage(ChatColor.RED + "Use /party leave to leave the group.");
        						return true;
        					}	 
        					if(plugin.getPartyList().get(hasParty(player.getName())).removeMember(split[1]) > 1 )
        					{ // try to remove the member from the group
        						if (Bukkit.getPlayer(split[1]) != null)
        						{ // check to see if the player is online, if they are then remove their stuff and teleport them
        							Bukkit.getPlayer(split[1]).getInventory().clear();
        							Bukkit.getPlayer(split[1]).teleport(skySMP.spawnworld.getSpawnLocation());
        							Bukkit.getPlayer(split[1]).performCommand("sethome");
        						}
        						if (Bukkit.getPlayer(tempLeader) != null)
        							Bukkit.getPlayer(tempLeader).sendMessage(ChatColor.RED + player.getName() +" has been removed from the group.");
        						/* update the worldguard protection */
        						DefaultDomain owners = plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(player.getName() + "Island").getOwners();
        						owners.removePlayer(split[1]);
        						plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(player.getName() + "Island").setOwners(owners);
        	    			    if (plugin.getPartyList().get(hasParty(player.getName())).getSize() < 2)
        	    			    {
        	    			    	plugin.getPartyList().remove(hasParty(player.getName()));
        	        			    player.sendMessage(ChatColor.RED + "Your party has been disbanded!");
        	    			    }
        						plugin.savePartyList();
        					} else
        						player.sendMessage(ChatColor.RED + "There was an error removing that party member.");
     			   		} else
     			   			player.sendMessage(ChatColor.RED + "That player is not in your party!");
     		   		} else
     		   			player.sendMessage(ChatColor.RED + "Only the party leader can remove!");
        		} else
        			player.sendMessage(ChatColor.RED + "You must be in a party to remove someone!");
    	} else if (split[0].equalsIgnoreCase("makeleader"))
		{
    		if (hasParty(player.getName()) >= 0)
    		{ // check to see if the player is in a party
    			if (tempLeader.equalsIgnoreCase(player.getName()))
 		   		{ // check to see if the player is the party leader
    				if (tempParty.hasMember(split[1]))
 			   		{ // check to see if the new leader is in the current leader's party
    					if (plugin.getPartyList().get(hasParty(sender.getName())).changeLeader(player.getName(), split[1]))
    					{ // change the leader in the party list to the new player
    						if (Bukkit.getPlayer(split[1]) != null)
    							Bukkit.getPlayer(split[1]).sendMessage(ChatColor.GREEN + "You are now the leader of the party.");
    						plugin.savePartyList(); // save the party changes to the party file
    						plugin.registerPlayerIsland(split[1].toLowerCase(), plugin.getPlayerIsland(player.getName())); // register the island location to the new leader
    						plugin.unregisterPlayerIsland(player.getName()); // unregister the current player from the island
    						/* create a new worldguard region with the new leader's name and transfer everything to it */
    						try
    						{
    							ProtectedRegion region2 = null;
    							region2 = new ProtectedCuboidRegion(split[1] +"Island", plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(player.getName() + "Island").getMinimumPoint(), plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(player.getName() + "Island").getMaximumPoint());
    							region2.setOwners(plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion(player.getName() + "Island").getOwners());
    							region2.setParent(plugin.getWorldGuard().getRegionManager(skySMP.skyworld).getRegion("__Global__"));
    							region2.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(plugin.getWorldGuard(), sender, "You are entering a protected island area. (" +split[1] + ")"));
    							region2.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(plugin.getWorldGuard(), sender, "You are leaving a protected island area. (" +split[1] + ")"));
    							region2.setFlag(DefaultFlag.PVP, DefaultFlag.PVP.parseInput(plugin.getWorldGuard(), sender, "deny"));
    							plugin.getWorldGuard().getRegionManager(skySMP.skyworld).removeRegion(player.getName() + "Island");
    							plugin.getWorldGuard().getRegionManager(skySMP.skyworld).addRegion(region2);
    						}catch(Exception e)
    						{ 
    							player.sendMessage(ChatColor.RED + "There was an error transfering the protected region!");
    							System.out.println("Error transferring WorldGuard Protected Region from (" + player.getName() + ") to (" + split[1] + ")");
    					    	return true; 
    					    }
    						return true;
    					} else
    						player.sendMessage(ChatColor.RED + "There was an unknown problem changing leaders!");
    				} else
    					player.sendMessage(ChatColor.RED + "That player is not in your party!");
 		   		} else
 		   			player.sendMessage(ChatColor.RED + "You are not the party leader!");
 		   }else
 			   player.sendMessage(ChatColor.RED + "Could not change leaders.");

	}else if (split[0].equalsIgnoreCase("partydebug")) { // reject an invite
		if (player.isOp())
		{
			player.sendMessage(ChatColor.RED+ "Checking Party of " + split[1]);
			partyDebug(player, split[1]);
		}else
			player.sendMessage(ChatColor.RED+ "You can't access that command!");
        return true;
	}     
       } 
        return true;
    }

    private int hasParty(String playerName)
    { // check to see if the given player name is a member of a party. Return the index of their party (less than 0 means they are not in a party)
     try{	
		for (int i = 0; i < plugin.getPartyList().size(); i++)
		{ // search through the party list and return the index of this player's party
			if (plugin.getPartyList().get(i).hasMember(playerName))
				return i;
		}
		return -1;
     }catch(Exception e){
    	 return -2;
     }
    }
        
    private int wasInvited(String playerName)
    { // check to see if the given player name has been invited to join a party.  Return the index of the invite list (less than 0 means they haven't been invited)
    	
    	
    	for (int i = 0; i < inviteList.size(); i++){
			if (inviteList.get(i).getInvited().equalsIgnoreCase(playerName))
				return i;
    	}																							
		return -1;
    }
    
    private int hasInvited(String playerName)
    { // check to see if the given player has invited someone else to join a party. Return the index of the invite list (less than 0 means they haven't invited anyone)
    	for (int i = 0; i < inviteList.size(); i++)
			if (inviteList.get(i).getInviting().equalsIgnoreCase(playerName))
				return i;
		return -1;
    }
    
    private void inviteDebug(Player player)
    { // display the invite list
    	for (int i = 0; i < inviteList.size(); i++)
    	{
			player.sendMessage("Inviting: " + inviteList.get(i).getInviting() + " Invited: " + inviteList.get(i).getInvited());
    	}
    }
    
    private void partyPurge()
    { // clear the party list **WARNING: EMERGENCY USE ONLY**
    	plugin.getPartyList().clear();
    }
    
    private void cleanParty()
    { // remove any party with only 1 person
		for (int i = 0; i < plugin.getPartyList().size(); i++)
		{
			if (plugin.getPartyList().get(i) != null)
			{
				if (plugin.getPartyList().get(i).getSize() < 2)
				{
					if (Bukkit.getPlayer(plugin.getPartyList().get(i).getLeader()) != null)
						Bukkit.getPlayer(plugin.getPartyList().get(i).getLeader()).sendMessage(ChatColor.RED + "Your party has been disbanded because it has less than 2 members!");
					plugin.getPartyList().remove(i);
				}
			}
		}
    }
    
    private void invitePurge()
    { // clear the invite list, useful if invites are broken
    	inviteList.clear();
    	inviteList.add(new Invite("NoInviter", "NoInvited"));
    }
    
    private void partyDebug(Player player, String name)
    { // display the entire list or the party of a given player
    	if (name.equalsIgnoreCase("none"))
    	{ // if 'none' is given as the player name, show the entire list (large lists will be clipped)
    		for (int i = 0; i < plugin.getPartyList().size(); i++)
    		{
    			player.sendMessage("Leader: " + plugin.getPartyList().get(i).getLeader() + " Members: " + plugin.getPartyList().get(i).getMembers());
    		}
    	}else
    	{ // display the party information for the given player
    		if (hasParty(name.toLowerCase()) >= 0)
    		    player.sendMessage("Leader: " + plugin.getPartyList().get(hasParty(name.toLowerCase())).getLeader() + " Members: " + plugin.getPartyList().get(hasParty(name.toLowerCase())).getMembers());
    		else
    			player.sendMessage("Invalid player, or Error");
    	}
    }
          
}