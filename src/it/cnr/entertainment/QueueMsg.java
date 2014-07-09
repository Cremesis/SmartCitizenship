package it.cnr.entertainment;

import java.util.Date;

public class QueueMsg {
	private int idGame;
	private Date timestamp;
	private int duration;
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getIdGame() {
		return idGame;
	}
	public void setIdGame(int idGame) {
		this.idGame = idGame;
	}
}
