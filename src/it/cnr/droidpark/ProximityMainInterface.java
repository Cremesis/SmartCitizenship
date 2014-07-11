package it.cnr.droidpark;

import java.util.Map;
import java.util.Set;

public interface ProximityMainInterface{
	public Map getChatList();
	
	public Set getFollowedRooms();
	
	public void followRoom(Integer roomKey, String roomName);
	
}

