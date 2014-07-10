package it.cnr.entertainment;

import java.util.Date;

public class Opinion {
	
	private int idGame;
	private int idUser;
	private Date timestamp;
	private String msg;
	
	public Opinion(int idGame, int idUser, Date timestamp, String msg) {
		super();
		this.idGame = idGame;
		this.idUser = idUser;
		this.timestamp = timestamp;
		this.msg = msg;
	}
	
	
	public int getIdGame() {
		return idGame;
	}
	public void setIdGame(int idGame) {
		this.idGame = idGame;
	}
	public int getIdUser() {
		return idUser;
	}
	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timeStamp) {
		this.timestamp = timeStamp;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
