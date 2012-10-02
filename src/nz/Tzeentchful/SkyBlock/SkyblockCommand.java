package nz.Tzeentchful.SkyBlock;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.qgel.skySMP.Party;

public class SkyblockCommand implements CommandExecutor{

	private skySMP plugin;

	public SkyblockCommand(skySMP plugin) {
		this.plugin = plugin;
	}
	PartyManager prtymanager = new PartyManager();
	IslandManager islandmanager = new IslandManager(plugin);

	private Map<Player, String> waiting = new HashMap<Player, String>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;

		;
		if(args.length == 0){
			Bukkit.dispatchCommand(sender, "skyblock help");
			return true;
		}else{
			if(sender instanceof Player){
				player = (Player) sender;

				if(args[0].equals("new") || args[0].equals("n")){
					if(player.hasPermission("skyblock.new")){
						if(!prtymanager.inParty(player.getName())){
						if(!islandmanager.hasIsland(player.getName())){
							islandmanager.createIsland(player);
							return true;
						}else{
							player.sendMessage(ChatColor.DARK_RED +"You allready have a island!");
							return true;
						}
						}else if(prtymanager.hasParty(player.getName())){
							if(!islandmanager.hasIsland(player.getName())){
								islandmanager.createIsland(player);
								return true;
							}else{
								player.sendMessage(ChatColor.DARK_RED +"The party allready has a island!");
								return true;
							}
						}else{
							player.sendMessage(ChatColor.DARK_RED +"Only the party leader can create a new island!");
							return true;
						}
					}
				}else if(args[0].equals("tp") || args[0].equals("t")){
					if(player.hasPermission("skyblock.tp")){
						if(prtymanager.inParty(player.getName())){
							if(prtymanager.hasParty(player.getName())){
								if(islandmanager.hasIsland(player.getName())){
									islandmanager.teleportHome(player);
									return true;
								}else{
									player.sendMessage(ChatColor.DARK_RED +"The party currently doesn't have a island!");
									return true;	
								}
							}else{
								Party prtyin = prtymanager.getPlayerParty(player.getName());
								if(islandmanager.hasIsland(Bukkit.getPlayer(prtyin.getLeader()))){
									islandmanager.teleportGroup(prtyin.getLeader(), player);
									return true;
								}else{
									player.sendMessage(ChatColor.DARK_RED +"The party currently doesn't have a island!");
									return true;
								}
							}
						}else if(islandmanager.hasIsland(player.getName())){
							islandmanager.teleportHome(player);
							return true;
						}else{
							player.sendMessage(ChatColor.DARK_RED +"You don't have a island!");
							return true;
						}
					}	
				}else if(args[0].equals("delete") || args[0].equals("d")){
					if(player.hasPermission("skyblock.delete")){
						if(islandmanager.hasIsland(player.getName())){
							if(prtymanager.hasParty(player.getName())){
								player.sendMessage(ChatColor.GOLD +"Are you sure you want to delete the party's island?");
								player.sendMessage(ChatColor.DARK_RED +"/skyblock yes"+ChatColor.GOLD+" or "+ ChatColor.DARK_RED +"/skyblock no");
								waiting.put(player, "islanddelprty");
								return true;
							}else{
								if(prtymanager.inParty(player.getName())){
									player.sendMessage(ChatColor.DARK_RED +"Only the party leader can delete the party's island!");
									return true;
								}else{
								player.sendMessage(ChatColor.GOLD +"Are you sure you want to delete your island?");
								player.sendMessage(ChatColor.DARK_RED +"/skyblock yes"+ChatColor.GOLD+" or "+ ChatColor.DARK_RED +"/skyblock no");
								waiting.put(player, "islanddel");
								return true;
								}
							}
						}else{
							if(prtymanager.inParty(player.getName())){
								player.sendMessage(ChatColor.DARK_RED +"Only the party leader can delete the island!");
								return true;
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have a island!");
								return true;
							}
						}
					}	
				}else if(args[0].equals("party") || args[0].equals("p")){
					if(args.length >= 2){
						if(args[1].equals("new") || args[1].equals("n")){
							if(player.hasPermission("skyblock.party.new")){
								if(!prtymanager.hasParty(player.getName())){
									if(!prtymanager.inParty(player.getName())){
										if(!islandmanager.hasIsland(player)){
											islandmanager.createIsland(player);
											prtymanager.createNewParty(player.getName(), islandmanager.getPlayerIsland(player.getName()));
											islandmanager.teleportHome(player);
											player.sendMessage(ChatColor.DARK_GREEN+"Sucessfuly created a new party. Use"+ChatColor.GOLD+" /skyblock party invite"+ChatColor.DARK_GREEN+" to invite memebers.");
											return true;
										}else{
											player.sendMessage(ChatColor.GOLD+"DO you want to create a new island or use your existing one?");
											player.sendMessage(ChatColor.DARK_RED+"/skyblock yes " +ChatColor.GOLD+"- Create new island.");
											player.sendMessage(ChatColor.DARK_RED+"/skyblock no " +ChatColor.GOLD+ "- Use your existing island.");
											waiting.put(player, "partynewi");
											return true;
										}
									}else{
										player.sendMessage(ChatColor.DARK_RED +"You are allready in a party!");
										return true;
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You allready have a party!");
									return true;
								}
							}
						}else if(args[1].equals("invite") || args[1].equals("i")){
							if(player.hasPermission("skyblock.party.invite")){
								if(prtymanager.hasParty(player.getName())){
									if(args.length >= 3){
										Player invplayer = Bukkit.getPlayer(args[2]);
										if(invplayer != null){
											if(!invplayer.equals(player)){
												if(!(prtymanager.inParty(invplayer.getName()) || prtymanager.hasParty(invplayer.getName()))){
													if(!prtymanager.hasInvite(invplayer.getName())){
														if(invplayer.isOnline()){
															prtymanager.invitePlayer(invplayer, player);
															player.sendMessage(ChatColor.GOLD +"Invited " + invplayer.getDisplayName() + ChatColor.GOLD + " to the party.");
															return true;
														}else{
															player.sendMessage(ChatColor.DARK_RED +"That player is offline!");
															return true;
														}
													}else{
														player.sendMessage(ChatColor.DARK_RED +"That player allready has a pending invite.");
														return true;
													}
												}else{
													player.sendMessage(ChatColor.DARK_RED +"That player is allready in a party!");
													return true;
												}
											}else{
												player.sendMessage(ChatColor.DARK_RED +"You can't invite youself!");
												return true;
											}
										}else{
											player.sendMessage(ChatColor.DARK_RED +"Could not find invited player!");
											return true;
										}

									}else{
										player.sendMessage(ChatColor.DARK_RED +"You did't specify a player to invite!");
										return true;	
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You don't have a Party!");
									return true;
								}
							}
						}else if(args[1].equals("accept") || args[1].equals("a")){
							if(player.hasPermission("skyblock.party.accept")){
								if(prtymanager.hasInvite(player.getName())){
									Party invitedprty = prtymanager.getPlayerParty(prtymanager.getInvite(player.getName()).getInviting());
									Party original = invitedprty;
									if(!(invitedprty.getMax() <= invitedprty.getSize())){
										if(islandmanager.hasIsland(player)){
											islandmanager.deleteIsland(player.getName());
										}

										invitedprty.addMember(player.getName());
										prtymanager.setParty(original, invitedprty);

										islandmanager.addOwnerToIsland(invitedprty.getLeader(), player);
										islandmanager.teleportGroup(invitedprty.getLeader(), player);

										prtymanager.removeInvite(player.getName());
										return true;
									}else{
										player.sendMessage(ChatColor.DARK_RED +"Party is full!");
										prtymanager.removeInvite(player.getName());
										return true;
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You don't have a invite!");
									return true;
								}
							}
						}else if(args[1].equals("decline") || args[1].equals("d")){
							if(player.hasPermission("skyblock.party.decline")){
								if(prtymanager.hasInvite(player.getName())){
									Player leader = Bukkit.getPlayer(prtymanager.getInvite(player.getName()).getInviting());
									leader.sendMessage(ChatColor.DARK_RED+ player.getName() +" Declined your party invite.");
									prtymanager.removeInvite(player.getName());
									return true;
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You don't have a invite!");
									return true;
								}
							}
						}else if(args[1].equals("leave") || args[1].equals("l")){
							if(player.hasPermission("skyblock.party.leave")){
								if(prtymanager.inParty(player.getName()) || prtymanager.hasParty(player.getName())){

									if(prtymanager.hasParty(player.getName())){
										Party plrprty = prtymanager.getPlayerParty(player.getName());
										if(plrprty.getSize() > 1){
											player.sendMessage(ChatColor.GOLD +"Are you sure? The new party leader will be "+plrprty.getMembers().get(0));
											waiting.put(player, "partyleave");
											return true;
										}else{
											player.sendMessage(ChatColor.GOLD +"Are you sure?");
											waiting.put(player, "partyleave");
											return true;
										}
									}else{
										player.sendMessage(ChatColor.GOLD +"Are you sure you want to leave the party?");
										waiting.put(player, "partyleave");
										return true;
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You aren't in a party!");
									return true;
								}
							}
						}else if(args[1].equals("disband") || args[1].equals("d")){
							if(prtymanager.hasParty(player.getName())){
								waiting.put(player, "partydisband");
								player.sendMessage(ChatColor.GOLD +"Are you sure you want to disband the party?");
								player.sendMessage(ChatColor.DARK_RED +"/skyblock yes"+ChatColor.GOLD+" or "+ ChatColor.DARK_RED +"/skyblock no");
								return true;
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You dont have a party to disband!");
								return true;
							}
						}else{
							player.performCommand("skyblock party");
							return true;
						}
					}else{
						player.sendMessage(ChatColor.YELLOW + "/===================================================\\");//Don't touch the double \\ it's to prevent escape chars
						player.sendMessage(ChatColor.DARK_AQUA + "              Skyblock Co-Op" + " V 5.0");
						player.sendMessage(ChatColor.YELLOW + "\\===================================================/");//Don't touch the double \\ it's to prevent escape chars
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party new" + ChatColor.AQUA + " - Creates a new party." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party leave" + ChatColor.AQUA + " - Leave the party you are in." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party invite" + ChatColor.AQUA + " - Invite someone to your party." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party accept" + ChatColor.AQUA + " - Accept a party invite." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party decline" + ChatColor.AQUA + " - Declines a party invite" );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party disband" + ChatColor.AQUA + " - Disbands your party." );
						return true;
					}
				/*}else if(args[0].equals("admin") || args[0].equals("a")){
					if(player.hasPermission("skyblock.admin")){
						if(args[1].equals("tp") || args[0].equals("t")){
							
							return true;
						}
						 
					}*/
				}else if(args[0].equals("yes") || args[0].equals("y")){
					if(waiting.containsKey(player)){
						String reason = waiting.get(player);
						if(reason.equals("partynewi")){
							islandmanager.deleteIsland(player.getName());
							islandmanager.createIsland(player);

							prtymanager.createNewParty(player.getName(), islandmanager.getPlayerIsland(player.getName()));
							waiting.remove(player);
							return true;
						}else if(reason.equals("partydisband")){
							Party disprty = prtymanager.getPlayerParty(player.getName());

							for(String prtyplr : disprty.getMembers()){
								Player cplr = Bukkit.getPlayer(prtyplr);
								if(cplr != null){
									if(cplr.isOnline()){
										cplr.sendMessage(ChatColor.DARK_RED+"Your party has been disbanded!");
										if(cplr.getWorld().equals(skySMP.skyworld)){
											cplr.teleport(skySMP.spawnworld.getSpawnLocation());
										}
									}
								}
							}

							Player leader = Bukkit.getPlayer(disprty.getLeader());
							if(leader.getWorld().equals(skySMP.skyworld)){
								leader.teleport(skySMP.spawnworld.getSpawnLocation());
							}

							leader.sendMessage(ChatColor.GOLD+"You disbanded the party.");
							prtymanager.removeParty(player.getName());
							islandmanager.deleteIsland(player.getName());
							waiting.remove(player);
							return true;
						}else if(reason.equals("partyleave")){
							Party newprty = prtymanager.getPlayerParty(player.getName());
							if(!prtymanager.hasParty(player.getName())){
								newprty.removeMember(player.getName());
								prtymanager.setParty(prtymanager.getPlayerParty(player.getName()), newprty);
								islandmanager.removeOwnerFromIsland(newprty.getLeader(), player.getName());
								if(player.getWorld().equals(skySMP.skyworld)){
									player.teleport(skySMP.spawnworld.getSpawnLocation());
								}
							}else{
								if(newprty.getSize() > 1){
									newprty.changeLeader(player.getName(), newprty.getMembers().get(0));
									newprty.removeMember(player.getName());
									prtymanager.setParty(prtymanager.getPlayerParty(player.getName()), newprty);
									islandmanager.removeOwnerFromIsland(newprty.getLeader(), player.getName());
									islandmanager.unprotectIsland(player.getName());
									islandmanager.unregisterPlayerIsland(player.getName());
									islandmanager.registerPlayerIsland(newprty.getLeader(), newprty.getIsland());
									
									islandmanager.unprotectIsland(newprty.getLeader());//incase they have a region allready
									islandmanager.protectIsland(sender, newprty.getLeader());
									if(player.getWorld().equals(skySMP.skyworld)){
										player.teleport(skySMP.spawnworld.getSpawnLocation());
									}
								}else{
									if(player.getWorld().equals(skySMP.skyworld)){
										player.teleport(skySMP.spawnworld.getSpawnLocation());
									}
									
									player.sendMessage(ChatColor.GOLD+"The party has been disbaned.");
									prtymanager.removeParty(player.getName());
									islandmanager.deleteIsland(player.getName());
									waiting.remove(player);
									return true;
								}
							}
							if(player.getWorld().equals(skySMP.skyworld)){
								player.teleport(skySMP.spawnworld.getSpawnLocation());
							}
							waiting.remove(player);
							player.sendMessage(ChatColor.GOLD+"You left the party.");

							for(String cmem : newprty.getMembers()){
								Player cplayer = Bukkit.getPlayerExact(cmem);
								if(cplayer != null){
									if(cplayer.isOnline()){
										cplayer.sendMessage(ChatColor.GOLD + player.getName() + " Left the party.");
									}
								}
							}
							return true;
						}else if(reason.equals("islanddel")){
							if(player.getWorld().equals(skySMP.skyworld)){
								player.teleport(skySMP.spawnworld.getSpawnLocation());
							}

							islandmanager.deleteIsland(player.getName());
							waiting.remove(player);
							player.sendMessage(ChatColor.GOLD+"Island deleted.");
							return true;
						}else if(reason.equals("islanddelprty")){
							
							Party plrprty = prtymanager.getPlayerParty(player.getName());
							
							for(String cmem : plrprty.getMembers()){
								if(!cmem.equals("EmptySlot")){
									Player mem = Bukkit.getPlayer(cmem);
									if(mem.getWorld().equals(skySMP.skyworld)){
										mem.teleport(skySMP.spawnworld.getSpawnLocation());
									}
									mem.sendMessage(ChatColor.GOLD+"The party island has been deleted by the leader.");
								}
							}
							if(player.getWorld().equals(skySMP.skyworld)){
								player.teleport(skySMP.spawnworld.getSpawnLocation());
							}

							islandmanager.deleteIsland(player.getName());
							waiting.remove(player);
							player.sendMessage(ChatColor.GOLD+"Party Island deleted.");
							return true;
						}
					}else{
						player.sendMessage(ChatColor.DARK_RED + "You don't need to confirm anything!");
						return true;
					}

				}else if(args[0].equals("no") || args[0].equals("n")){
					if(waiting.containsKey(player)){
						String reason = waiting.get(player);
						if(reason.equals("partynewi")){
							if(islandmanager.hasIsland(player.getName())){
								prtymanager.createNewParty(player.getName(), islandmanager.getPlayerIsland(player.getName()));
								islandmanager.teleportHome(player);
							}else{
								islandmanager.createIsland(player);
								prtymanager.createNewParty(player.getName(), islandmanager.getPlayerIsland(player.getName()));
								player.sendMessage(ChatColor.GOLD + "For some reason you don't have a island to use. Creating a new one.");
								islandmanager.teleportHome(player);
							}
							waiting.remove(player);
							return true;
						}else if(reason.equals("partydisband")){
							player.sendMessage(ChatColor.DARK_RED + "Canceled party disband.");
							waiting.remove(player);
							return true;
						}else if(reason.equals("partyleave")){
							player.sendMessage(ChatColor.DARK_RED + "Canceled party leave.");
							waiting.remove(player);
							return true;
						}else if(reason.equals("islanddel")){
							player.sendMessage(ChatColor.DARK_RED + "Canceled removal of island.");
							waiting.remove(player);
							return true;
						}
					}else{
						player.sendMessage(ChatColor.DARK_RED + "You don't need to confirm anything!");
						return true;
					}
				}
			}
			if(args[0].equals("help") || args[0].equals("h")){
				player.sendMessage(ChatColor.YELLOW + "/===================================================\\");//Don't touch the double \\ it's to prevent escape chars
				player.sendMessage(ChatColor.DARK_AQUA + "           Skyblock Co-Op" + " V 5.0");
				player.sendMessage(ChatColor.YELLOW + "\\===================================================/");//Don't touch the double \\ it's to prevent escape chars
				player.sendMessage(ChatColor.DARK_RED + "/skyblock help" + ChatColor.AQUA + " - Shows this help menu." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock new" + ChatColor.AQUA + " - Creates a new Skyblock island." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock tp" + ChatColor.AQUA + " - Teleports you to your island." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock delete" + ChatColor.AQUA + " - Deletes your current island." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock party" + ChatColor.AQUA + " - Shows help for the party commands." );
				//player.sendMessage(ChatColor.DARK_RED + "/skyblock admin" + ChatColor.AQUA + " - Manages the Skyblock plugin." );
				return true;
			}
		}
		return false;
	}
}