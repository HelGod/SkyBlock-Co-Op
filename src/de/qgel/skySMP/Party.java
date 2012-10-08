package de.qgel.skySMP;

import  java.io.Serializable;
import java.util.*;

public class Party implements Serializable {
	private static final long serialVersionUID = 7L;
	private String pLeader;
    private String m2;
    private String m3;
    private String m4;
    private Island pIsland;
    private int pSize;
    private static int maxSize = 4;
    private List<String> members;
    
    public Party(String leader, String member2, Island island) {
        pLeader = leader;
        m2 = member2;
        m3 = "EmptySlot";
        m4 = "EmptySlot";
        pSize = 1;//may need to change to 2 for compadablity
        pIsland = island;
        members = new ArrayList<String>();
        
    }

    public String getLeader(){
    	return this.pLeader;
    }
    
    public Island getIsland(){
    	return this.pIsland;
    }
    
    public int getSize(){
    	return this.pSize;
    }
    
    public boolean hasMember(String player)
    {
    	if(player.equalsIgnoreCase(pLeader) || player.equalsIgnoreCase(m2) || player.equalsIgnoreCase(m3) || player.equalsIgnoreCase(m4))
    		return true;
    	else
    		return false;
    }
    
    public List<String> getMembers(){
    	members.clear();
    	members.add(m2);
    	members.add(m3);
    	members.add(m4);
    	return members;
    }
    
    public boolean changeLeader(String oLeader, String nLeader)
    {
    	if (oLeader.equalsIgnoreCase(pLeader))
    	{
    		if (nLeader.equalsIgnoreCase(m2))
    		{
    			m2 = oLeader;
    			pLeader = nLeader;
    			return true;
    		}else if (nLeader.equalsIgnoreCase(m3))
    		{
    			m3 = oLeader;
    			pLeader = nLeader;
    			return true;
    		}else if (nLeader.equalsIgnoreCase(m4))
    		{
    			m4 = oLeader;
    			pLeader = nLeader;
    			return true;
    		}
    	    return false;
    	}
        return false;
    }
    
    public int getMax()
    {
    	return maxSize;
    }
    
    public boolean addMember(String nMember)
    {
    	if (this.getSize() < maxSize)
    	{
    		if (m2.equals("EmptySlot"))
    		{
    			m2 = nMember;
    			pSize++;
    		}else if (m3.equals("EmptySlot"))
    		{
    			m3 = nMember;
    			pSize++;
    		}else if (m4.equals("EmptySlot"))
    		{
    			m4 = nMember;
    			pSize++;
    		}
    	return true;
    	}else
    	{
    		return false;
    	}
    }
    
    public int removeMember(String oMember)
    {
    	if (oMember.equalsIgnoreCase(pLeader))
    	{
    		return 0;
    	}else if (oMember.equalsIgnoreCase(m2))
    	{ // shift m3 and m4 up
    		m2 = m3;
    		m3 = m4;
    		m4 = "EmptySlot";
    		pSize--;
    		return 2;
    	}else if (oMember.equalsIgnoreCase(m3))
    	{
    		m3 = m4;
    		m4 = "EmptySlot";
    		pSize--;
    		return 2;
    	}else if (oMember.equalsIgnoreCase(m4))
    	{
    		m4 = "EmptySlot";
    		pSize--;
    		return 2;
    	}
    	return 1;
    }
}