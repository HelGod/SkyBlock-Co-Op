package de.qgel.skySMP;

import  java.io.Serializable;

public class Invite implements Serializable {
	private static final long serialVersionUID = 9L;
	private String invitingPlayer;
    private String invitedPlayer;
    
    public Invite(String invited, String inviting) {
        invitingPlayer = inviting;
        invitedPlayer = invited;
        
    }

    public String getInvited(){
    	return this.invitedPlayer;
    }
    
    public String getInviting(){
    	return this.invitingPlayer;
    }
    
}