package nz.Tzeentchful.SkyBlock;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

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
		if(args.length == 0){
			Bukkit.dispatchCommand(sender, "skyblock help");
			return true;
		}else{
			if(sender instanceof Player){
				player = (Player) sender;

				if(args[0].equalsIgnoreCase("new") || args[0].equals("n")){
					if(player.hasPermission("skyblock.user.new")){
						if(!prtymanager.inParty(player.getName())){
							if(!islandmanager.hasIsland(player.getName())){
								islandmanager.createIsland(player);
								return true;
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You already have a island!");
								return true;
							}
						}else if(prtymanager.hasParty(player.getName())){
							if(!islandmanager.hasIsland(player.getName())){
								islandmanager.createIsland(player);
								return true;
							}else{
								player.sendMessage(ChatColor.DARK_RED +"The party already has a island!");
								return true;
							}
						}else{
							player.sendMessage(ChatColor.DARK_RED +"Only the party leader can create a new island!");
							return true;
						}
					}else{
						player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
						return true;
					}
				}else if(args[0].equalsIgnoreCase("home") || args[0].equals("h")){
					if(player.hasPermission("skyblock.user.home")){
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
								if(islandmanager.hasIsland(prtyin.getLeader())){
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
					}else{
						player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
						return true;
					}	
				}else if(args[0].equalsIgnoreCase("delete") || args[0].equals("d")){
					if(player.hasPermission("skyblock.user.delete")){
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
					}else{
						player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
						return true;
					}
				}else if(args[0].equalsIgnoreCase("party") || args[0].equals("p")){
					if(args.length >= 2){
						if(args[1].equals("new") || args[1].equals("n")){
							if(player.hasPermission("skyblock.user.party.new")){
								if(!prtymanager.hasParty(player.getName())){
									if(!prtymanager.inParty(player.getName())){
										if(!islandmanager.hasIsland(player.getName())){
											islandmanager.createIsland(player);
											prtymanager.createNewParty(player.getName(), islandmanager.getPlayerIsland(player.getName()));
											islandmanager.teleportHome(player);
											player.sendMessage(ChatColor.DARK_GREEN+"Sucessfuly created a new party. Use"+ChatColor.GOLD+" /skyblock party invite"+ChatColor.DARK_GREEN+" to invite memebers.");
											return true;
										}else{
											player.sendMessage(ChatColor.GOLD+"Do you want to create a new island or use your existing one?");
											player.sendMessage(ChatColor.DARK_RED+"/skyblock yes " +ChatColor.GOLD+"- Create new island.");
											player.sendMessage(ChatColor.DARK_RED+"/skyblock no " +ChatColor.GOLD+ "- Use your existing island.");
											waiting.put(player, "partynewi");
											return true;
										}
									}else{
										player.sendMessage(ChatColor.DARK_RED +"You are already in a party!");
										return true;
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You already have a party!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("invite") || args[1].equals("i")){
							if(player.hasPermission("skyblock.user.party.invite")){
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
														player.sendMessage(ChatColor.DARK_RED +"That player already has a pending invite.");
														return true;
													}
												}else{
													player.sendMessage(ChatColor.DARK_RED +"That player is already in a party!");
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
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("accept") || args[1].equals("a")){
							if(player.hasPermission("skyblock.user.party.accept")){
								if(prtymanager.hasInvite(player.getName())){
									Party invitedprty = prtymanager.getPlayerParty(prtymanager.getInvite(player.getName()).getInviting());
									Party original = invitedprty;
									if(!(invitedprty.getMax() <= invitedprty.getSize())){
										if(islandmanager.hasIsland(player.getName())){
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
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("decline") || args[1].equals("d")){
							if(player.hasPermission("skyblock.user.party.decline")){
								if(prtymanager.hasInvite(player.getName())){
									Player leader = Bukkit.getPlayer(prtymanager.getInvite(player.getName()).getInviting());
									leader.sendMessage(ChatColor.DARK_RED+ player.getName() +" Declined your party invite.");
									prtymanager.removeInvite(player.getName());
									return true;
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You don't have a invite!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("leave") || args[1].equals("l")){
							if(player.hasPermission("skyblock.user.party.leave")){
								if(prtymanager.inParty(player.getName()) || prtymanager.hasParty(player.getName())){

									if(prtymanager.hasParty(player.getName())){
										Party plrprty = prtymanager.getPlayerParty(player.getName());
										if(plrprty.getSize() > 1){
											player.sendMessage(ChatColor.GOLD +"Are you sure? The new party leader will be "+plrprty.getMembers().get(0));
											player.sendMessage(ChatColor.DARK_RED +"/skyblock yes"+ChatColor.GOLD+" or "+ ChatColor.DARK_RED +"/skyblock no");
											waiting.put(player, "partyleave");
											return true;
										}else{
											player.sendMessage(ChatColor.GOLD +"Are you sure?");
											waiting.put(player, "partyleave");
											return true;
										}
									}else{
										player.sendMessage(ChatColor.GOLD +"Are you sure you want to leave the party?");
										player.sendMessage(ChatColor.DARK_RED +"/skyblock yes"+ChatColor.GOLD+" or "+ ChatColor.DARK_RED +"/skyblock no");
										waiting.put(player, "partyleave");
										return true;
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You aren't in a party!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("disband") || args[1].equals("d")){
							if(prtymanager.hasParty(player.getName())){
								waiting.put(player, "partydisband");
								player.sendMessage(ChatColor.GOLD +"Are you sure you want to disband the party?");
								player.sendMessage(ChatColor.DARK_RED +"Your island will be deleted!!");
								player.sendMessage(ChatColor.DARK_RED +"/skyblock yes"+ChatColor.GOLD+" or "+ ChatColor.DARK_RED +"/skyblock no");
								return true;
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You dont have a party to disband!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("kick") || args[1].equals("k")){
							if(player.hasPermission("skyblock.user.party.kick")){
								if(prtymanager.hasParty(player.getName())){
									if(args.length >= 3){
										Player kplayer = Bukkit.getPlayer(args[2]);
										if(kplayer != null){
											if(!kplayer.equals(player)){
												if(prtymanager.inParty(kplayer.getName()) ){
													if(prtymanager.getPlayerParty(kplayer.getName()).equals(prtymanager.getPlayerParty(player.getName()))){
														Party prty = prtymanager.getPlayerParty(player.getName());
														prty.removeMember(kplayer.getName());
														prtymanager.setParty(prtymanager.getPlayerParty(player.getName()), prty);
														islandmanager.removeOwnerFromIsland(player.getName(), kplayer.getName());
														if(kplayer.getWorld().equals(skySMP.skyworld)){
															kplayer.teleport(skySMP.spawnworld.getSpawnLocation());
															kplayer.sendMessage(ChatColor.DARK_RED +"You have been kicked from the party.");
														}
													}else{
														player.sendMessage(ChatColor.DARK_RED +"That player is not in your party.");
														return true;
													}

												}else{
													player.sendMessage(ChatColor.DARK_RED +"That player is not in your party.");
													return true;
												}
											}else{
												player.sendMessage(ChatColor.DARK_RED +"That player is already in a party!");
												return true;
											}
										}else{
											player.sendMessage(ChatColor.DARK_RED +"You can't kick youself!");
											return true;
										}


									}else{
										player.sendMessage(ChatColor.DARK_RED +"You did't specify a player to kick!");
										return true;	
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You don't have a Party!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else{
							player.performCommand("skyblock party");
							return true;
						}
					}else{
						player.sendMessage(ChatColor.YELLOW + "/===================================================\\");//Don't touch the double \\ it's to prevent escape chars
						player.sendMessage(ChatColor.DARK_AQUA + "                    Skyblock Co-Op" + " V 1.5.1");
						player.sendMessage(ChatColor.YELLOW + "\\===================================================/");//Don't touch the double \\ it's to prevent escape chars
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party new" + ChatColor.AQUA + " - Creates a new party." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party leave" + ChatColor.AQUA + " - Leave the party you are in." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party invite" + ChatColor.AQUA + " - Invite someone to your party." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party accept" + ChatColor.AQUA + " - Accept a party invite." );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party decline" + ChatColor.AQUA + " - Declines a party invite" );
						player.sendMessage(ChatColor.DARK_RED + "/skyblock party disband" + ChatColor.AQUA + " - Disbands your party." );
						return true;
					}
				}else if(args[0].equalsIgnoreCase("admin") || args[0].equals("a")){
					if(args.length >= 2){
						if(args[1].equalsIgnoreCase("island") || args[1].equals("i")){
							if(player.hasPermission("skyblock.admin.island")){
								if(args.length >= 3){
									Player tpto = Bukkit.getPlayer(args[2]);
									if(tpto != null){
										if(islandmanager.hasIsland(tpto.getName())){
											islandmanager.teleportHomeOther(tpto.getName(), player);
											player.sendMessage(ChatColor.DARK_RED +"Teleported you to "+ tpto.getName() +"");
											return true;
										}else{
											player.sendMessage(ChatColor.DARK_RED +"This player doesn't have an island!");
											return true;
										}
									}else{										
										OfflinePlayer tpto2 = Bukkit.getOfflinePlayer(args[2]);

										if(tpto2 != null){
											if(islandmanager.hasIsland(tpto2.getName())){
												islandmanager.teleportHomeOther(tpto2.getName(), player);
												return true;
											}else{
												player.sendMessage(ChatColor.DARK_RED +"This player doesn't have an island!");
												return true;
											}
										}else{
											player.sendMessage(ChatColor.DARK_RED +"Could not find player!");
											return true;
										}
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You did not specify a player!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("reprotectall") || args[1].equals("r")){
							if(player.hasPermission("skyblock.admin.reprotect")){
								islandmanager.checkislands(sender);
								player.sendMessage(ChatColor.DARK_RED +"reprotected all islands.");
								return true;
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("protect") || args[1].equals("p")){
							if(player.hasPermission("skyblock.admin.protect")){
								if(args.length >= 3){
									Player protectplr = Bukkit.getPlayer(args[2]);
									if(protectplr != null){
										if(islandmanager.hasIsland(protectplr.getName())){
											boolean result = islandmanager.protectIsland(sender, protectplr.getName());
											if(result){
												player.sendMessage(ChatColor.DARK_GREEN +"Successfully protected island for " + protectplr.getName());
											}
											return true;
										}else{
											player.sendMessage(ChatColor.DARK_RED +"This player doesn't have an island!");
											return true;
										}
									}else{
										player.sendMessage(ChatColor.DARK_RED +"Could not find player!");
										return true;
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You did not specify a player!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("delete") || args[1].equals("d")){
							if(player.hasPermission("skyblock.admin.delete")){
								if(args.length >= 3){
									Player protectplr = Bukkit.getPlayer(args[2]);

									if(protectplr != null){
										if(islandmanager.hasIsland(protectplr.getName())){
											if(protectplr.isOnline()){
												if(protectplr.getWorld().equals(skySMP.skyworld)){
													protectplr.teleport(skySMP.spawnworld.getSpawnLocation());
												}
												protectplr.sendMessage(ChatColor.DARK_RED +"An admin has deleted your island.");

											}
											islandmanager.deleteIsland(protectplr.getName());
											player.sendMessage(ChatColor.DARK_RED +"Successfully deleted " + protectplr.getName()+ "'s island.");

											return true;
										}else{
											player.sendMessage(ChatColor.DARK_RED +"This player doesn't have an island!");
											return true;
										}
									}else{
										OfflinePlayer protectplr2 = Bukkit.getOfflinePlayer(args[2]);
										if(protectplr2 != null){
											islandmanager.deleteIsland(protectplr2.getName());
											player.sendMessage(ChatColor.DARK_RED +"Successfully deleted " + protectplr2.getName()+ "'s island.");
											return true;
										}else{
											player.sendMessage(ChatColor.DARK_RED +"Could not find player!");
											return true;
										}
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You did not specify a player!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else if(args[1].equalsIgnoreCase("check") || args[1].equals("c")){
							if(player.hasPermission("skyblock.admin.check")){
								if(args.length >= 3){
									Player plr = Bukkit.getPlayer(args[2]);
									OfflinePlayer plr2 = null;
									if(plr == null){
										plr2 = Bukkit.getOfflinePlayer(args[2]);
										long time = System.currentTimeMillis() - plr2.getLastPlayed();
										if(time / (1000*60*60*24*30*12) > 20){
											player.sendMessage(ChatColor.DARK_RED + "This player has never logged into the server!");
											return true;	
										}
										if(time != 0){
											player.sendMessage(ChatColor.DARK_GREEN + plr2.getName() + " was last online "+ChatColor.DARK_RED+time / (1000*60*60)+ChatColor.DARK_GREEN+" hours, "+ChatColor.DARK_RED+ (time % (1000*60*60)) / (1000*60)+ChatColor.DARK_GREEN+" minutes and "+ChatColor.DARK_RED+((time % (1000*60*60)) % (1000*60)) / 1000+ChatColor.DARK_GREEN+" seconds ago");

											return true;
										}else{
											player.sendMessage("This player has never logged into the server");
											return true;
										}

									}
									if(plr != null){
										long time = System.currentTimeMillis() - plr.getLastPlayed();
										if(plr.isOnline()){
											player.sendMessage(ChatColor.DARK_GREEN + plr.getName() + " is Online!");
											return true;
										}else if(time != 0){
											player.sendMessage(ChatColor.DARK_GREEN + plr.getName() + " was last online "+ChatColor.DARK_RED+time / (1000*60*60)+ChatColor.DARK_GREEN+" hours, "+ChatColor.DARK_RED+ (time % (1000*60*60)) / (1000*60)+ChatColor.DARK_GREEN+" minutes and "+ChatColor.DARK_RED+((time % (1000*60*60)) % (1000*60)) / 1000+ChatColor.DARK_GREEN+" seconds ago");
											return true;
										}else{
											player.sendMessage("This player has never logged into the server");
											return true;
										}
									}
								}else{
									player.sendMessage(ChatColor.DARK_RED +"You did not specify a player!");
									return true;
								}
							}else{
								player.sendMessage(ChatColor.DARK_RED +"You don't have permission for this command!");
								return true;
							}
						}else{
							player.performCommand("skyblock admin");
							return true;
						}
					}else{
						if(player.hasPermission("skyblock.admin")){
							player.sendMessage(ChatColor.YELLOW + "/===================================================\\");//Don't touch the double \\ it's to prevent escape chars
							player.sendMessage(ChatColor.DARK_AQUA + "                    Skyblock Co-Op" + " V 1.5.1");
							player.sendMessage(ChatColor.YELLOW + "\\===================================================/");//Don't touch the double \\ it's to prevent escape chars
							player.sendMessage(ChatColor.DARK_RED + "/skyblock admin island <player>" + ChatColor.AQUA + " - Teleports you to a specific players island." );
							player.sendMessage(ChatColor.DARK_RED + "/skyblock admin check <player>" + ChatColor.AQUA + " - Checks the last time the player was online." );
							player.sendMessage(ChatColor.DARK_RED + "/skyblock admin delete <player>" + ChatColor.AQUA + " - Deletes a players island." );
							player.sendMessage(ChatColor.DARK_RED + "/skyblock admin protect <player>" + ChatColor.AQUA + " - Protects a island for a specific player." );
							player.sendMessage(ChatColor.DARK_RED + "/skyblock admin reprotectall" + ChatColor.AQUA + " - Recreates all skyblock regions." );
							return true;
						}
					}

				}else if(args[0].equalsIgnoreCase("yes") || args[0].equals("y")){
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

									islandmanager.unprotectIsland(newprty.getLeader());//incase they have a region already
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
										cplayer.sendMessage(ChatColor.GOLD + player.getName() + " left the party.");
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
									try{
									Player mem = Bukkit.getPlayer(cmem);									
									if(mem != null){
										if(mem.isOnline()){
											if(mem.getWorld().equals(skySMP.skyworld)){
												mem.teleport(skySMP.spawnworld.getSpawnLocation());
											}
											mem.sendMessage(ChatColor.GOLD+"The party island has been deleted by the leader.");
										}
									}
									}catch (NullPointerException ex) {
									System.out.println("fffffuuuuuuuuuuuu");
									}
								}
							}

							if(player.getWorld().equals(skySMP.skyworld)){
								player.teleport(skySMP.spawnworld.getSpawnLocation());
							}

							islandmanager.deleteIsland(player.getName());
							waiting.remove(player);
							player.sendMessage(ChatColor.GOLD+"Party island deleted.");
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
			}else{
				sender.sendMessage("Skyblock commands can only be executed by a player.");
				return true;
			}
			if(args[0].equals("help") || args[0].equals("h")){
				player.sendMessage(ChatColor.YELLOW + "/===================================================\\");//Don't touch the double \\ it's to prevent escape chars
				player.sendMessage(ChatColor.DARK_AQUA + "                    Skyblock Co-Op" + " V 1.5.1");
				player.sendMessage(ChatColor.YELLOW + "\\===================================================/");//Don't touch the double \\ it's to prevent escape chars
				player.sendMessage(ChatColor.DARK_RED + "/skyblock help" + ChatColor.AQUA + " - Shows this help menu." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock new" + ChatColor.AQUA + " - Creates a new Skyblock island." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock home" + ChatColor.AQUA + " - Teleports you to your island." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock delete" + ChatColor.AQUA + " - Deletes your current island." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock party" + ChatColor.AQUA + " - Shows help for the party commands." );
				player.sendMessage(ChatColor.DARK_RED + "/skyblock admin" + ChatColor.AQUA + " - Manages the Skyblock plugin." );
				return true;
			}
		}
		return false;
	}
}