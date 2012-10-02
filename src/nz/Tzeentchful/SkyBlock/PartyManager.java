package nz.Tzeentchful.SkyBlock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.qgel.skySMP.Invite;
import de.qgel.skySMP.Island;
import de.qgel.skySMP.Party;

public class PartyManager {
	private static List<Party> partyList = null;
	private static List<Invite> inviteList = new ArrayList<Invite>();
	
	public PartyManager(){
		if(partyList == null){
			partyList = loadPartyList();
		}
	}

	public void clean(){
		partyList = null;
		inviteList = null;
	}
	
	public List<Party> loadPartyList(){
    	if(new File("partylist.bin").exists()){
    		List<Party> load = null;
			try {
				load = (List<Party>)SLAPI.load("partylist.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(load != null){

				return load;
			}
    	}else{
    		return new ArrayList<Party>();
    	}
		return null;
	}
	
	
	public void setPartyList(List<Party> list){
		partyList = list;
	}

	public List<Party> getPartyList(){
		return partyList;
	}

	public void setInviteList(List<Invite> list){
		inviteList = list;
	}

	public List<Invite> getInviteList(){
		return inviteList;
	}

	public void savePartys(){
		if(new File("partylist.bin").exists()){
			try {
				SLAPI.save(partyList, "partylist.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}else{
			try {
				new File("partylist.bin").createNewFile();
				savePartys();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	//===============[party]==============
	public Boolean inParty(String player){

		for(Party prty : partyList){
			if(prty.getMembers().contains(player)){
				return true;
			}
		}
		return false;
	}

	public Boolean hasParty(String player){

		for(Party prty : partyList){
			if(prty.getLeader().equals(player)){
				return true;
			}
		}
		return false;
	}

	public Party getPlayerParty(String player){

		for(Party prty : partyList){
			if(prty.getLeader().equals(player)){
				return prty;
			}
			if(prty.getMembers().contains(player)){
				return prty;
			}

		}
		return null;
	}


	public Party createNewParty(String player, Island island){

		Party nparty = new Party(player, "EmptySlot", island);
		partyList.add(nparty);
		savePartys();
		return nparty;
	}

	public void removeParty(String player){

		Iterator<Party> it =  partyList.iterator();
		
		while(it.hasNext()){
			if(it.next().getLeader().equals(player)){
				it.remove();
			}
		}
		savePartys();
	}

	public void setParty(Party oldp, Party newp){
		partyList.remove(oldp);
		partyList.add(newp);
		savePartys();
	}

	//==========[Invites]==========	
	public Boolean hasInvite(String player){

		for(Invite invite : inviteList){
			if(invite.getInvited().equals(player)){
				return true;
			}
		}
		return false;
	}

	public Invite getInvite(String player){

		for(Invite invite : inviteList){
			if(invite.getInvited().equals(player)){
				return invite;
			}
		}
		return null;
	}

	public void removeInvite(String player){

		
		Iterator<Invite> it = inviteList.iterator();
		while(it.hasNext()){
			if(it.next().getInvited().equalsIgnoreCase(player)){
				it.remove();
				break;
			}
		
		}
	}

	public void invitePlayer(Player player, Player sender){

		if(player.isOnline()){

			player.sendMessage(ChatColor.GOLD + sender.getName() + " has invited you to join their party.");
			player.sendMessage(ChatColor.GOLD +"Use"+ ChatColor.DARK_RED +" /skyblock party accept"+ ChatColor.GOLD +" to join or"+ ChatColor.DARK_RED +" /skyblock party decline"+ ChatColor.GOLD +" to decline");

			Invite invite = new Invite(player.getName(), sender.getName());
			inviteList.add(invite);
		}else{
			sender.sendMessage(ChatColor.GOLD +"The player you invited is offline.");
		}
	}

}
