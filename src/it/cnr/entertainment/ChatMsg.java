package it.cnr.entertainment;

import java.io.Serializable;

public class ChatMsg implements Serializable{
	
	private int serialNumber=0;
	private String roomName;
	private Serializable content;
	
	public ChatMsg(String room, Serializable content){
		++serialNumber;
		roomName = room;
		this.content = content;
	}
	
	public int getSerialNumber(){
		return serialNumber;
	}
	
		
	public String getRoomName(){
		return roomName;
	}
	
	public Serializable getContent(){
		return content;
	}
	
	
	
	
	
	
}
